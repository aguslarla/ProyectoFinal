package Actors

import Messages._
import akka.actor.{Actor, ActorLogging, ActorPath}
import akka.cluster.ClusterEvent._
import akka.cluster.client.{ClusterClient, ClusterClientSettings}

/**
  * Created by agustin on 2/07/17.
  */
class ActorClient extends Actor with ActorLogging{

  val initialContacts = Set(ActorPath.fromString("akka.tcp://DataFederationSystem@192.168.1.3:2551/system/receptionist"),
                            ActorPath.fromString("akka.tcp://DataFederationSystem@192.168.1.10:2551/system/receptionist"))
  val settings = ClusterClientSettings(context.system).withInitialContacts(initialContacts)

  val clientAkka = context.system.actorOf(ClusterClient.props(settings), "ClientAkka")

  def receive = {
    case "hello" => println(Console.BLUE + "[CLIENT] => Arranca nodo cliente!!")
    case "exit" =>  println(Console.BLUE + "[CLIENT] => Finaliza ejecución!!")
    case ClientQuery(query) => clientAkka ! ClusterClient.Send("/user/serviceA", ClientQuery(query), localAffinity = true)
    case ResultQuery(msg) =>  println(Console.BLUE + "[CLIENT] => " + msg)
                              // Cuando encontremos el separador de row meter un salto de línea para representar los datos
    case QueryVacia(msg) => {
      if (msg.contains("CREATE"))
        println(Console.BLUE + "[CLIENT] => CREATED TABLE!!")
      if (msg.contains("DROP"))
        println(Console.BLUE + "[CLIENT] => DROPPED TABLE!!")
      else
        println(Console.BLUE + "[CLIENT] => " + msg)
    }
    case _: MemberEvent => // ignore
  }
}