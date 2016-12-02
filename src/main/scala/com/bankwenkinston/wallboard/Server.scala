package com.bankwenkinston.wallboard

import java.util.concurrent.atomic.AtomicInteger

import akka.NotUsed
import akka.http.scaladsl.model.ws.Message
import akka.stream.Fusing.FusedGraph
import akka.stream.scaladsl.{Flow, MergeHub, Sink, Source, _}
import akka.stream.{FlowShape, Fusing, Materializer}
import com.bankwenkinston.wallboard.Connection.Welcome
import com.bankwenkinston.wallboard.JsonUtils._
import org.json4s._

import scala.collection.concurrent.TrieMap
import scala.language.higherKinds

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Dec-2016
  */
object Server {
  private val servers: TrieMap[String, Server] = new TrieMap[String, Server]()
  private val id: AtomicInteger = new AtomicInteger(1)

  def create(implicit materializer: Materializer): Flow[Message, Message, Any] = {
    val serverSource: Source[Message, Sink[Message, NotUsed]] = MergeHub.source[Message]
    val serverSink: Sink[Message, Source[Message, NotUsed]] = BroadcastHub.sink[Message]

    Connection.debugFlow.via(Flow.fromSinkAndSourceMat(serverSink, serverSource) {
      (source, sink) => {
        val id: String = genId
        val server: Server = new Server(id, sink, source)
        servers.put(id, server)

        //val source: Source[Message, Sink[Message, NotUsed]] = MergeHub.source[Message]
        println("Connected to Server: " + id)
      }
    })
  }

  def get(id: String): Option[Server] = servers.get(id)

  private def genId: String = "server_" + id.getAndIncrement()
}
class Server(id: String, sink: Sink[Message, NotUsed], val source: Source[Message, NotUsed])(implicit materializer: Materializer) extends Connection(id.asInstanceOf[AnyVal], sink) {
  private val clients: TrieMap[AnyVal, Client] = new TrieMap[AnyVal, Client]()
  private val clientIdOffset: AtomicInteger = new AtomicInteger(1)

  def createClient(): Flow[Message, Message, Any] = {
    val clientId: Int = clientIdOffset.getAndIncrement()
    val clientSource: Source[Message, Sink[Message, NotUsed]] = MergeHub.source[Message]

    // make sure the messages were intended for us
    val clientGraph = getClientGraph(clientId)

    // attach the client to the server
    Connection.debugFlow.via(wrapClientMessageToServerGraph(clientId))
      .via(Flow.fromSinkAndSourceMat(this.sink, clientSource) {
      (_, clientSink) => {
        val client: Client = new Client(clientId, clientSink)
        this.clients.put(clientId, client)

        // attach the server to the client
        this.source.via(clientGraph).to(clientSink).run()

        // let the server know the client exists
        this.sendMessage(Welcome("welcome client", clientId))

        println("Connected to Client: " + clientId)
      }
    })
  }

  def getServerMessageFlow: Flow[Message, JObject, NotUsed] = {
    Flow[Message].via(Connection.getParseGraph)
      .filter((obj) => {
        // get messages without ids
        (obj \ "id").toOption.isEmpty
      })
      .map((obj) => {
        (obj \ "data").asInstanceOf[JObject]
      })
  }

  def getClientGraph(clientId: Int): FusedGraph[FlowShape[Message, Message], NotUsed] = {
    val flow = Connection.getParseGraph
      .filter((obj) => {
        // get messages with ids
        (obj \ "id").toOption.isDefined
      })
      .map((obj) => {
        ((obj \ "id").toInt.toInt, (obj \ "data").asInstanceOf[JObject])
      })
      .filter(_._1 == clientId)
      .map(_._2)
      .map(Connection.serialize)

    Fusing.aggressive(flow)
  }

  def wrapClientMessageToServerGraph(clientId: Int): FusedGraph[FlowShape[Message, Message], NotUsed] = {
    val flow = Connection.getParseGraph
      .map(Client.ClientWrapper(clientId, _))
      .map(Connection.serialize)

    Fusing.aggressive(flow)
  }
}
