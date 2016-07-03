package arimitsu.sf.a_chat_system.actors

import akka.actor.{ ActorSystem, Props }
import akka.cluster.sharding.{ ClusterSharding, ClusterShardingSettings, ShardRegion }

trait ShardingSupport {
  val shardingName: String
  val role: Option[String] = Option("chat-system")
  val props: Props
  val extractEntityId: ShardRegion.ExtractEntityId
  val extractShardId: ShardRegion.ExtractShardId
  def startProxy(system: ActorSystem): Unit = {
    ClusterSharding(system).startProxy(
      typeName = shardingName,
      role = role,
      extractEntityId = extractEntityId,
      extractShardId = extractShardId
    )
  }
  def startSharding(system: ActorSystem): Unit = {
    ClusterSharding(system).start(
      typeName = shardingName,
      entityProps = props,
      settings = ClusterShardingSettings(system).withRole(role),
      extractEntityId = extractEntityId,
      extractShardId = extractShardId)
  }
}
