const buttonClickEventNames = ["mousedown", "touchstart"];
const arrowKeycodes = [37, 38, 39, 40]

initializeAllListeners();

// initialize button listeners for click, click-like, and keypress events
function initializeAllListeners() {
  document.addEventListener("keydown", (event) => {
      event.preventDefault();
      if (arrowKeycodes.includes(event.keyCode)) {
        sendThingToTheSocketThing("key:" + event.keyCode);
      }
  });

  const buttons = document.getElementsByClassName("buttondiv");
  for (button of buttons) {
    for (eventName of buttonClickEventNames) {
      button.addEventListener(eventName, (event) => {
          sendThingToTheSocketThing(button.id);
      });
    }
  }
}

function sendThingToTheSocketThing(buttonName) {
  console.log(buttonName);
}

// TODO: use the method in common.js
function getWebSocketBase() {
    const loc = window.location;
    const path = loc.pathname.substring(0, loc.pathname.lastIndexOf('/'))
    let protocol;
    if (loc.protocol === "https:") {
        protocol = "wss:";
    } else {
        protocol = "ws:";
    }
    return protocol + "//" + loc.host + path + "/";
}