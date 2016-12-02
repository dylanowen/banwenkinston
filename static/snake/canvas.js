var snakeCanvas = {
  canvasWidth: 800,
  canvasHeight: 600,
  pixelSize: 40,
  snakeSizeIncrease: 3,
  KEY_MAPPING: {
    39: "right",
    40: "down",
    37: "left",
    38: "up"
  },
  started: true,
  grid: {},
  apples: new Map(),
  snakes: new Map(),

  initGrid: function() {

  },

  gameHeight: function() {
    return this.canvasHeight / this.pixelSize;
  },

  gameWidth: function() {
    return this.canvasWidth / this.pixelSize;
  },

  canvas: function() {
    if (snakeCanvas.context) { return snakeCanvas.context; }
    var canvas = document.getElementById("snake-game");
    snakeCanvas.context = canvas.getContext("2d");
    return snakeCanvas.context;
  },

  executeNTimesPerSecond: function(tickCallback, gameSpeed) {
    tickCallback();
    snakeCanvas.processID = setInterval(function() {
      tickCallback();
    }, 1000 / gameSpeed);
  },

  onArrowKey: function(callback) {
    document.addEventListener('keydown', function(e) {
      if (snakeCanvas.KEY_MAPPING[e.which]) {
        e.preventDefault();
        callback(snakeCanvas.KEY_MAPPING[e.which]);
      }
    });
  },

  endGame: function() {
    this.started = false
    clearInterval(snakeCanvas.processID);
  },

  draw: function(objects) {
    if (this.started) {
     snakeCanvas.drawObjects(objects);
    }
  },

  clear: function() {
    snakeCanvas.canvas().clearRect(0, 0, snakeCanvas.canvasWidth, snakeCanvas.canvasHeight);
  },

  drawObjects: function(objects) {
    var ui = this;
    objects.forEach(function(pixel) {
    	var translatedPixel = snakeCanvas.translatePixel(pixel);
      ui.canvas().drawImage(images[pixel.type], translatedPixel.x, translatedPixel.y);
    });
  },

  drawPixel: function(color, pixel) {
    snakeCanvas.canvas().fillStyle = color;
    var translatedPixel = snakeCanvas.translatePixel(pixel);
    snakeCanvas.context.fillRect(translatedPixel.x, translatedPixel.y, snakeCanvas.pixelSize, snakeCanvas.pixelSize);
  },

  translatePixel: function(pixel) {
    return { x: pixel.x * snakeCanvas.pixelSize,
             y: pixel.y * snakeCanvas.pixelSize }
  },

  detectCollisionBetween: function(objectA, objectB) {
    return objectA.some(function(pixelA) {
      return objectB.some(function(pixelB) {
        return pixelB.y === pixelA.y && pixelB.x === pixelA.x;
      });
    });
  },

  randomLocation: function() {
    return {
      y: Math.floor(Math.random()*snakeCanvas.gameHeight()),
      x: Math.floor(Math.random()*snakeCanvas.gameWidth()),
    }
  },

  randomEmptyLocation: function() {
    var locationfound = false;
    while(!locationfound){
      var location = this.randomLocation();
      if(!this.grid.hasOwnProperty(location.x) || !this.grid[location.x][location.y]){
        return location;
      }
    }
  },

  flashMessage: function(message) {
    var canvas = document.getElementById("snake-game");
    var context = canvas.getContext('2d');
    context.font = '20pt Calibri';
    context.fillStyle = 'yellow';
    context.fillText(message, 275, 100);
  },

  addApple: function(){
    let location = this.randomEmptyLocation();
    var apple = new Apple(location.x, location.y);
    this.addPixel(apple.pixel);
    this.apples.set(Apple.getID(apple.pixel), apple);
  },

  drawApples: function(){
    this.apples.forEach( function(value, key){
      value.draw();
    });
  },

  addSnake: function(snake){
    this.snakes.set(snake.type, snake);
    if((this.snakes.keys.length / 2) > this.apples.keys.length){
      this.addApple();
    }
  },

  removeSnake: function(oldSnake){
    this.snakes.delete(oldSnake.type);
    oldSnake.pixels.forEach(function(pixel){
      snakeCanvas.removePixel(pixel);
    });
  },

  addPixel: function(pixel){
  	if(!this.grid.hasOwnProperty(pixel.x)){
  		this.grid[pixel.x] = {};
  	}

 		this.grid[pixel.x][pixel.y] = pixel.type;
  },

  removePixel: function(pixel) {
  	if(this.grid.hasOwnProperty(pixel.x)){
  		delete this.grid[pixel.x][pixel.y];
  	}
  },

  detectCollision: function(pixel){
    if(this.grid.hasOwnProperty(pixel.x)){
      if(this.grid[pixel.x][pixel.y] == "apple"){
        this.eatApple(pixel);
      }
      return this.grid[pixel.x][pixel.y];
    }

    return NaN;
  },

  eatApple(pixel){
    this.apples.delete(Apple.getID(pixel));
    if((this.snakes.keys.length / 2) >= this.apples.keys.length ||
        this.apples.keys.length == 0) {
        this.addApple();
      }
  }
}
