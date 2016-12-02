const _socket = Symbol("socket");
const _clients = Symbol("clients");
const _heartbeatsMissed = Symbol("heartbeatsMissed");
const _heartbeatTimeoutId = Symbol("heartbeatTimeoutId");
const _shutdown = Symbol("shutdown");
const _afterShutdown = Symbol("afterShutdown");
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
      console.log("Starting screen server at: " + getWebSocketBase() + 'ws?type=server')
      this[_socket] = new WebSocket(getWebSocketBase() + 'ws?type=server');
      this[_clients] = new Set();
      this[_heartbeatsMissed] = 0;
      this[_heartbeatTimeoutId] = null;

      // Start listening for events
      this[_socket].onmessage = (event) => {
        const packet = JSON.parse(event.data);
        if (packet._type == Packet.FROM_CLIENT) {
          const clientId = packet.client;
          const message = packet.message;
          const client = message.user;
          if (message.type == "connect") {
            console.log("Client connecting: " + client);
            this[_clients].add(client);
            this.onClientConnect(client);
          } else if (message.type == "disconnect") {
            if (this[_clients].has(client)) {
              console.log("Client disconnecting: " + client);
              this.onClientDisconnect(client);
              this[_clients].remove(client);
            } else {
              console.error("Can not disconnect {" + client + "}, not currently connected");
            }
          } else {
            if (this[_clients].has(client)) {
              this.onInput(client, message);
            } else {
              console.error("Client {" + client + "} attempted to send input but has not connected");
            }
          }
        } else if (packet._type == Packet.PING) {
          console.log("Received ping:");
          console.log(packet.message);
          this[_socket].send(JSON.stringify(packet));
        } else if (packet._type == Packet.HEARTBEAT) {
          console.log("Received heartbeat");
          this[_heartbeatsMissed] = 0;
        } else if (packet.id){
          console.log("Connected, server id: " + packet.id);
        } else {
          console.error("Invalid packet received:");
          console.error(packet);
        }
      }

      this[_socket].onclose = (event) => {
        console.error("Connection closed: " + event.code + " -> " + event.reason);
        this[_afterShutdown]();
      }

      const heartbeatFunction = () => {
        this[_heartbeatsMissed]++;
        if (this[_heartbeatsMissed] > this.heartbeatTolerance || this[_socket] == null) {
          console.error(this.heartbeatTolerance + " heartbeats missed, stopping server");
          this[_shutdown]();
        } else {
          console.log("Sending heartbeat");
          const heartbeatMessage = this.heartbeat();
          this[_socket].send(JSON.stringify(new Packet({type: Packet.HEARTBEAT, message: heartbeatMessage})));
          this[_heartbeatTimeoutId] = setTimeout(heartbeatFunction, this.heartbeatInterval);
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

      // TODO: this will eventually notify clients it has sent
      this[_socket].send(new Packet({type: Packet.STOP}))

      this[_shutdown]();
    }
  }

  // Sends a message to a specific client
  sendMessage(client, message) {
    this[_socket].send(new Packet({type: Packet.TO_CLIENT, client, message}));
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

  // Implement to handle remote server shutdown (should not be called manually)
  onShutdown() {
  }

  // Should return some information about the ScreenServer, default returns true to indicate an active server
  heartbeat() {
    return true;
  }
}
