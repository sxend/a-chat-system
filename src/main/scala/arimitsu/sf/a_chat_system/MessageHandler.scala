package arimitsu.sf.a_chat_system

import akka.actor.{ ActorRef, Props }
import akka.cluster.pubsub.{ DistributedPubSub, DistributedPubSubMediator }
import akka.pattern._
import akka.util.Timeout
import arimitsu.sf.a_chat_system.actors.ChatRoomActor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.socket._
import org.springframework.web.socket.handler.TextWebSocketHandler

import scala.concurrent.duration._
import scala.language.existentials

@Component
class MessageHandler extends TextWebSocketHandler {
  @Autowired
  private val components: Components = null

  import components.system.dispatcher

  implicit private lazy val timeout: Timeout = Timeout(5.seconds)

  private lazy val room = ChatSystem.getShardRegion(ChatRoomActor.shardingName, components.system)

  private lazy val mediator = DistributedPubSub(components.system).mediator

  override def afterConnectionEstablished(session: WebSocketSession) {
    super.afterConnectionEstablished(session)
    val params = parseParams(session)
    val handler = components.system.actorOf(Props(classOf[WebSocketSessionActor], session))
    val sessionProtocol = ChatRoomActor.Protocol.Session(session.getId, handler)
    room ! ChatRoomActor.Protocol.Join(params("room"), params("name"), sessionProtocol)
  }

  override protected def handleTextMessage(session: WebSocketSession, message: TextMessage) {
    super.handleTextMessage(session, message)
    val params = parseParams(session)
    val messageProtocol = ChatRoomActor.Protocol.Message(params("name"), new String(message.asBytes()))
    room ! ChatRoomActor.Protocol.Send(params("room"), messageProtocol)
  }

  override def afterConnectionClosed(session: WebSocketSession, status: CloseStatus): Unit = {
    super.afterConnectionClosed(session, status)
    publishCloseMessage(session)
  }

  override def handleTransportError(session: WebSocketSession, exception: Throwable): Unit = {
    super.handleTransportError(session, exception)
    publishCloseMessage(session)
  }

  private def publishCloseMessage(session: WebSocketSession): Unit = {
    val close = DistributedPubSubMediator.Publish(s"session-close-${session.getId}", WebSocketSessionActor.Event.Close(session.getId))
    components.system.log.info(s"send session close publish: $close")
    mediator ! close
  }

  private def parseParams(session: WebSocketSession) = {
    session.getUri.getQuery.split("&").foldLeft(Map[String, String]())((map, kv) => {
      val sep = kv.split("=")
      map + (sep.head -> sep.last)
    })
  }

}
