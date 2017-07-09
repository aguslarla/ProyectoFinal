package AkkaClusterClient

/**
  * Created by agustin on 13/06/17.
  */

import Actors.ActorClient
import Messages.ClientQuery
import akka.actor.{ActorSystem, Props}

import scala.io.StdIn

object AkkaClient  extends App{

  val system = ActorSystem("DataFederationClient")

  val clientActor = system.actorOf(Props[ActorClient], name = "clientActor")

  clientActor ! "hello"
  Thread.sleep(500)

  var query = ""

  while (!query.contentEquals("exit")) {
      query = StdIn.readLine("\n Escribe peticion:  ")
      if (!query.isEmpty){
        clientActor ! ClientQuery(query)
      }
      Thread.sleep(1500)
    }

    system.terminate()
}
