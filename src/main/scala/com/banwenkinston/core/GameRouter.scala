package com.banwenkinston.core

import java.util.concurrent.{ConcurrentHashMap, ConcurrentMap}

import akka.Done
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.ws.Message
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import com.banwenkinston.utils.UniqueIds
import resource.ManagedResource
import resource.managed

import scala.util.Try

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Dec-2016
  */
abstract class GameRouter(implicit materializer: Materializer, log: LoggingAdapter) {
  // java concurrent map gives us a stricter enforcement of atomicity over TrieMap
  protected val servers: ConcurrentMap[String, Server] = new ConcurrentHashMap[String, Server]()

  val name: String

  def getServer(id: String): Option[Server] = Option(servers.get(id)).filter(!_.equals(Server.PLACE_HOLDER))

  protected def findUniqueId(): ManagedResource[CloseableId] = {
    var foundUniqueId: Boolean = false
    var uniqueId: String = ""

    do {
      uniqueId = UniqueIds.get
      this.servers.computeIfAbsent(uniqueId, (_) => {
        foundUniqueId = true

        // hold our spot in the concurrent map
        Server.PLACE_HOLDER
      })
    } while(!foundUniqueId)

    managed(new CloseableId(uniqueId))
  }

  // not thread safe?
  class CloseableId(val id: String) extends AutoCloseable {
    override def close(): Unit = {
      // release any still existing placeholder
      servers.remove(id, Server.PLACE_HOLDER)
    }

    override def toString: String = this.id
  }
}
