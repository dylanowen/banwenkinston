/**
Valid packet types when sending to ScreenServer:
  fromClient (requires client)
  ping
  heartbeat

Valid packet times when sending from ScreenServer:
  heartbeat
  toClient (if client is null, will broadcast)
  stop


*/

class Packet {
  constructor({type, client, message}) {
    this.type = type;
    this.client = client;
    this.message = message;
  }
}

Packet.PING = "ping";
Packet.HEARTBEAT = "heartbeat";
Packet.STOP = "stop";
Packet.FROM_CLIENT = "fromClient";
Packet.TO_CLIENT = "toClient";
