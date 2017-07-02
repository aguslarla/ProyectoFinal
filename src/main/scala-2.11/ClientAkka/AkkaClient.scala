package AkkaClusterClient

/**
  * Created by agustin on 13/06/17.
  */

import Actors.ActorClient
import ClientAkka.EnviaMsj
import akka.actor.{Actor, ActorLogging, ActorPath, ActorSystem, Props}
import akka.cluster.ClusterEvent.MemberEvent
import akka.cluster.client.{ClusterClient, ClusterClientReceptionist, ClusterClientSettings}
import akka.protobuf.Service
import com.typesafe.config.ConfigFactory

import scala.io.StdIn

object AkkaClient  extends App{

    val system = ActorSystem("DataFederationSystem", ConfigFactory.load())

    val clientActor = system.actorOf(Props[ActorClient], name = "clientActor")

    //clientActor ! ClusterClient.Send("/user/serviceA", "hello", localAffinity = true)
    //clientActor ! ClusterClient.SendToAll("/user/serviceA", "hello")

    var query = ""
    while (!query.contentEquals("exit")) {
      query = StdIn.readLine()
      clientActor ! EnviaMsj(query)
    }

    system.terminate()
}
