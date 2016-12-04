package com.banwenkinston.streams

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.ws.Message
import akka.stream.scaladsl.Flow

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Dec-2016
  */
object FlowUtils {

  implicit class DebugFlow(val underlying: Flow[Message, Message, Any]) extends AnyVal {
    def debug(implicit log: LoggingAdapter): Flow[Message, Message, Any] = {
      if (log.isDebugEnabled) {
        val debug: Flow[Message, Message, Any] = Flow[Message].map(msg => {
          log.debug(msg.toString)
          msg
        })

        // wrap the source and sink in debug flows
        debug.via(underlying).via(debug)
      }
      else {
        underlying
      }
    }
  }
}
