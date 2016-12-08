package com.banwenkinston.snake

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.ws.Message
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import com.banwenkinston.streams.{Client, Server}

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Dec-2016
  */
object SnakeServer {

}
class SnakeServer(id: String)
                 (implicit override val materializer: Materializer, override val log: LoggingAdapter) extends Server(id) {

  override def registerClient(clientId: Int, callback: (Client) => Unit): Flow[Message, Message, Any] = {
    null
  }
}
