package arimitsu.sf.a_chat_system

import akka.cluster.Cluster

object ChatSystem {
  def main(args: Array[String]): Unit = {
    val components = Components
    import components.system
    val cluster = Cluster(components.system)

  }
}
