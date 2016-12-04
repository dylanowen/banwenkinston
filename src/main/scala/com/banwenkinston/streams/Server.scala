package com.banwenkinston.streams

import java.util.concurrent.atomic.AtomicInteger

import akka.NotUsed
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.model.ws.Message
import akka.stream.Fusing.FusedGraph
import akka.stream.scaladsl.{Flow, MergeHub, Sink, Source, _}
import akka.stream.{FlowShape, Fusing, Graph, Materializer}
import com.banwenkinston.api._
import com.banwenkinston.utils.JsonUtils._
import com.banwenkinston.utils.UniqueIds
import org.json4s._
import FlowUtils._

import scala.collection.concurrent.TrieMap
import scala.language.higherKinds

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Dec-2016
  */
class Server(id: AnyVal, sink: Sink[Message, NotUsed], val source: Source[Message, NotUsed])
            (implicit val materializer: Materializer, val log: LoggingAdapter) extends WebSocketConnection(id, sink) {
  private val clients: TrieMap[AnyVal, Client] = new TrieMap[AnyVal, Client]()
  private val clientIdOffset: AtomicInteger = new AtomicInteger(1)

  def registerClient(): Flow[Message, Message, Any] = {
    val clientId: Int = clientIdOffset.getAndIncrement()
    val clientSource: Source[Message, Sink[Message, NotUsed]] = MergeHub.source[Message]

    // attach the client to the server
    val graph = Fusing.aggressive(getClientMessageWrapperFlow(clientId)
      .via(Flow.fromSinkAndSourceMat(this.sink, clientSource) {
      (_, clientSink) => {
        val client: Client = new Client(clientId, clientSink)
        this.clients.put(clientId, client)

        // attach the server to the client
        this.source.via(client.getInputGraph).to(clientSink).run()

        // let the server know the client exists
        this.sendMessage(ClientConnected(clientId))

        log.info("Client: " + this.id + "->" + clientId + " connected")
      }
    }))

    Flow.fromGraph(graph).debug
  }

  def getServerMessageFlow: Flow[Message, JObject, NotUsed] = {
   WebSocketConnection.getParseFlow
      .filter((obj) => {
        // get messages without ids
        (obj \ "id").toOption.isEmpty
      })
      .map((obj) => {
        (obj \ "data").asInstanceOf[JObject]
      })
  }

  /**
    * @return a flow that wraps all client messages to the server in an object that includes
    *         the clientId
    */
  def getClientMessageWrapperFlow(clientId: Int): Flow[Message, Message, NotUsed] = {
    WebSocketConnection.getParseFlow
      .map(ClientWrapper(clientId, _))
      .map(serialize)
  }
}
