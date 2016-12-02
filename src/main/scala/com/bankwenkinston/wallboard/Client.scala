package com.bankwenkinston.wallboard

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
  def create(server: Server)(implicit materializer: Materializer): Flow[Message, Message, Any] = {
    val sink = server.sink

    Flow[Message].map((msg) => {
      println(msg)
      Source.single(msg).runWith(sink)

      msg
    })//.via(Flow.fromSinkAndSource(sink, Source.single(TextMessage("oh shit what up"))))
  }
  /*
    Flow[Message].mapConcat({
      case tm: TextMessage =>
        TextMessage(Source.single("Hello : ") ++ tm.textStream ++ Source.single("!")) :: Nil
      case bm: BinaryMessage =>
        // ignore binary messages but drain content to avoid the stream being clogged
        bm.dataStream.runWith(Sink.ignore)
        Nil
    })
  }
  */
}
