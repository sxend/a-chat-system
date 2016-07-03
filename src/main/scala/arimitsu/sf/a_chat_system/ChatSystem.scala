package arimitsu.sf.a_chat_system

import akka.actor.ActorSystem
import akka.cluster.Cluster
import akka.cluster.sharding.ClusterSharding
import arimitsu.sf.a_chat_system.actors.ChatRoomActor

object ChatSystem {
  def main(args: Array[String]): Unit = {
    val components = Components
    val config = components.config
    config.getString("cluster.role") match {
      case "seed"   => startSeed(components)
      case "member" => startMember(components)
      case role     => throw new RuntimeException(s"invalid role: $role")
    }
  }

  def startSeed(components: Components): Unit = {
    Cluster(components.system)
    ChatRoomActor.startSharding(components.system)
  }
  def startMember(components: Components): Unit = {
    Cluster(components.system)
    ChatRoomActor.startProxy(components.system)
  }
  def getShardRegion(typeName: String, system: ActorSystem) =
    ClusterSharding(system).shardRegion(typeName)
}
