package Actors

import Messages.{ClientQuery, ResultQuery}
import akka.actor.{Actor, ActorLogging, ActorPath}
import akka.cluster.ClusterEvent._
import akka.cluster.client.{ClusterClient, ClusterClientSettings}

/**
  * Created by agustin on 2/07/17.
  */
class ActorClient extends Actor with ActorLogging{

  val initialContacts = Set(ActorPath.fromString("akka.tcp://DataFederationSystem@127.0.0.1:2551/system/receptionist"))
  val settings = ClusterClientSettings(context.system).withInitialContacts(initialContacts)

  val clientAkka = context.system.actorOf(ClusterClient.props(settings), "ClientAkka")

  def receive = {
    case "hello" => println("Arranca nodo cliente !!")
    case "exit" =>  println("Finaliza ejecución !!")
                    context.system.terminate()
    case ClientQuery(query) => println("Query del cliente: " + query)
                               clientAkka ! ClusterClient.Send("/user/serviceA", ClientQuery(query), localAffinity = true)
    case ResultQuery(msg) => println(msg)
    case _: MemberEvent => // ignore
  }
}