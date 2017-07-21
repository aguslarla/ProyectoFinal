package ClusterAkka

/**
  * Created by agustin on 26/05/17.
  */

import java.nio.charset.Charset

import sys.process._
import Actors.{ActorCluster, ActorExecutor}
import akka.actor.{ActorSystem, Props}
import akka.cluster.client.ClusterClientReceptionist
import com.typesafe.config.ConfigFactory
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.RetryOneTime
import org.apache.spark.sql.SparkSession
import org.apache.zookeeper.CreateMode
import scala.collection.JavaConversions._


object AkkaCluster {
  def main(args: Array[String]): Unit = {

    // Arranco los demonios de hdfs
    println(Console.YELLOW + "\nRUNNING HDFS DAEMONS...")
    "arranca_demonios.sh" !

    // Hago el put del fichero parquet en el HDFS
    println(Console.YELLOW + "\nPUTTING CSV & PARQUET FILES INTO HDFS...")
    "put_hdfs.sh" !

    println(Console.YELLOW + "RUNNING MASTER SPARK...")
    "start_master_spark.sh" !

    println(Console.YELLOW + "RUNNING WORKER SPARK...")
    "start_worker_spark.sh" !

    println(Console.YELLOW + "RUNNING ZK SERVER...")
    "start_zk_server.sh" !

    val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + args(0)).withFallback(ConfigFactory.load())

    // Create an AkkaActorSystem
    val system = ActorSystem.create("DataFederationSystem", config)

    // Creating SparkSession
    val config_spark_zk = ConfigFactory.load("spark_zk.conf")
    val masterSpark = config_spark_zk.getString("sparkConfig.master-url")
    val sparkDriverHost = config_spark_zk.getString("sparkConfig.driver-host")

    val spark = SparkSession.builder()
      .master(masterSpark)
      .appName("SparkDataFederation")
      .config("spark.cores.max", 2)
      .config("spark.driver.host", sparkDriverHost)
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    // Zookeeper Config
    val zKServer = config_spark_zk.getString("zkConfig.host")
    val zk_path_meta = config_spark_zk.getString("zkConfig.path_meta")

    // Creating Zookeeper connection
    val zk_client = CuratorFrameworkFactory.newClient(zKServer,new RetryOneTime(3))
    zk_client.start()
    zk_client.getZookeeperClient.blockUntilConnectedOrTimedOut()

    // If not exist any METADATA then save them.
    if (zk_client.checkExists().forPath(zk_path_meta) == null)
      zk_client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(zk_path_meta)

    // Looking for some table.
    val tables_ZK = zk_client.getChildren().forPath(zk_path_meta)

    tables_ZK.foreach( tabla => {
        val queries_ZK = zk_client.getChildren().forPath(zk_path_meta+"/"+tabla)

        queries_ZK.foreach(comandoZK => {
          val bytesQuery = zk_client.getData().forPath(zk_path_meta+"/"+tabla+"/"+comandoZK)
          val stringQuery = new String (bytesQuery, Charset.forName("UTF-8"))
          // Execute all queries
          val msg = "[CLUSTER] => QUERY in ZOOKEEPER: "
          try {
            val result = spark.sql(stringQuery)
            if (result.count() == 0)
              println(Console.RED + msg + stringQuery + " ejecutada!!")
          }
          catch {
            case e: Exception => {
              val error_msg = e.getMessage
              println(Console.RED + msg + stringQuery + error_msg)
            }
          }
        })
    })

    val executorRef = system.actorOf(Props(classOf[ActorExecutor], spark), "executorRef")

    val serviceA = system.actorOf(Props(classOf[ActorCluster], executorRef, zk_client), "serviceA")
    ClusterClientReceptionist(system).registerService(serviceA)

  }
}

