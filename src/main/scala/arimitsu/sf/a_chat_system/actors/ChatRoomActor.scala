package arimitsu.sf.a_chat_system.actors

import akka.actor.{ ActorRef, Props }
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.sharding.ShardRegion
import akka.persistence.PersistentActor
import arimitsu.sf.a_chat_system.WebSocketSessionActor
import arimitsu.sf.a_chat_system.actors.ChatRoomActor.Protocol

import scala.concurrent.duration._

class ChatRoomActor extends PersistentActor {
  import ChatRoomActor.Protocol._
  import akka.cluster.pubsub.DistributedPubSubMediator
  context.setReceiveTimeout(120.seconds)

  override def persistenceId: String =
    ChatRoomActor.shardingName + "-" + self.path.name

  override def snapshotPluginId: String = "cassandra-snapshot-store"

  override def journalPluginId: String = "cassandra-journal"

  override def receiveRecover: Receive = {
    case send @ Send(room, message) =>
      this.messages = this.messages :+ message
      context.system.log.info(s"$self recover: $send")
  }

  var messages: List[Message] = Nil

  var sessions: List[Session] = Nil

  val mediator = DistributedPubSub(context.system).mediator

  override def receiveCommand: Receive = {
    case join @ Join(room, name, session) =>
      broadcastMessage(s"join -> $name")
      this.messages.foreach { message =>
        session.handler ! WebSocketSessionActor.Protocol.SendMessage(s"${message.name}: ${message.content}")
      }
      this.sessions = this.sessions :+ session
      mediator ! DistributedPubSubMediator.Subscribe(s"session-close-${session.id}", self)
    case send @ Send(room, message) =>
      persist(send) { _ =>
        broadcastMessage(s"${message.name}: ${message.content}")
        this.messages = this.messages :+ message
      }
    case close: WebSocketSessionActor.Event.Close =>
      this.sessions = this.sessions.filterNot(_.id == close.id)
      context.system.log.info(s"current sessions: ${this.sessions.size}")
      broadcastMessage(s"leave -> ${close.id}")
  }
  private def broadcastMessage(message: String): Unit = {
    this.sessions.foreach(_.handler ! WebSocketSessionActor.Protocol.SendMessage(message))
  }
}

object ChatRoomActor extends ShardingSupport {

  object Protocol {
    case class Join(room: String, name: String, session: Session)
    case class Send(room: String, message: Message)
    case class Message(name: String, content: String)
    case class Session(id: String, handler: ActorRef)
  }

  override val shardingName: String = "chat-room"
  override val props: Props = Props[ChatRoomActor]

  override val extractEntityId: ShardRegion.ExtractEntityId = {
    case send: Protocol.Send => (send.room, send)
    case join: Protocol.Join => (join.room, join)
  }
  override val extractShardId: ShardRegion.ExtractShardId = {
    case send: Protocol.Send => send.room
    case join: Protocol.Join => join.room
  }
}