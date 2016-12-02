const buttonClickEventNames = ["mousedown", "touchstart"];
const arrowKeycodes = [37, 38, 39, 40, 87, 65, 83, 68] // left, up, right, down, w, a, s, d

initializeAllListeners();
// TODO: what is the server ID? what about client ID?
//const socket = new WebSocket(getWebSocketBase() + 'ws?type=client&serverId=server_1');

// initialize button listeners for click, click-like, and keypress events
function initializeAllListeners() {
  // listen to arrow keys and wasd
  document.addEventListener("keydown", (event) => {
      if (arrowKeycodes.includes(event.keyCode)) {
        event.preventDefault();
        sendThingToTheSocketThing("key:" + event.keyCode);
      }
  });

  // listen to clicks and touches
  const buttons = document.getElementsByClassName("buttondiv");
  for (let button of buttons) {
    for (eventName of buttonClickEventNames) {
      button.addEventListener(eventName, (event) => {
          sendThingToTheSocketThing(button.id);
      });
    }
  }
}

function sendThingToTheSocketThing(message) {
  console.log(message);
  // TODO: compose the thing into the proper format and send it
  // socket.send(message);
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
    return protocol + "//" + loc.host + "/";
}