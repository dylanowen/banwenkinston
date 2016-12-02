class ScreenClient {
  constructor(server) {
    this.socket = new WebSocket(getWebSocketBase() + 'ws?type=client&serverId=server_1');
  }

  connect(user) {
    this.user = user;

    let packet = new Packet({type: Packet.FROM_CLIENT, client: user, message: new Message("connect")});
    this.socket.send(JSON.stringify(packet));
  }

  disconnect() {

  }

  send(msg) {
    let packet = new Packet({type: Packet.FROM_CLIENT, client: this.user, message: msg});
    this.socket.send(JSON.stringify(packet));
  }
}
