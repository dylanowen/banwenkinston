package com.banwenkinston.streams

import java.util.concurrent.{ConcurrentHashMap, ConcurrentMap}

import akka.NotUsed
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.ws.Message
import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Flow, MergeHub, Sink, Source}
import com.banwenkinston.streams.FlowUtils._
import com.banwenkinston.utils.UniqueIds

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
  def createWallboardServerFlow: Flow[Message, Message, Any] = {
    val serverSource: Source[Message, Sink[Message, NotUsed]] = MergeHub.source[Message].named("fromServerSource")
    val serverSink: Sink[Message, Source[Message, NotUsed]] = BroadcastHub.sink[Message].named("toServerSink")

    Flow.fromSinkAndSourceMat(serverSink, serverSource)((source: Source[Message, NotUsed], sink: Sink[Message, NotUsed]) => {
      registerServer((id: String) => {
        new WallboardServer(id, sink, source)
      })
    }).debug
  }

  def createClientFlow(server: Server): Flow[Message, Message, Any] = {
    server.registerClient()
  }

  def getServer(id: String): Option[Server] = Option(servers.get(id))

  //def deleteServer(id: String): Unit = this.servers.remove(id)

  def registerServer(createServer: (String) => Server): Server = {
    var server: Server = null
    var foundUniqueId: Boolean = false
    // loop trying to find a unique id, once we find one hold our spot with a placeholder
    do {
      val uniqueId: String = UniqueIds.get
      server = this.servers.computeIfAbsent(uniqueId, (_) => {
        foundUniqueId = true
        log.info("Server: " + uniqueId + " connected")

        createServer(uniqueId)
      })
    } while(!foundUniqueId)

    server
  }
}
