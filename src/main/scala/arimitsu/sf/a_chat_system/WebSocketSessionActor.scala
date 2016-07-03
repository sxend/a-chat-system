package arimitsu.sf.a_chat_system

import akka.actor.{ Actor, Props }
import org.springframework.web.socket.{ CloseStatus, TextMessage, WebSocketSession }

class WebSocketSessionActor(session: WebSocketSession) extends Actor {
  import WebSocketSessionActor.Protocol._
  import scala.collection.JavaConverters._

  def receive = {
    case GetAttributes =>
      sender() ! session.getAttributes.asScala.toMap
    case GetPrincipal =>
      sender() ! session.getPrincipal
    case GetBinaryMessageSizeLimit =>
      sender() ! session.getBinaryMessageSizeLimit
    case GetId =>
      sender() ! session.getId
    case GetAcceptedProtocol =>
      sender() ! session.getAcceptedProtocol
    case GetHandshakeHeaders =>
      sender() ! session.getHandshakeHeaders
    case GetLocalAddress =>
      sender() ! session.getLocalAddress
    case GetTextMessageSizeLimit =>
      sender() ! session.getTextMessageSizeLimit
    case GetRemoteAddress =>
      sender() ! session.getRemoteAddress
    case IsOpen =>
      sender() ! session.isOpen
    case GetUri =>
      sender() ! session.getUri
    case GetExtensions =>
      sender() ! session.getExtensions.asScala.toList
    case SendMessage(message) if session.isOpen =>
      session.sendMessage(new TextMessage(message))
    case SendMessage(message) if !session.isOpen =>
      context.stop(self)
    case Close(statusOpt) =>
      statusOpt match {
        case Some(status) => session.close(status)
        case None         => session.close()
      }
      context.stop(self)
    case SetTextMessageSizeLimit(messageSizeLimit) =>
      session.setTextMessageSizeLimit(messageSizeLimit)
    case SetBinaryMessageSizeLimit(messageSizeLimit) =>
      session.setBinaryMessageSizeLimit(messageSizeLimit)
    case message: Any =>
      context.system.log.warning(s"unhandled message: $message")
  }

}
object WebSocketSessionActor {

  def toProps(session: WebSocketSession): (Props, String) =
    (Props(classOf[WebSocketSessionActor], session), s"websocket-session-${session.getId}")

  object Protocol {
    case object GetAttributes
    case object GetPrincipal
    case object GetBinaryMessageSizeLimit
    case object GetId
    case object GetAcceptedProtocol
    case object GetHandshakeHeaders
    case object GetLocalAddress
    case object GetTextMessageSizeLimit
    case object GetRemoteAddress
    case object IsOpen
    case object GetUri
    case object GetExtensions
    case class Close(status: Option[CloseStatus] = None)
    case class SendMessage(message: String)
    case class SetTextMessageSizeLimit(messageSizeLimit: Int)
    case class SetBinaryMessageSizeLimit(messageSizeLimit: Int)
  }
}