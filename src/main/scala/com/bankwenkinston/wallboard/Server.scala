package com.bankwenkinston.wallboard

import scala.collection.concurrent.TrieMap

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Dec-2016
  */
object Server {
  private val servers: TrieMap[String, Server] = new TrieMap[String, Server]()

  def get(id: String): Option[Server] = servers.get(id)
}
class Server {

}
