package arimitsu.sf.a_chat_system.actors

import akka.actor.{ ActorRef, Props }
import akka.cluster.sharding.ShardRegion
import akka.persistence.PersistentActor
import arimitsu.sf.a_chat_system.WebSocketSessionActor
import arimitsu.sf.a_chat_system.actors.ChatRoomActor.Protocol

import scala.concurrent.duration._

class ChatRoomActor extends PersistentActor {
  context.setReceiveTimeout(120.seconds)

  override def persistenceId: String =
    ChatRoomActor.shardingName + "-" + self.path.name

  override def snapshotPluginId: String = "cassandra-snapshot-store"

  override def journalPluginId: String = "cassandra-journal"

  override def receiveRecover: Receive = {
    case send: Protocol.Send =>
      this.messages = this.messages :+ (send.name, send.message)
      context.system.log.info(s"${self} recover: $send")
  }

  var messages: List[(String, String)] = Nil
  var sessions: List[ActorRef] = Nil

  override def receiveCommand: Receive = {
    case join: Protocol.Join =>
      broadcastMessage(s"join -> ${join.name}")
      this.messages.foreach { message =>
        join.session ! WebSocketSessionActor.Protocol.SendMessage(s"${message._1}: ${message._2}")
      }
      this.sessions = this.sessions :+ join.session
    case send: Protocol.Send =>
      persist(send) { _ =>
        broadcastMessage(s"${send.name}: ${send.message}")
        this.messages = this.messages :+ (send.name, send.message)
      }
    case leave: Protocol.Leave =>
      this.sessions = this.sessions.filterNot(_ == leave.session)
  }
  private def broadcastMessage(message: String): Unit = {
    this.sessions.foreach(_ ! WebSocketSessionActor.Protocol.SendMessage(message))
  }
}

object ChatRoomActor extends ShardingSupport {

  object Protocol {
    case class Join(room: String, name: String, session: ActorRef)
    case class Send(room: String, name: String, message: String)
    case class Leave(room: String, session: ActorRef)
  }

  override val shardingName: String = "chat-room"
  override val props: Props = Props[ChatRoomActor]

  override val extractEntityId: ShardRegion.ExtractEntityId = {
    case send: Protocol.Send   => (send.room, send)
    case join: Protocol.Join   => (join.room, join)
    case leave: Protocol.Leave => (leave.room, leave)
  }
  override val extractShardId: ShardRegion.ExtractShardId = {
    case send: Protocol.Send   => send.room
    case join: Protocol.Join   => join.room
    case leave: Protocol.Leave => leave.room
  }
}