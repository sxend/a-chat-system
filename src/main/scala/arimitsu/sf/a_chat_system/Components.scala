package arimitsu.sf.a_chat_system

import akka.actor.{ ActorRef, ActorRefFactory, ActorSystem, Props }
import com.typesafe.config.{ Config, ConfigFactory }
import org.springframework.stereotype.Component

trait Components {
  val config: Config
  implicit val system: ActorSystem
}

@Component
object Components extends Components {
  override val config: Config = ConfigFactory.load.getConfig("arimitsu.sf.a-chat-system")
  override implicit val system: ActorSystem = ActorSystem(config.getString("system-name"), ConfigFactory.load.withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.port = 9000")))
}