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
  var j1 = document.createElement("IMG");
  goblin.src = "images/j1.png";
  //up
  var j2 = document.createElement("IMG");
  goblinu.src = "images/j2.png";
  //down
  var j3 = document.createElement("IMG");
  goblind.src = "images/j3.png";
  //left
  var j4 = document.createElement("IMG");
  goblinl.src = "images/j4.png";

	var j5 = document.createElement("IMG");
  goblinl.src = "images/j5.png";

  var guac = document.createElement("IMG");
  guac.src = "images/guac.png";

  images["j1"] = j1;
  images["j2"] = j2;
  images["j3"] = j3;
  images["j4"] = j4;
	images["j5"] = j5;

	uiIds = Object.keys(images);

	images["apple"] = guac;

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
