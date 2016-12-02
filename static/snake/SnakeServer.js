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
    console.log(input)
    let snake = snakeCanvas.snakes.get(client);
    snake.changeDirection(DIRECTION_MAPPING[input.direction]);
  }

  onWelcome(serverId) { 
    const header = document.getElementById('serverId')
    const gameHeader = document.createElement('a');
    gameHeader.href = '../client.html?server=' + serverId + '&user=dyl'
    gameHeader.textContent = serverId
    gameHeader.target = '_blank'
    header.textContent = '';
    header.appendChild(gameHeader)
  }
}
