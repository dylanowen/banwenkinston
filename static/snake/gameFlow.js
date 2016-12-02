var DIRECTION_MAPPING = {
	"right": {dx:1, dy:0, opp:"left", name: "right"},
	"left": {dx:-1, dy:0, opp:"right", name: "left"},
	"up": {dx:0, dy:-1, opp:"down", name: "up"},
	"down": {dx:0, dy:1, opp:"up", name: "down"}
}

let queue = new SnakeQueue();
let server = new SnakeServer();

server.heartbeatTolerance = 1000;

var images = new Object();

var init = function(){

  //right
  var goblin = document.createElement("IMG");
  goblin.src = "images/goblin.png";
  //up
  var goblinu = document.createElement("IMG");
  goblinu.src = "images/goblin-up.png";
  //down
  var goblind = document.createElement("IMG");
  goblind.src = "images/goblin-down.png";
  //left
  var goblinl = document.createElement("IMG");
  goblinl.src = "images/goblin-left.png";

  var guac = document.createElement("IMG");
  guac.src = "images/guac.png";

  images["right"] = goblinl;
  images["left"] = goblin;
  images["down"] = goblinu;
  images["up"] = goblind;
  images["apple"] = guac;

	uiIds = images.keys();
  var imagesLoaded = 0;

  var waitforload = function(images){
    imagesLoaded++;
    if(imagesLoaded == Object.keys(images).length){
      snakeCanvas.executeNTimesPerSecond(advanceGame, 5);
			server.start();

    }
  }

  Object.keys(images).forEach(function(image){
    if(!image.complete){
      image.onload = waitforload(images);
    } else {
      imagesLoaded++;
    }
  });
}

var advanceGame = function() {
  snakeCanvas.clear();
  snakeCanvas.snakes.forEach(function(value, key) {
    value.moveSnake();
  });
  snakeCanvas.drawApples();
	queue.checkQueue();
}

var react = function(direction){
	snakeCanvas.snakes.forEach(function(snake, key) {
    snake.changeDirection(direction);
  });
}


document.addEventListener('keydown', function(e) {
      if (snakeCanvas.KEY_MAPPING[e.which]) {
        e.preventDefault();
        react(DIRECTION_MAPPING[snakeCanvas.KEY_MAPPING[e.which]]);
      }
    });

snakeCanvas.addApple();
var delay = 0;
