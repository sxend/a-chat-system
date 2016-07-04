package arimitsu.sf.a_chat_system

import akka.actor.{ Actor, Props }
import akka.cluster.pubsub.{ DistributedPubSub, DistributedPubSubMediator }
import org.springframework.web.socket.{ CloseStatus, TextMessage, WebSocketSession }

import scala.concurrent.duration._
import scala.util.Try

class WebSocketSessionActor(session: WebSocketSession) extends Actor {
  val mediator = DistributedPubSub(context.system).mediator

  mediator ! DistributedPubSubMediator.Subscribe(s"session-close-${session.getId}", self)

  import WebSocketSessionActor.Protocol._
  def receive = {
    case SendMessage(message) if session.isOpen =>
      session.sendMessage(new TextMessage(message))
    case WebSocketSessionActor.Event.Close(id) if id == session.getId =>
      context.stop(self)
    case WebSocketSessionActor.Event.Close(id) =>
    case message: Any =>
      context.system.log.warning(s"unhandled message: $message")
  }

  import context.dispatcher
  private def check(): Unit = {
    if (!session.isOpen) {
      Try {
        context.system.log.info(s"this session is already closed. ${session.getId}")
        mediator ! DistributedPubSubMediator.Publish(s"session-close-${session.getId}", WebSocketSessionActor.Event.Close(session.getId))
      }
    } else {
      context.system.scheduler.scheduleOnce(5.seconds)(check())
    }
  }
  context.system.scheduler.scheduleOnce(5.seconds)(check())
}

object WebSocketSessionActor {

  def toProps(session: WebSocketSession): (Props, String) =
    (Props(classOf[WebSocketSessionActor], session), s"websocket-session-${session.getId}")

  object Protocol {
    case class SendMessage(message: String)
  }
  object Event {
    case class Close(id: String)
  }
}