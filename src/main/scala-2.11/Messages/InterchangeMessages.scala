package Messages

/**
  * Created by agustin on 3/07/17.
  */
case class ClientQuery(query: String)
case class ResultQuery(res: String)
case class Initializing()
case class ActivatedNode(msg: String)
case class DisconnectedNode(msg: String)