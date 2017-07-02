package Actors

import akka.actor.{Actor, ActorLogging, ActorPath}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.cluster.client.{ClusterClient, ClusterClientSettings}
import akka.pattern.Patterns
import akka.util.Timeout
import scala.concurrent.duration._

/**
  * Created by agustin on 2/07/17.
  */
class ActorClient extends Actor with ActorLogging{

  case class EnviaMsj(msj: String)

  val initialContacts = Set(ActorPath.fromString("akka.tcp://DataFederationSystem@127.0.0.1:2551/system/receptionist"))
  val settings = ClusterClientSettings(context.system).withInitialContacts(initialContacts)

  val clientAkka = context.system.actorOf(ClusterClient.props(settings), "ClientAkka")

  // subscribe to cluster changes, re-subscribe when restart
 /*override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
  }
  override def postStop(): Unit = cluster.unsubscribe(self)
*/
  def receive = {
    case "hello" => println("Hola nodo cluster !!")
    case EnviaMsj(msj) => {
      implicit val timeout = Timeout(duration = 15 seconds)
      println("Dentro de EnviaMsj: " + msj)
      val result = Patterns.ask(clientAkka, ClusterClient.Send("/user/serviceA", msj, localAffinity = true), timeout)
    }
      //clientAkka ! ClusterClient.Send("/user/serviceA", "hello", localAffinity = true)
    case msg => println("Entra paentro !! ")
                println(msg.toString)

    case _: MemberEvent => // ignore

  }

}