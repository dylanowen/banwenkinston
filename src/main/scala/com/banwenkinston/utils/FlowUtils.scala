package com.banwenkinston.utils

import akka.Done
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.ws.Message
import akka.stream.scaladsl.Flow
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}

import scala.util.{Failure, Success, Try}

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Dec-2016
  */
object FlowUtils {

  implicit class DebugFlow[In, Out](val underlying: Flow[In, Out, Any]) {
    def debug(implicit log: LoggingAdapter): Flow[In, Out, Any] = {
      if (log.isDebugEnabled) {
        // wrap the source and sink in debug flows
        Flow[In].map(msg => {
          log.debug(msg.toString)
          msg
        }).via(underlying).map(msg => {
          log.debug(msg.toString)
          msg
        })
      }
      else {
        underlying
      }
    }
  }

  implicit class CompleteFlow[In, Out](val underlying: Flow[In, Out, Any]) extends AnyVal {
    def onComplete(callback: (Try[Done]) => Unit): Flow[In, Out, Any] = {
      this.underlying.via(new GraphStage[FlowShape[Out, Out]] {
        val in: Inlet[Out] = Inlet[Out]("in")
        val out: Outlet[Out] = Outlet[Out]("out")
        override val shape: FlowShape[Out, Out] = FlowShape.of(in, out)

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
