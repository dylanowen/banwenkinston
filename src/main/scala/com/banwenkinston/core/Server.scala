package com.banwenkinston.core

import java.util.concurrent.atomic.AtomicInteger

import akka.{Done, NotUsed}
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.ws.Message
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, MergeHub, Sink, Source}
import com.banwenkinston.utils.FlowUtils._
import akka.actor.Status.Success

import scala.collection.concurrent.TrieMap
import scala.language.higherKinds
import scala.util.{Failure, Try}

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Dec-2016
  */
object Server {
  private[core] val PLACE_HOLDER: Server = new Server {
    override def registerClient() = throw new UnsupportedOperationException("This is just a placeholder")
  }
}
trait Server {
  def registerClient(): Flow[Message, Message, Any]
}