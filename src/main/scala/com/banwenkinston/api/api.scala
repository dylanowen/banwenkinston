package com.banwenkinston

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Dec-2016
  */
package api {
  object ws {
    // enforce _type type on all packets
    trait Packet {
      def _type: String
    }

    case class Welcome(message: String, id: AnyVal, _type: String = "welcome") extends Packet

    // Router to Server
    case class ClientWrapper(id: Int, message: AnyRef, _type: String = "fromClient") extends Packet

    // Server to Router
    case class ClientConnected(id: AnyVal, _type: String = "clientConnect") extends Packet

    // Router to Client

    // Server to Client
  }
  object web {

  }
}

