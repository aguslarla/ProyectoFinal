package Actors

import Messages.{ActivatedNode, ClientQuery, Initializing, ResultQuery}
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
                    clientAkka ! ClusterClient.Send("/user/serviceA", Initializing(), localAffinity = true)
    case "exit" =>  println("Finaliza ejecución !!")
                    context.system.terminate()
    case ActivatedNode(msg) => println(msg)
    case ClientQuery(query) => clientAkka ! ClusterClient.Send("/user/serviceA", ClientQuery(query), localAffinity = true)
    case ResultQuery(msg) => println(msg)
    case _: MemberEvent => // ignore
  }
}