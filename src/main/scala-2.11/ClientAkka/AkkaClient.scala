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

    var query = ""
    while (!query.contentEquals("exit")) {
      query = StdIn.readLine()
      if (!query.isEmpty){
        clientActor ! ClientQuery(query)
      }
    }

    system.terminate()
}
