class Pixel {
  constructor(x, y, type){
    this.x = x;
    this.y = y;
    this.type = type;
  }
}

class Apple{
  constructor(x, y){
  	this.pixel = new Pixel(x, y, "apple");
  }
	
  draw() {
	snakeCanvas.draw([this.pixel]);
  }

  static getID(pixel){
  	return "X" + pixel.x + "Y" + pixel.y;
  }
}