let uiIds = new Array();
let colors = ["red", "blue", "green", "yellow", "orange", "white", "purple"];
class Snake {
  constructor( x, y, id){
    console.log("New snake at: " + x + ", " + y);
    this.pixels = [];
    this.type = id;
    this.id = uiIds.pop();
    this.color = colors.pop();
    this.length = 1;
    this.grow();
    this.pixels.push(new Pixel(x, y, this.id));
    /*for (var i = this.length-1; i >= 0; i--){
      this.pixels.push(new Pixel(x + i, y, this.id));
    }*/
    //this.delay = 0;
    this.direction = DIRECTION_MAPPING["right"];
  }

  moveSnake() {
    var oldSegment = this.pixels[0];
    // JS does not know how to modulo negative numbers so prevent this by adding gameWidth or gameHeight
    var newX = (snakeCanvas.gameWidth() + oldSegment.x + this.direction.dx) %  snakeCanvas.gameWidth();
    var newY = (snakeCanvas.gameHeight() + oldSegment.y + this.direction.dy) % snakeCanvas.gameHeight();
    console.log("SnakePos: " + newX + ", "+ newY)

    var newSegment = new Pixel(newX, newY, oldSegment.type);

    var col = snakeCanvas.detectCollision(newSegment);

    if(col){
      if(col == "apple"){
        this.grow();
      }else{
        snakeCanvas.removeSnake(this);
        this.releaseID();
        queue.addPlayer(this.type);
        return;
      }
    }

    this.pixels.unshift(newSegment);
    snakeCanvas.addPixel(newSegment);
    if(this.delay > 0){
      this.delay = this.delay - 1;
    }
    else{
      snakeCanvas.removePixel(this.pixels.pop());
    }

    this.draw();
  }

  grow(){
    this.delay = snakeCanvas.snakeSizeIncrease;
    this.length = this.length + snakeCanvas.snakeSizeIncrease;
  }

  changeDirection(direction){
    if(direction.opp != this.direction.name){
      this.direction = direction;
    }
  }

  draw(){
      snakeCanvas.draw([this.pixels[0]]);
      for(var i = 1; i < this.pixels.length; i++){
        snakeCanvas.drawPixel(this.color, this.pixels[i]);
      }
  }

  releaseID(){
    uiIds.push(this.id);
    colors.push(this.color);
  }

  static addNewSnake(client){
    let location = snakeCanvas.randomEmptyLocation();
    snakeCanvas.addSnake(new Snake(location.x, location.y, client));
  }
}
