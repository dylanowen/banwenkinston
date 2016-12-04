package com.banwenkinston.streams

import akka.NotUsed
import akka.http.scaladsl.model.ws.Message
import akka.stream.Fusing.FusedGraph
import akka.stream.scaladsl.Sink
import akka.stream.{FlowShape, Fusing, Materializer}
import com.banwenkinston.utils.JsonUtils._
import org.json4s.{JObject, _}

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Dec-2016
  */
object Client {

}

class Client(id: Int, sink: Sink[Message, NotUsed])(implicit materializer: Materializer) extends WebSocketConnection(id, sink) {

  /**
    * @return a graph that filters out all messages intended for us
    */
  def getInputGraph: FusedGraph[FlowShape[Message, Message], NotUsed] = {
    val flow = WebSocketConnection.getParseFlow
      .filter((obj) => {
        // get messages with ids
        (obj \ "id").toOption.isDefined
      })
      .map((obj) => {
        ((obj \ "id").toInt.toInt, (obj \ "data").asInstanceOf[JObject])
      })
      .filter(_._1 == id)
      .map(_._2)
      .map(serialize)

    Fusing.aggressive(flow)
  }
}
