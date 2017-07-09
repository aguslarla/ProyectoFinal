package Actors

import Messages._
import akka.actor.{Actor, ActorLogging, FSM}
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SparkSession

/**
  * Created by agustin on 1/06/17.
  */

// Defining STATES of actor.
sealed trait ActorState
case object InactiveSt extends ActorState
case object ActiveSt extends ActorState
case object DisconnectedSt extends ActorState

// Defining DATA between state transitions.
sealed trait DataTrans
case object NoData extends DataTrans


class ActorCluster extends Actor with ActorLogging with FSM[ActorState, DataTrans] {

  val cluster = Cluster(context.system)

  var counter = 0

  // Inicialize ActorState
  startWith(InactiveSt, NoData)

  val config = ConfigFactory.load()
  val masterSpark = config.getString("sparkConfig.master-url")
  val sparkDriverHost = config.getString("sparkConfig.driver-host")
  val zKHost = config.getString("zkConfig.host")

  // Creating SparkSession
  val spark = SparkSession.builder()
    .master(masterSpark)
    .appName("SparkDataFederation")
    .config("spark.cores.max", 2)
    .config("spark.driver.host", sparkDriverHost)
    .getOrCreate()

  // Creating Zookeeper connection

  // Reading datasets and creating temp table
  //val df_parquet = spark.read.parquet("")
  val df_csv = spark.read
    .format("com.databricks.spark.csv")
    .option("header", "true")
    .option("mode", "DROPMALFORMED")
    .load("airports.csv")

  df_csv.createOrReplaceTempView("airports")
  // Reviewing database to checking new table.
  println("Tabla " + df_csv.sqlContext.tableNames()(0).toUpperCase + " creada!! ")

  when(InactiveSt) {
    case Event(Initializing(), NoData) =>

      val msg = "Nodo Cluster Activado!!"

      sender() ! ActivatedNode(msg)
      goto(ActiveSt) using NoData
  }

  when(ActiveSt) {
    case Event(ClientQuery(query), NoData) =>
      val res = spark.sql(query)
      if (res.count() != 0)
        res.collect().foreach(t => sender ! ResultQuery(t.toString()))
      else
        sender() ! ResultQuery("La query: " + query.toUpperCase + " no devuelve ningún dato!! ")
      stay() using NoData
  }

  whenUnhandled {
    case _ => goto(InactiveSt)
  }
}
