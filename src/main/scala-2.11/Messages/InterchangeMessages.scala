package Messages

import akka.actor.ActorRef

/**
  * Created by agustin on 3/07/17.
  */
case class ClientQuery(query: String)
case class QueryToExecutor(res_query: String)
case class ResultQuery(res: String)
case class QueryVacia(msg: String)