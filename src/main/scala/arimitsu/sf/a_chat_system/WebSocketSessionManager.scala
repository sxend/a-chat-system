package arimitsu.sf.a_chat_system

import akka.actor.{ Actor, ActorRef }
import org.springframework.web.socket.WebSocketSession
import akka.pattern._

class WebSocketSessionManager extends Actor {
  import WebSocketSessionManager.Protocol._
  var sessions: Map[String, ActorRef] = Map.empty
  def receive = {
    case Get(session) =>
      val (props, id) = WebSocketSessionActor.toProps(session)
      this.sessions.find(_._1 == session.getId) match {
        case Some(actor) =>
          sender() ! actor
        case None =>
          val actor = context.actorOf(props, id)
          this.sessions = this.sessions + (session.getId -> actor)
          sender() ! actor
      }
    case Remove(session) =>
      this.sessions.find(_._1 == session.getId) match {
        case Some((id, actor)) =>
          this.sessions = this.sessions.filterNot(_._1 == session.getId)
          sender() ! actor
        case None =>
          sender() ! new RuntimeException("unregistered session")
      }
  }
}
object WebSocketSessionManager {
  object Protocol {
    case class Get(session: WebSocketSession)
    case class Remove(session: WebSocketSession)
  }
}