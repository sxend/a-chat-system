package arimitsu.sf.a_chat_system.actors

import akka.actor.{ ActorRef, Props }
import akka.cluster.sharding.ShardRegion
import akka.persistence.PersistentActor
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
      context.system.log.info(s"$send")
  }

  var messages: List[(String, String)] = Nil
  var sessions: List[ActorRef] = Nil

  override def receiveCommand: Receive = {
    case join: Protocol.Join =>
      broadcastMessage(s"join -> ${join.name}")
      this.messages.foreach { message =>
        join.session ! s"${message._1}: ${message._2}"
      }
      this.sessions = this.sessions :+ join.session
    case send: Protocol.Send =>
      persist(send) { ev =>
        broadcastMessage(s"${send.name}: ${send.message}")
        this.messages = this.messages :+ (send.name, send.message)
      }
  }
  private def broadcastMessage(message: String): Unit = {
    this.sessions.foreach(_ ! message)
  }
}

object ChatRoomActor extends ShardingSupport {

  object Protocol {
    case class Join(room: String, name: String, session: ActorRef)
    case class Send(room: String, name: String, message: String)
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