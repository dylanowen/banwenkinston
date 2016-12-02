class SnakeQueue{

  constructor(){
    this.players = new Array();
    this.delay = 0 ;
  }

  addPlayer(client){
    this.players.push(client);
    if(this.delay == 0){
      this.delay = 25;
    }
  }

  checkQueue(){
    if(this.delay <= 0){
      if(this.players.length > 0){
        let player = this.players.pop();
        Snake.addNewSnake(player);
        this.delay= 25;
      }
    } else if(this.delay > 0) {
      this.delay--;
    }
  }

  removePlayer(client){
    let index = this.players.getIndexOf(client);
    if(index && index >= 0){
      this.players.splice(index,1);
    }
  }
}
