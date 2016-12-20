package com.banwenkinston.wallboard

import java.util.concurrent.atomic.AtomicInteger

import akka.Done
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.ws.Message
import akka.stream.{Fusing, Materializer}
import akka.stream.scaladsl.Flow
import com.banwenkinston.api.ws.ClientWrapper
import com.banwenkinston.core.{Server, WebSocketConnection}
import com.banwenkinston.utils.JsonUtils._
import org.json4s.JObject

import scala.collection.concurrent.TrieMap
import scala.util.Try

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Dec-2016
  */
class WallboardServer(id: String, val socket: WebSocketConnection[JObject, AnyRef])
                     (implicit val materializer: Materializer, val log: LoggingAdapter) extends Server {
  private val clients: TrieMap[Int, WallboardClient] = new TrieMap[Int, WallboardClient]()
  private val clientIdOffset: AtomicInteger = new AtomicInteger(1)
  private val name: String = "wallboard"

  def registerClient(): Flow[Message, Message, Any] = {
    val uniqueId: Int = clientIdOffset.getAndIncrement()

    WallboardRouter.parseFlow
      .via(WebSocketConnection.open(name + ":" + uniqueId,
        (clientSocket: WebSocketConnection[JObject, AnyRef]) => {
          val client = newClient(uniqueId, clientSocket)

          this.clients.put(uniqueId, client)
        },
        (done: Try[Done]) => {
          this.clients.remove(uniqueId)

          done.failed.foreach(log.error(_, "Failed to close client web socket"))
        }
      ))
      .via(WallboardRouter.serializeFlow)
  }

  private def newClient(clientId: Int, clientSocket: WebSocketConnection[JObject, AnyRef]): WallboardClient = {
    // parse messages from the client and wrap them sending them to the server
    clientSocket.socketSource
      .map(ClientWrapper(clientId, _))
      .runWith(this.socket.socketSink)

    // receive messages from the server, filter them and send them to the client
    this.socket.socketSource
      .via(filterClientFlow)
      .runWith(clientSocket.socketSink)

    new WallboardClient(clientId)
  }

  private def filterClientFlow: Flow[JObject, JObject, Any] = {
    val fusedGraph = Fusing.aggressive(
      Flow[JObject].filter((obj) => {
        // get messages with ids
        (obj \ "id").toOption.isDefined
      })
      .map((obj) => {
        ((obj \ "id").toInt.toInt, (obj \ "data").asInstanceOf[JObject])
      })
      .filter(_._1 == id)
      .map(_._2)
    )

    Flow.fromGraph(fusedGraph)
  }
}
