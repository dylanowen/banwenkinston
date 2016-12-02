package com.bankwenkinston.wallboard

import akka.NotUsed
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Keep, MergeHub, Sink, Source}
import com.bankwenkinston.wallboard.Connection.Packet
import org.json4s.JObject

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Dec-2016
  */
object Client {
  case class ClientWrapper(id: Int, message: AnyRef, _type: String = "fromClient") extends Packet
}

class Client(id: Int, sink: Sink[Message, NotUsed])(implicit materializer: Materializer) extends Connection(id, sink) {

}
