package com.banwenkinston.streams

import akka.Done
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.ws.Message
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.scaladsl.Flow
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}

import scala.util.{Failure, Success, Try}

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Dec-2016
  */
object FlowUtils {

  implicit class DebugFlow(val underlying: Flow[Message, Message, Any]) {
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

  implicit class CompleteFlow(val underlying: Flow[Message, Message, Any]) extends AnyVal {
    def onComplete(callback: (Try[Done]) => Unit): Flow[Message, Message, Any] = {
      Flow.fromGraph(new GraphStage[FlowShape[Message, Message]] {
        val in: Inlet[Message] = Inlet[Message]("in")
        val out: Outlet[Message] = Outlet[Message]("out")
        override val shape: FlowShape[Message, Message] = FlowShape.of(in, out)

        override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
        // basically identity stage
          new GraphStageLogic(shape) with InHandler with OutHandler {
            def onPush(): Unit = push(out, grab(in))
            def onPull(): Unit = pull(in)

            override def onUpstreamFailure(cause: Throwable): Unit = {
              callback(Failure(cause))
              failStage(cause)
            }

            override def onUpstreamFinish(): Unit = {
              callback(Success(Done))
              completeStage()
            }

            setHandlers(in, out, this)
          }
      })
    }
  }
}
