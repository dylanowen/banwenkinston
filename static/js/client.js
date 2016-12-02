var buttonClickEventNames = ["mousedown", "touchstart"];

initializeAllButtonListeners();

// initialize button listeners for click and click-like events
function initializeAllButtonListeners() {
  var buttons = document.getElementsByClassName("buttondiv");
  for (button of buttons) {
    for (eventName of buttonClickEventNames) {
      registerButtonListener(button);
    }
  }
}

// register a listener that sends the button press to the web socket
function registerButtonListener(button) {
  button.addEventListener(eventName, (event) => {
    sendThingToTheSocketThing(button.id);
  });
}

function sendThingToTheSocketThing(string) {
  console.log(string);
}