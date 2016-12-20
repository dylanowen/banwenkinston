package com.banwenkinston.wallboard

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.scaladsl.{Flow, Sink}
import akka.stream.{Fusing, Materializer}
import akka.{Done, NotUsed}
import com.banwenkinston.api.ws.Welcome
import com.banwenkinston.core.{GameRouter, Server, WebSocketConnection}
import org.json4s.native.JsonMethods.parse
import org.json4s.native.Serialization
import org.json4s.{DefaultFormats, Formats, JObject}

import scala.concurrent.Future
import scala.util.Try

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Dec-2016
  */
object WallboardRouter {
  implicit val formats: Formats = DefaultFormats

  def parseFlow(implicit materializer: Materializer): Flow[Message, JObject, NotUsed] = {
    val fusedFlow = Fusing.aggressive(
      Flow[Message].mapAsync(1) {
        // transform websocket message to domain message (string)
        case TextMessage.Strict(text) => Future.successful(text)
        case streamed: TextMessage.Streamed => streamed.textStream.runFold("")(_ ++ _)
        case bm: BinaryMessage =>
          // ignore binary messages but drain content to avoid the stream being clogged
          bm.dataStream.runWith(Sink.ignore)
          Future.successful("")
      }.map((str) => {
        // get our abstract json value and make sure it's an object
        Try(
          parse(str) match {
            case obj: JObject => Some(obj)
            case _ => None
          }
        ).getOrElse(None)
      })
      .filter(_.isDefined) //filter out the objects
      .map(_.get) // get their actual value
    )

    Flow.fromGraph(fusedFlow)
  }

  def serializeFlow(implicit materializer: Materializer): Flow[AnyRef, Message, NotUsed] = {
    val fusedFlow = Fusing.aggressive(Flow[AnyRef]
      .map(Serialization.write(_))
      .map(TextMessage(_))
    )

    Flow.fromGraph(fusedFlow)
  }
}
class WallboardRouter(implicit materializer: Materializer, log: LoggingAdapter) extends GameRouter {
  override val name: String = "wallboard"

  /**
    * creates a flow for a server web socket connection
    */
  def createServerFlow(): Flow[Message, Message, Any] = {
    findUniqueId().acquireAndGet(uniqueId =>
      WallboardRouter.parseFlow
        .via(WebSocketConnection.open(name + ":" + uniqueId,
          (socket: WebSocketConnection[JObject, AnyRef]) => {
            val server = newServer(uniqueId.id, socket)

            this.servers.put(uniqueId.id, server)
          },
          (done: Try[Done]) => {
            this.servers.remove(uniqueId)

            done.failed.foreach(log.error(_, "Failed to close server web socket"))
          }
        ))
        .via(WallboardRouter.serializeFlow)
    )
  }

  def newServer(uniqueId: String, socket: WebSocketConnection[JObject, AnyRef]): Server = {
    val server = new WallboardServer(uniqueId, socket)

    // send our welcome message
    socket.sendMessage(Welcome("oh shit whatup", uniqueId))

    server
  }
}
