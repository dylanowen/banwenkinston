package com.banwenkinston.streams

import java.util.concurrent.atomic.AtomicInteger

import akka.NotUsed
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.ws.Message
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, MergeHub, Sink, Source}
import FlowUtils._
import akka.actor.Status.Success

import scala.collection.concurrent.TrieMap
import scala.language.higherKinds
import scala.util.Failure

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Dec-2016
  */
abstract class Server(val id: String)(implicit val materializer: Materializer, val log: LoggingAdapter) {
  private val clients: TrieMap[Int, Client] = new TrieMap[Int, Client]()
  private val clientIdOffset: AtomicInteger = new AtomicInteger(1)

  def registerClient(): Flow[Message, Message, Any] = {
    val clientId: Int = clientIdOffset.getAndIncrement()
    registerClient(clientId, (client: Client) => {
      this.clients.put(clientId, client)

      log.info("Client: " + this.id + "->" + clientId + " connected")
    }).onComplete((_) => {
      // TODO do some error handling here or something
      clients.remove(clientId)

      log.info("Client: " + clientId + " disconnected")
    })
  }

  protected def registerClient(clientId: Int, callback: (Client) => Unit): Flow[Message, Message, Any]
}
