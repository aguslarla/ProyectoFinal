package Actors

import akka.actor.{Actor, ActorLogging}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import org.apache.spark.sql.SparkSession

/**
  * Created by agustin on 1/06/17.
  */
class ActorCluster extends Actor with ActorLogging {

  val cluster = Cluster(context.system)

  var counter = 0

  // Creating SparkSession
  val spark = SparkSession.builder()
                          .master("local")
                          .appName("SparkDataFederation")
                          .config("spark.cores.max", 1)
                          .getOrCreate()

  // Leemos datasets y creamos una tabla temporal
  //val df_parquet = spark.read.parquet("")
  val df_csv = spark.read
                    .format("com.databricks.spark.csv")
                    .option("header", "true")
                    .option("mode", "DROPMALFORMED")
                    .load("airports.csv")

  df_csv.createOrReplaceTempView("airports")
  // Para comprobar si se ha creado correctamente miramos los nombres de tablas que existen
  println("Tabla " + df_csv.sqlContext.tableNames()(0) + " creada!! ")

  // subscribe to cluster changes, re-subscribe when restart
  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
  }
  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive = {
    case MemberUp(member) => log.info("Member is Up: {}", member.address)
    case UnreachableMember(member) => log.info("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) => log.info("Member is Removed: {} after {}", member.address, previousStatus)
    case "hello" => println("Hello " + counter)
                    counter+=1
                    sender ! "received"
    case "received" => println("Hi! " + counter)
                    counter+=1
                    sender ! "close"
    case "exit" =>  println("Hasta luego Lucas!!")
                    context.system.terminate()
    case msg => println("Query Sql: " + msg.toString)
                val res = spark.sql(msg.toString)
                res.collect().foreach(t => println())
    case _: MemberEvent => // ignore
  }
}