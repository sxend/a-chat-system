package arimitsu.sf.a_chat_system

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

class ChatSystem {
  val config = ConfigFactory.load.getConfig("arimitsu.sf.a-chat-system")
  implicit val system: ActorSystem = ActorSystem(config.getString("system-name"))
  def login(id: String ): Unit = {
    // join cluster
  }
}
