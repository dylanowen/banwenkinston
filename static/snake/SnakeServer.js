class SnakeServer extends ScreenServer{

  constructor() {
    super();
  }

  onClientConnect(client) {
    queue.addPlayer(client);
  }

  onClientDisconnect(client) {
    queue.removePlayer(client);
    let snake = snakeCanvas.snakes.get(client);
    snakeCanvas.removeSnake(snake);
    snake.releaseID();
  }

  onInput(client, input) {
    let snake = snakeCanvas.snakes.get(client);
    snake.changeDirection(DIRECTION_MAPPING[input.direction]);
  }
}
