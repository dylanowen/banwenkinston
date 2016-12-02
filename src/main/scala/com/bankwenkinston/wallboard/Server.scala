package com.bankwenkinston.wallboard

import java.util.concurrent.atomic.AtomicInteger

import akka.NotUsed
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Keep, MergeHub, Sink, Source, _}
import com.bankwenkinston.wallboard.Connection.Welcome
import com.bankwenkinston.wallboard.JsonUtils._
import org.json4s._
import org.json4s.native.JsonMethods._

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future
import scala.language.higherKinds

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
    val serverSource: Source[Message, Sink[Message, NotUsed]] = MergeHub.source[Message]
    val serverSink: Sink[Message, Source[Message, NotUsed]] = BroadcastHub.sink[Message]

    Connection.debugFlow.via(Flow.fromSinkAndSourceMat(serverSink, serverSource) {
      (source, sink) => {
        val id: String = genId
        val server: Server = new Server(id, sink, source)
        servers.put(id, server)

        //val source: Source[Message, Sink[Message, NotUsed]] = MergeHub.source[Message]
        println("Connected to Server: " + id)
      }
    })

      /*
    val (serverSink, serverSource) = MergeHub.source[Message].toMat(BroadcastHub.sink[Message])(Keep.both).run()



    Flow.fromSinkAndSource(serverSink, serverSource)
    */
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
      case _: JObject => true
      case _ => false
    }).filter((obj) => {
      (obj \ "id").toOption.isDefined
    }).map(
      (obj) => obj \ "data"
    ).map(Connection.serialize)

    // attach the client to the server
    Connection.debugFlow.via(Flow.fromSinkAndSourceMat(this.sink, clientSource) {
      (_, clientSink) => {
        val client: Client = new Client(clientId, clientSink)
        this.clients.put(clientId, client)

        // attach the server to the client
        this.source.via(filterFlow).to(clientSink).run()

        // let the server know the client exists
        this.sendMessage(Welcome("welcome client", clientId))

        println("Connected to Client: " + clientId)
      }
    })
  }
}
