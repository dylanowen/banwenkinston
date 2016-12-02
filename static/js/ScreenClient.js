class ScreenClient {
  constructor(serverId, userId) {
    this.server = serverId;
    this.user = userId;
  }

  connect() {
    this.socket = new WebSocket(getWebSocketBase() + 'ws?type=client&serverId=' + serverId);
    this.socket.onopen = () => {
      let packet = new Packet({type: Packet.FROM_CLIENT, client: this.user, message: new Message("connect")});
      this.socket.send(JSON.stringify(packet));
    };
  }

  disconnect() {

  }

  send(msg) {
    let packet = new Packet({type: Packet.FROM_CLIENT, client: this.user, message: msg});
    this.socket.send(JSON.stringify(packet));
  }
}
