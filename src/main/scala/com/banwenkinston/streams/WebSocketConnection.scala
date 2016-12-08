package com.banwenkinston.streams

import akka.{Done, NotUsed}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream._
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.banwenkinston.api.ws.Welcome
import org.json4s.native.JsonMethods.parse
import org.json4s.native.Serialization
import org.json4s.{DefaultFormats, Formats, JObject}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Dec-2016
  */
object WebSocketConnection {
  def getParseFlow(implicit materializer: Materializer): Flow[Message, JObject, NotUsed] = {
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
}

trait WebSocketConnection {
  implicit val formats: Formats = DefaultFormats
  implicit val materializer: Materializer

  val sink: Sink[Message, NotUsed]

  private val welcomeMessage: String = "oh shit what up"

  def getId: AnyVal

  /**
    * Only use this for one off messages
    */
  def sendMessage[A <: AnyRef](message: A): Unit = {
    val serialized: TextMessage = serialize(message)

    Source.single(serialized).runWith(sink)
  }

  def serialize[A <: AnyRef](message: A): TextMessage = {
    TextMessage(Serialization.write(message))
  }

  sendMessage(Welcome(this.welcomeMessage, getId))
}