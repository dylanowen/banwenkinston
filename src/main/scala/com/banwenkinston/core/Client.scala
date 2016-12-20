package com.banwenkinston.core

import akka.NotUsed
import akka.http.scaladsl.model.ws.Message
import akka.stream.Fusing.FusedGraph
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{FlowShape, Fusing, Materializer}
import com.banwenkinston.utils.JsonUtils._
import org.json4s.{JObject, _}

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Dec-2016
  */
abstract class Client[+Out, -In](val id: Int, socket: WebSocketConnection[Out, In])(implicit val materializer: Materializer) {
}
