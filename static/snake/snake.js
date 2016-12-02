let uiIds = ["up", "down", "right", "left"];
class Snake {
  constructor( x, y, id){
    this.pixels = [];
    this.type = id;
    this.id = uiIds.pop();
    this.length = 5;
    for (var i = this.length-1; i >= 0; i--){
      this.pixels.push(new Pixel(x + i, y, this.id));
    }
    this.delay = 0;
    this.direction = DIRECTION_MAPPING["right"];
  }

  moveSnake() {
    var oldSegment = this.pixels[0];
    var newX = (snakeCanvas.gameWidth() + oldSegment.x + this.direction.dx) %  snakeCanvas.gameWidth();
    var newY = (snakeCanvas.gameHeight() + oldSegment.y + this.direction.dy) % snakeCanvas.gameHeight();

    var newSegment = new Pixel(newX, newY, oldSegment.type);

    var col = snakeCanvas.detectCollision(newSegment);

    if(col){
      if(col == "apple"){
        this.grow();
      }else{
        snakeCanvas.removeSnake(this);
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
      snakeCanvas.draw(this.pixels);
  }

  releaseID(){
    uiIds.push(this.id);
  }

  static addNewSnake(client){
    let location = snakeCanvas.randomEmptyLocation();
    snakeCanvas.addSnake(new Snake(location.x, location.y, client));
  }
}