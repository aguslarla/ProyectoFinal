package ClusterAkka

/**
  * Created by agustin on 26/05/17.
  */

import sys.process._

import Actors.ActorCluster
import akka.actor.{ActorSystem, Address, Props}
import akka.cluster.Cluster
import akka.cluster.client.{ClusterClient, ClusterClientReceptionist, ClusterClientSettings}
import com.typesafe.config.ConfigFactory

object AkkaCluster {
  def main(args: Array[String]): Unit = {

    // Arranco los demonios de hdfs
    "arranca_demonios.sh" !

    // Hago el put del fichero parquet en el HDFS
    "put_hdfs.sh" !

    val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + args(0)).withFallback(ConfigFactory.load())

    // Create an AkkaActorSystem
    val system = ActorSystem.create("DataFederationSystem", config)

    val seedAddress = Address("akka.tcp", "DataFederationSystem", "127.0.0.1", args(1).toInt)
    Cluster(system).join(seedAddress)

    val serviceA = system.actorOf(Props[ActorCluster], "serviceA")
    ClusterClientReceptionist(system).registerService(serviceA)

    while (true){
   //   Thread.sleep(5000)
      //actor1.tell("hello", actor1)
    }

    system.terminate()
  }
}

