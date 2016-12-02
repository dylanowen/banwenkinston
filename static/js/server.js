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

//const messages = document.getElementById('messages');
const socket = new WebSocket(getWebSocketBase() + 'ws?type=client&serverId=2');

let i = 0;
socket.onopen = function (event) {
    socket.send("" + i++);
};
socket.onmessage = function (event) {
    //const received = document.createElement('div');
    //received.textContent = event.data;
    //messages.appendChild(received);

    if (i < 10000000) {
        socket.send("" + i++);
    }
}


class ScreenServer {
  constructor() {
    this.socket = null;
    this.clients = new Set();
    this.heartbeatInterval = 5000;
    this.heartbeatsMissed = 0;
    this.heartbeatTolerance = 3;
    this.heartbeatTimeoutId = null;
  }

  start() {
    this.socket = new WebSocket(getWebSocketBase() + 'ws?type=server')

    // Start listening for events
    this.socket.onmessage = (event) => {
      let packet = event.data;
      if (packet.type == "from_client") {
        let client = packet.client
        var message = packet.message
        if (message.type == "connect") {
          this.clients.add(client);
          this.onClientConnect(client);
        } else if (message.type == "disconnect") {
          this.onClientDisconnect(client);
          this.clients.remove(client);
        } else if (message.type == "input") {
          this.onInput(client, message);
        } else {
          console.err("Invalid message received:")
          console.err(message);
        }
      } else if (packet.type == "ping") {
        console.log("Received ping: ")
        console.log(packet.message);
        this.socket.sendMessage(packet)
      } else if (packet.type == "heartbeat") {
        console.log("Received heartbeat")

      } else {
        console.err("Invalid packet received:");
        console.err(packet)
      }
    }

    let heartbeatFunction = () => {
      heartbeatsMissed++;
      if (heartbeatsMissed > this.heartbeatTolerance || this.socket == null) {
        this.onShutdown()
        this.stop()
      } else {
        let heartbeatMessage = sendHeartbeat();
        this.socket.sendMessage(new Packet({type: "heartbeat", message: heartbeatMessage}));
        this.heartbeatTimeoutId = setTimeout(this.heartbeatInterval, heartbeatFunction);
      }
    }

    // Start heartbeat
    this.heartbeatTimeoutId = setTimeout(this.heartbeatInterval,)
  }

  stop() {
    // TODO: notify router & clients that screen has stopped
    this.socket.close();
    this.socket = null
    if (this.heartbeatTimeoutId) {
      clearTimeout(this.heartbeatTimeoutId)
    }
  }

  // Sends a message to a specific client
  sendMessage(clientId, message) {
    this.socket.send({clientId, message});
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
  sendHeartbeat() {
    return true
  }
}
