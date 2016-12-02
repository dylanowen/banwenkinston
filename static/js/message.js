class Message {
  constructor(type) {
    this.type = type;
  }
}

var Direction = new Set(["up, down, left, right"])

class InputMessage extends Message {
  constructor(direction) {
    super("input");
    this.direction = direction;
  }
}
