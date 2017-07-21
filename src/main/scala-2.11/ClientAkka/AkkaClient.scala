package AkkaClusterClient

/**
  * Created by agustin on 13/06/17.
  */

import Actors.ActorClient
import Messages.ClientQuery
import akka.actor.{ActorSystem, Props}

import scala.io.{Source, StdIn}

object AkkaClient  extends App{

  val system = ActorSystem("DataFederationClient")

  val clientActor = system.actorOf(Props[ActorClient], name = "clientActor")

  clientActor ! "hello"
  Thread.sleep(500)

  var query = ""

  // QUERIES FROM FILE
  println(Console.BLUE + "[CLIENT] => QUERIES FROM FILE\n")
  val filename = "Queries_sql"
  for (query <- Source.fromFile(filename).getLines) {
    if (!query.startsWith("//")){
      println(Console.BLUE + "[CLIENT] => " + query)
      clientActor ! ClientQuery(query)
      Thread.sleep(3500)
    }
  }

  // QUERIES FROM CONSOLE
  while (!query.contentEquals("exit")) {
      query = StdIn.readLine(Console.BLUE + "\n[CLIENT] => Escribe peticion:  ")
      if (!query.isEmpty){
        clientActor ! ClientQuery(query)
      }
     // Thread.sleep(1500)
  }
  system.terminate()
}
