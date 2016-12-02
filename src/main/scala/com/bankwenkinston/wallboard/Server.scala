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
import akka.stream.stage.AbstractStage.{PushPullGraphStage, PushPullGraphStageWithMaterializedValue}
import akka.stream.stage._
import org.reactivestreams.{Processor, Publisher, Subscriber, Subscription}

import scala.annotation.unchecked.uncheckedVariance
import scala.collection.immutable
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.language.higherKinds
import akka.stream.impl.fusing.FlattenMerge
import com.bankwenkinston.wallboard.Connection.Welcome
import org.json4s.JsonAST.JValue
import org.json4s._
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{read, write}
import org.json4s.native.JsonMethods._
import JsonUtils._
import com.bankwenkinston.wallboard.Client.NewClient

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
    val (serverSink, serverSource) = MergeHub.source[Message].toMat(BroadcastHub.sink[Message])(Keep.both).run()

    val id: String = genId
    val server: Server = new Server(id, serverSink, serverSource)
    servers.put(id, server)

    //val source: Source[Message, Sink[Message, NotUsed]] = MergeHub.source[Message]

    Flow.fromSinkAndSource(serverSink, serverSource)
  }

  def get(id: String): Option[Server] = servers.get(id)

  private def genId: String = "server_" + id.getAndIncrement()
}
class Server(id: String, sink: Sink[Message, NotUsed], val source: Source[Message, NotUsed])(implicit materializer: Materializer) extends Connection(id.asInstanceOf[AnyVal], sink) {
  private val clients: TrieMap[AnyVal, Client] = new TrieMap[AnyVal, Client]()
  private val clientIdOffset: AtomicInteger = new AtomicInteger(1)

  def createClient(): Flow[Message, Message, Any] = {
    val clientId: Int = clientIdOffset.getAndIncrement()
    val clientSource: Source[Message, Sink[Message, NotUsed]] = MergeHub.source[Message]

    // make sure the messages were intended for us
    val filterFlow = Flow[Message].mapAsync(1) {
      // transform websocket message to domain message (string)
      case TextMessage.Strict(text) => Future.successful(text)
      case streamed: TextMessage.Streamed => streamed.textStream.runFold("")(_ ++ _)
      case bm: BinaryMessage =>
        // ignore binary messages but drain content to avoid the stream being clogged
        bm.dataStream.runWith(Sink.ignore)
        Future.successful("")
    }.map(
      parse(_)
    ).filter({
      case obj: JObject => (obj \ "id").toInt == clientId
      case _ => false
    }).map(
      (obj) => obj \ "data"
    ).map(Connection.serialize)

    // attach the client to the server
    Flow.fromSinkAndSourceMat(this.sink, clientSource) {
      (_, clientSink) => {
        val client: Client = new Client(clientId, clientSink)
        this.clients.put(clientId, client)

        // attach the server to the client
        this.source.via(filterFlow).to(clientSink).run()

        // let the server know the client exists
        this.sendMessage(NewClient(clientId))
      }
    }
  }
}
