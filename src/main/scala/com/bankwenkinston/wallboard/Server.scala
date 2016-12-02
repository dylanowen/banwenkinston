package com.bankwenkinston.wallboard

import java.util.concurrent.atomic.AtomicInteger

import akka.NotUsed
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Keep, MergeHub, RunnableGraph, Sink, Source}
import akka.stream._
import akka.stream.scaladsl._
import akka.event.LoggingAdapter
import akka.stream._
import akka.Done
import akka.stream.impl.StreamLayout.Module
import akka.stream.impl._
import akka.stream.impl.fusing._
import akka.stream.stage.AbstractStage.{ PushPullGraphStage, PushPullGraphStageWithMaterializedValue }
import akka.stream.stage._
import org.reactivestreams.{ Processor, Publisher, Subscriber, Subscription }
import scala.annotation.unchecked.uncheckedVariance
import scala.collection.immutable
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.language.higherKinds
import akka.stream.impl.fusing.FlattenMerge

import scala.collection.concurrent.TrieMap

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Dec-2016
  */
object Server {
  private val servers: TrieMap[String, Server] = new TrieMap[String, Server]()
  private val id: AtomicInteger = new AtomicInteger(1)

  def create(implicit materializer: Materializer): Flow[Message, Message, Any] = {
    val source: Source[Message, Sink[Message, NotUsed]] = MergeHub.source[Message]

    Flow.fromSinkAndSourceMat(Sink.ignore, source) {
      (_, sink) => {
        val id: String = genId
        val server: Server = new Server(sink)
        servers.put(id, server)

        println("new server: " + id)
      }
    }.map((msg) => {
      println("To Server: " + msg)
      msg
    })
  }

  def get(id: String): Option[Server] = servers.get(id)

  private def genId: String = "server_" + id.getAndIncrement()
}
class Server(val sink: Sink[Message, NotUsed])(implicit materializer: Materializer) {

  Source.single(TextMessage("oh shit what up")).runWith(sink)

  //private val clients: TrieMap[Int, ] = TrieMap[I]

}
