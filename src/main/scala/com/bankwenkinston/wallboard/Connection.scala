package com.bankwenkinston.wallboard

import akka.NotUsed
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.{FlowShape, Fusing, Materializer}
import akka.stream.scaladsl.{Flow, MergeHub, Sink, Source}
import com.bankwenkinston.wallboard.Connection.Welcome
import org.json4s.JsonAST.JValue
import org.json4s.native.JsonMethods.parse
import org.json4s.{DefaultFormats, Formats, JObject}
import org.json4s.native.Serialization
import akka.stream.Fusing.FusedGraph

import scala.concurrent.Future
import scala.util.Try

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Dec-2016
  */
object Connection {
  implicit val formats: Formats = DefaultFormats

  // enforce _type type on all packets
  trait Packet {
    def _type: String
  }

  case class Welcome(message: String, id: AnyVal, _type: String = "welcome") extends Packet

  val debugFlow: Flow[Message, Message, Any] = Flow[Message].map((msg) => {
    println(msg)

    msg
  })

  def getParseGraph(implicit materializer: Materializer): Flow[Message, JObject, NotUsed] = {
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
  }

  /*
  def create(callback: (Sink[Message, NotUsed]) => Unit)(implicit materializer: Materializer): Flow[Message, Message, Any] = {
    val source: Source[Message, Sink[Message, NotUsed]] = MergeHub.source[Message]

    Flow.fromSinkAndSourceMat(Sink.ignore, source) {
      (_, sink) => callback(sink)
    }
  }
  */

  def serialize[A <: AnyRef](message: A): TextMessage = {
    TextMessage(Serialization.write(message))
  }
}
class Connection(val id: AnyVal, val sink: Sink[Message, NotUsed])(implicit materializer: Materializer) {
  /**
    * Only use this for one off messages
    */
  def sendMessage[A <: AnyRef](message: A): Unit = {
    val serialized: TextMessage = Connection.serialize(message)

    Source.single(serialized).runWith(sink)
  }

  sendMessage(Welcome("oh shit what up", this.id))
}