package arimitsu.sf.a_chat_system

import akka.actor.ActorSystem
import com.typesafe.config.{ Config, ConfigFactory }
import org.springframework.stereotype.Component

trait Components {
  val config: Config
  implicit val system: ActorSystem
}

@Component
object Components extends Components {
  override val config: Config = ConfigFactory.load.getConfig("arimitsu.sf.a-chat-system")
  override implicit val system: ActorSystem = ActorSystem(config.getString("system-name"))
}