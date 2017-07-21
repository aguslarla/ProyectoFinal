package Actors

import Messages._
import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.cluster.pubsub.DistributedPubSubMediator.SendToAll
import akka.cluster.pubsub.DistributedPubSub
import com.typesafe.config.ConfigFactory
import org.apache.curator.framework.CuratorFramework
import org.apache.zookeeper.CreateMode

/**
  * Created by agustin on 1/06/17.
  */

class ActorCluster(actorRef: ActorRef, zKClient: CuratorFramework) extends Actor with ActorLogging{

  val cluster = Cluster(context.system)

  override def preStart(): Unit =  {
    cluster.subscribe(self, classOf[MemberEvent])
  }

  override def postStop(): Unit = cluster.unsubscribe(self)

  override def receive: Receive = {
    case MemberUp(member) => {
      val msg="Node activated!!"
      log.info("Member is Up: {}", member.address)
      println(Console.RED + "[CLUSTER: " + member.address + "] => " + msg)
    }
    case UnreachableMember(member) =>{
      log.info("Member detected as unreachable: {}", member)
      println(Console.RED + "[CLUSTER: " + member.address + "] => Node unreachable!!" )
    }
    case MemberRemoved(member, previousStatus) =>{
      log.info("Member is Removed: {} after {}", member.address, previousStatus)
      println(Console.RED + "[CLUSTER: " + member.address + "] => Node removed!!" )
    }
    case ClientQuery(query) => {
      println("Llega Query: query")
      // Obtain command "CREATE", "DROP", "SELECT", ...
      val command_zk = query.split(" ")(0).toUpperCase
      if (!command_zk.equals("SELECT")){
        println("Entra poor aqui")
        // Obtain table from query
        var table_zk = ""
        query.split(" ").foreach(t => {
          if (t.contains("tb_"))
            table_zk = t
        })

        // Save Metadata in ZooKeeper
        val zkConfig = ConfigFactory.load("spark_zk.conf")
        val zkMetadata = zkConfig.getString("zkConfig.path_meta")
        val zkTablePath = zkMetadata+"/"+table_zk
        val zkQueryPath = zkMetadata+"/"+table_zk+"/"+command_zk

        if (zKClient.checkExists().forPath(zkTablePath) == null)
          zKClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(zkTablePath)

        if (zKClient.checkExists().forPath(zkQueryPath) == null)
          zKClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(zkQueryPath)

        zKClient.setData().forPath(zkQueryPath, query.getBytes())

        val mediator = DistributedPubSub(context.system).mediator

        mediator ! SendToAll("/user/serviceA", query, allButSelf = true)

      }
       println("Envia a executor")
      actorRef forward QueryToExecutor(query)
    }
    case _: MemberEvent => // ignore
  }
}
