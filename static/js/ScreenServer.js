function getWebSocketBase() {
    const loc = window.location;
    const path = loc.pathname.substring(0, loc.pathname.lastIndexOf('/'))
    let protocol;
    if (loc.protocol === "https:") {
        protocol = "wss:";
    } else {
        protocol = "ws:";
    }
    return protocol + "//" + loc.host + path + "/";
}

let _socket = Symbol("socket");
let _clients = Symbol("clients");
let _heartbeatsMissed = Symbol("heartbeatsMissed");
let _heartbeatTimeoutId = Symbol("heartbeatTimeoutId");
let _shutdown = Symbol("shutdown");
let _afterShutdown = Symbol("afterShutdown");
class ScreenServer {
  constructor() {
    this[_socket] = null;
    this[_clients] = new Set();
    this[_heartbeatsMissed] = 0;
    this[_heartbeatTimeoutId] = null;
    this.heartbeatInterval = 5000;
    this.heartbeatTolerance = 3;
  }

  start() {
    if (!this[_socket]) {
      this[_socket] = new WebSocket(getWebSocketBase() + 'ws?type=server');

      // Start listening for events
      this[_socket].onmessage = (event) => {
        let packet = event.data;
        if (packet.type == "from_client") {
          let client = packet.client;
          var message = packet.message;
          if (message.type == "connect") {
            this.clients.add(client);
            this.onClientConnect(client);
          } else if (message.type == "disconnect") {
            this.onClientDisconnect(client);
            this.clients.remove(client);
          } else if (message.type == "input") {
            this.onInput(client, message);
          } else {
            console.error("Invalid message received:");
            console.error(message);
          }
        } else if (packet.type == "ping") {
          console.log("Received ping:");
          console.log(packet.message);
          this[_socket].send(packet);
        } else if (packet.type == "heartbeat") {
          console.log("Received heartbeat");
          this[_heartbeatsMissed] = 0;
        } else {
          console.error("Invalid packet received:");
          console.error(packet);
        }
      }

      this[_socket].onclose = (event) => {
        console.error("Connection closed: " + event.code + " -> " + event.reason);
        this[_afterShutdown]();
      }

      let heartbeatFunction = () => {
        this[_heartbeatsMissed]++;
        if (this[_heartbeatsMissed] > this.heartbeatTolerance || this[_socket] == null) {
          console.error(this.heartbeatTolerance + " heartbeats missed, stopping server");
          this[_shutdown]();
        } else {
          console.log("Sending heartbeat");
          let heartbeatMessage = this.heartbeat();
          this[_socket].send(new Packet({type: "heartbeat", message: heartbeatMessage}));
          this[_heartbeatTimeoutId] = setTimeout(this.heartbeatInterval, heartbeatFunction);
        }
      }

      // Start heartbeat
      this[_heartbeatTimeoutId] = setTimeout(heartbeatFunction, this.heartbeatInterval);
    } else {
      console.error("Attempted to start ScreenServer that is not stopped");
    }
  }

  [_shutdown]() {
    console.log("ScreenServer shutting down");
    this[_socket].close();
    this[_afterShutdown]();
    this.onShutdown();
  }

  [_afterShutdown]() {
    if (this[_socket] != null) {
      this[_socket] = null;
      if (this[_heartbeatTimeoutId]) {
        clearTimeout(this[_heartbeatTimeoutId]);
      }
    }
  }

  stop() {
    if (this[_socket] != null) {
      console.log("Stopping ScreenServer");

      // TODO: notify router & clients that screen has stopped

      this[_shutdown]();
    }
  }

  // Sends a message to a specific client
  sendMessage(client, message) {
    this[_socket].send({client, message});
  }


  /****** VIRTUAL METHODS ******/

  // Implement to handle client connections (should not be called manually)
  onClientConnect(client) {
  }

  // Implement to handle client disconnections (should not be called manually)
  onClientDisconnect(client) {
  }

  // Implement to handle client input (should not be called manually)
  onInput(client, input) {
  }

  onTick() {
  }

  // Implement to handle remote server shutdown (should not be called manually)
  onShutdown() {
  }

  /* Should return some information about the ScreenServer, by default returns true to indicate that
    the server is still active */
  heartbeat() {
    return true;
  }
}
