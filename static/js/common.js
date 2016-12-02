function getWebSocketBase() {
    const loc = window.location;
    const path = loc.pathname.substring(0, loc.pathname.lastIndexOf('/'))
    let protocol;
    if (loc.protocol === "https:") {
        protocol = "wss:";
    } else {
        protocol = "ws:";
    }
    return protocol + "//" + loc.host + "/";
}

class Message {
  constructor(type, user) {
    this.type = type;
    this.user = user;
  }
}

var Direction = new Set(["up, down, left, right"])

class InputMessage extends Message {
  constructor(user, direction) {
    super("input", user);
    this.direction = direction;
  }
}


/**
Valid packet types when sending to ScreenServer:
  fromClient (requires client)
  ping
  heartbeat

Valid packet types when sending from ScreenServer:
  heartbeat
  toClient (if client is null, will broadcast)
  stop
*/
class Packet {
  constructor({type, client, message}) {
    this._type = type;
    this.client = client;
    this.message = message;
  }
}

Packet.PING = "ping";
Packet.HEARTBEAT = "heartbeat";
Packet.STOP = "stop";
Packet.FROM_CLIENT = "fromClient";
Packet.TO_CLIENT = "toClient";
