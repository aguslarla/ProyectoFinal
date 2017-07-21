package Actors

import Messages.{QueryToExecutor, QueryVacia, ResultQuery}
import akka.actor.{Actor, ActorLogging}
import akka.cluster.ClusterEvent.{MemberEvent, MemberRemoved, MemberUp, UnreachableMember}
import org.apache.spark.sql.SparkSession

/**
  * Created by agustin on 16/07/17.
  */
class ActorExecutor(sparkSession: SparkSession) extends Actor with ActorLogging{
  def receive = {
    case MemberUp(member) => {
      log.info("Member is Up: {}", member.address)
      println(Console.GREEN + "[CLUSTER: " + member.address + "] => Node EXECUTOR activated!!")
    }
    case UnreachableMember(member) =>{
      log.info("Member detected as unreachable: {}", member)
      println(Console.GREEN + "[CLUSTER: " + member.address + "] => Node EXECUTOR unreachable!!")
    }
    case MemberRemoved(member, previousStatus) =>{
      log.info("Member is Removed: {} after {}", member.address, previousStatus)
      println(Console.GREEN + "[CLUSTER: " + member.address + "] => Node EXECUTOR removed!!")
    }
    case QueryToExecutor(query) => {
      println(Console.GREEN + "[CLUSTER] => Query: " + query + " received!!")
  println(sparkSession)

      val res = sparkSession.sql(query)
      if (res.count() != 0)
        {
          println("Contiene resultados!")
          res.collect().foreach(t => sender ! ResultQuery(t.toString()))
        }

      //sender ! ResultQuery(res.collect().map(_.toString).mkString(","))
      else{
        val msg = query.toUpperCase + " no devuelve ningún dato!! "
        println("Query vacia")
        sender ! QueryVacia(msg)
      }
    }
    case _: MemberEvent => // ignore
  }
}
