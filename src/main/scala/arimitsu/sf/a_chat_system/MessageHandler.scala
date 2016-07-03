package arimitsu.sf.a_chat_system

import akka.actor.{ Actor, Props }
import arimitsu.sf.a_chat_system.actors.ChatRoomActor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.socket.handler.TextWebSocketHandler
import org.springframework.web.socket.{ TextMessage, WebSocketSession }

@Component
class MessageHandler extends TextWebSocketHandler {
  @Autowired
  private val components: Components = null

  private lazy val room = ChatSystem.getShardRegion(ChatRoomActor.shardingName, components.system)

  override def afterConnectionEstablished(session: WebSocketSession) {
    val params = parseParams(session)

    val handler = components.system.actorOf(Props(classOf[MessageHandlerActor], session), s"session-handler-${session.getId}")
    room ! ChatRoomActor.Protocol.Join(params("room"), params("name"), handler)
  }

  override protected def handleTextMessage(session: WebSocketSession, message: TextMessage) {
    val params = parseParams(session)
    room ! ChatRoomActor.Protocol.Send(params("room"), params("name"), new String(message.asBytes()))
  }

  private def parseParams(session: WebSocketSession) = {
    session.getUri.getQuery.split("&").foldLeft(Map[String, String]())((map, kv) => {
      val sep = kv.split("=")
      map + (sep.head -> sep.last)
    })
  }
}
class MessageHandlerActor(session: WebSocketSession) extends Actor {
  def receive = {
    case message: String if session.isOpen =>
      session.sendMessage(new TextMessage(message))
  }
}