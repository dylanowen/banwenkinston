const players = {}
const board = document.getElementById('gameView')

class PokerServer extends ScreenServer{

  onClientConnect(client) {
    const div = document.createElement('div')
    const header = document.createElement('h2')
    header.textContent = client
    const count = document.createElement('div')
    count.textContent = 0

    div.appendChild(header)
    div.appendChild(count)

    board.appendChild(div)

    players[client] = {
      count: 0,
      countElmnt: count
    }
  }

  onClientDisconnect(client) {
    
  }

  onInput(client, input) {
    const player = players[client]
    player.count++
    player.countElmnt.textContent = player.count
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

function init() {
  const server = new PokerServer()
  server.start()
}