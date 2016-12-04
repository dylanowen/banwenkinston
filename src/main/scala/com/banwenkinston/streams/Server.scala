package com.banwenkinston.streams

import java.util.concurrent.atomic.AtomicInteger

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.ws.Message
import akka.stream.Materializer
import akka.stream.scaladsl.Flow

import scala.collection.concurrent.TrieMap
import scala.language.higherKinds

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Dec-2016
  */
abstract class Server(val id: String)(implicit val materializer: Materializer, val log: LoggingAdapter) {
  protected val clients: TrieMap[Int, Client] = new TrieMap[Int, Client]()
  private val clientIdOffset: AtomicInteger = new AtomicInteger(1)

  def registerClient(): Flow[Message, Message, Any]

  protected def getNextClientId: Int = clientIdOffset.getAndIncrement()
}
