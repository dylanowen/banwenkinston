package com.bankwenkinston.wallboard

import akka.NotUsed
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Keep, MergeHub, Sink, Source}

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Dec-2016
  */
object Client {

}

class Client(id: Int, sink: Sink[Message, NotUsed])(implicit materializer: Materializer) extends Connection(id, sink) {

}
