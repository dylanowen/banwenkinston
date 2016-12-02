const DELAY = 10;

class SnakeQueue{

  constructor(){
    this.players = new Array();
    this.delay = 0 ;
  }

  addPlayer(client){
    this.players.push(client);
    if(this.delay == 0){
      this.delay = DELAY;
    }
  }

  checkQueue(){
    if(this.delay <= 0){
      if(this.players.length > 0){
        if(uiIds.length != 0 ){
          let player = this.players.pop();
          Snake.addNewSnake(player);
          this.delay= DELAY;
        }
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
