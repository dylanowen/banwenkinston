package com.banwenkinston.core

import akka.{Done, NotUsed}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream._
import akka.stream.scaladsl.{BroadcastHub, Flow, MergeHub, Sink, Source}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.banwenkinston.api.ws.Welcome
import org.json4s.native.JsonMethods.parse
import org.json4s.native.Serialization
import org.json4s.{DefaultFormats, Formats, JObject}
import com.banwenkinston.utils.FlowUtils._
import akka.event.LoggingAdapter

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Dec-2016
  */
object WebSocketConnection {

  def open[Out, In](name: String, onOpen: (WebSocketConnection[In, Out]) => Unit, onComplete: (Try[Done] => Unit))
          (implicit materializer: Materializer, log: LoggingAdapter): Flow[In, Out, Any] = {
    val sinkToSocket: Sink[In, Source[In, NotUsed]] = BroadcastHub.sink[In].named("fromServerSink")
    val sourceFromSocket: Source[Out, Sink[Out, NotUsed]] = MergeHub.source[Out].named("toServerSource")

    Flow.fromSinkAndSourceMat(sinkToSocket, sourceFromSocket) (
      (source: Source[In, NotUsed], sink: Sink[Out, NotUsed]) => {
        onOpen(new WebSocketConnection(source.named(name + "-fromSocket"), sink.named(name + "-toSocket")))
      }
    ).onComplete(onComplete).debug
  }
}

class WebSocketConnection[+Out, -In](val socketSource: Source[Out, NotUsed], val socketSink: Sink[In, NotUsed])
                                    (implicit materializer: Materializer) {
  /**
    * Only use this for one off messages
    */
  def sendMessage[T <: In](message: T): Unit = {
    Source.single(message).runWith(this.socketSink)
  }
}