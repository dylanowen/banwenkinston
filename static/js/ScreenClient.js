class ScreenClient {
  constructor(serverId, userId) {
    this.server = serverId;
    this.user = userId;
  }

  connect() {
    console.log("Initializing ScreenClient");
    this.socket = new WebSocket(getWebSocketBase() + 'ws?type=client&serverId=' + serverId);
    this.socket.onopen = () => {
      //let packet = new Packet({type: Packet.FROM_CLIENT, client: this.user, message: new Message("connect")});
      //this.socket.send(JSON.stringify(packet));
      console.log("Connecting to server");
      this.socket.send(JSON.stringify(new Message("connect", this.user)));
    };
  }

  disconnect() {

  }

  send(msg) {
    //let packet = new Packet({type: Packet.FROM_CLIENT, client: this.user, message: msg});
    //this.socket.send(JSON.stringify(packet));
    console.log("Sending message: " + msg);
    msg.user = this.user
    this.socket.send(JSON.stringify(msg));
  }
}
