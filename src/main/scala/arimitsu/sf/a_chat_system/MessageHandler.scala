package arimitsu.sf.a_chat_system

import akka.actor.{ Actor, ActorRef, Props }
import arimitsu.sf.a_chat_system.actors.ChatRoomActor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.socket.handler.TextWebSocketHandler
import org.springframework.web.socket._

import scala.language.existentials
import akka.pattern._
import akka.util.Timeout
import scala.concurrent.duration._

@Component
class MessageHandler extends TextWebSocketHandler {
  @Autowired
  private val components: Components = null

  import components.system.dispatcher

  implicit private lazy val timeout: Timeout = Timeout(5.seconds)

  private lazy val room = ChatSystem.getShardRegion(ChatRoomActor.shardingName, components.system)

  private lazy val manager: ActorRef = components.webSocketSessionManager(components.system)

  override def afterConnectionEstablished(session: WebSocketSession) {
    val params = parseParams(session)
    manager.ask(WebSocketSessionManager.Protocol.Get(session)).mapTo[ActorRef].onSuccess {
      case handler =>
        room ! ChatRoomActor.Protocol.Join(params("room"), params("name"), handler)
    }
  }

  override protected def handleTextMessage(session: WebSocketSession, message: TextMessage) {
    val params = parseParams(session)
    room ! ChatRoomActor.Protocol.Send(params("room"), params("name"), new String(message.asBytes()))
  }

  override def afterConnectionClosed(session: WebSocketSession, status: CloseStatus): Unit = {
    val params = parseParams(session)
    manager.ask(WebSocketSessionManager.Protocol.Remove(session)).mapTo[ActorRef].onSuccess {
      case handler =>
        room ! ChatRoomActor.Protocol.Leave(params("room"), handler)
    }
  }

  private def parseParams(session: WebSocketSession) = {
    session.getUri.getQuery.split("&").foldLeft(Map[String, String]())((map, kv) => {
      val sep = kv.split("=")
      map + (sep.head -> sep.last)
    })
  }

}
