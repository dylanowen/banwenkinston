package com.banwenkinston.streams

import java.util.concurrent.{ConcurrentHashMap, ConcurrentMap}

import akka.NotUsed
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.ws.Message
import akka.stream.{FlowShape, Graph, Materializer}
import akka.stream.scaladsl.{BroadcastHub, Flow, MergeHub, Sink, Source}
import com.banwenkinston.utils.UniqueIds
import FlowUtils._

import scala.collection.concurrent.TrieMap
import scala.collection.immutable.HashMap
import scala.collection.mutable

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Dec-2016
  */
class GameRouter(implicit materializer: Materializer, log: LoggingAdapter) {
  // java concurrent map gives us a stricter enforcement of atomicity over TrieMap
  private val servers: ConcurrentMap[String, Server] = new ConcurrentHashMap[String, Server]()

  /**
    * creates a flow for a server web socket connection
    */
  def createServerFlow: Flow[Message, Message, Any] = {
    val serverSource: Source[Message, Sink[Message, NotUsed]] = MergeHub.source[Message]
    val serverSink: Sink[Message, Source[Message, NotUsed]] = BroadcastHub.sink[Message]

    Flow.fromSinkAndSourceMat(serverSink, serverSource)(registerUniqueServer).debug
  }

  def createClientFlow(server: Server): Flow[Message, Message, Any] = {
    server.registerClient()
  }

  def getServer(id: String): Option[Server] = Option(servers.get(id))

  //def deleteServer(id: String): Unit = this.servers.remove(id)

  private def registerUniqueServer(source: Source[Message, NotUsed], sink: Sink[Message, NotUsed]): Server = {
    var server: Server = null
    var foundUniqueId: Boolean = false
    // loop trying to find a unique id, once we find one hold our spot with a placeholder
    do {
      val uniqueId: String = UniqueIds.get
      server = this.servers.computeIfAbsent(uniqueId, (_) => {
        foundUniqueId = true
        log.info("Server: " + uniqueId + " connected")

        new WallboardServer(uniqueId, sink, source)
      })
    } while(!foundUniqueId)

    server
  }
}
