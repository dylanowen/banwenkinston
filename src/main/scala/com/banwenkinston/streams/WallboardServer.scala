package com.banwenkinston.streams

import akka.NotUsed
import akka.http.scaladsl.model.ws.Message
import akka.stream.{Fusing, Materializer}
import akka.stream.scaladsl.{Flow, MergeHub, Sink, Source}
import com.banwenkinston.api.ws.{ClientConnected, ClientWrapper}
import org.json4s.JObject
import FlowUtils._
import akka.event.LoggingAdapter

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Dec-2016
  */
class WallboardServer(id: String, override val sink: Sink[Message, NotUsed], val source: Source[Message, NotUsed])
                     (implicit override val materializer: Materializer, override val log: LoggingAdapter) extends Server(id) with WebSocketConnection {

  override def getId: AnyVal = id.asInstanceOf[AnyVal]

  def registerClient(): Flow[Message, Message, Any] = {
    val clientId: Int = getNextClientId
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

  /**
    * @return a flow that wraps all client messages to the server in an object that includes
    *         the clientId
    */
  def getClientMessageWrapperFlow(clientId: Int): Flow[Message, Message, NotUsed] = {
    WebSocketConnection.getParseFlow
      .map(ClientWrapper(clientId, _))
      .map(serialize)
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
}
