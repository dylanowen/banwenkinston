package com.bankwenkinston.wallboard

import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Dec-2016
  */
object ServerGraph {
  def apply(implicit materializer: Materializer): Flow[Message, Message, Any] = Flow[Message].mapConcat({
    case tm: TextMessage =>
      TextMessage(Source.single("Hello ") ++ tm.textStream ++ Source.single("!")) :: Nil
    case bm: BinaryMessage =>
      // ignore binary messages but drain content to avoid the stream being clogged
      bm.dataStream.runWith(Sink.ignore)
      Nil
  })
}
