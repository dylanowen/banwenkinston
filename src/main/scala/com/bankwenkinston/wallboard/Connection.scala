package com.bankwenkinston.wallboard

import akka.NotUsed
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, MergeHub, Sink, Source}
import com.bankwenkinston.wallboard.Connection.Welcome
import org.json4s.{DefaultFormats, Formats}
import org.json4s.native.Serialization

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Dec-2016
  */
object Connection {
  implicit val formats: Formats = DefaultFormats

  case class Welcome(message: String, id: AnyVal)

  val debugFlow: Flow[Message, Message, Any] = Flow[Message].map((msg) => {
    println(msg)

    msg
  })

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