const buttonClickEventNames = ["mousedown", "touchstart"];
const eventMap = new Map();

// get the server and user IDs from the query parameters
// TODO: I'm sure they will actually be there some day.
const serverId = getUrlParameter("server");
const userId = getUrlParameter("user");
// set up the screen client and open the websocket
const screenClient = new ScreenClient(serverId, userId);
screenClient.connect();
// prepare the key/click mappings and listeners tied to the websocket
initializeEventMap();
initializeAllListeners();

// TODO: what is the server ID? what about client ID?
//const socket = new WebSocket(getWebSocketBase() + 'ws?type=client&serverId=server_1');

function initializeEventMap() {
  eventMap.set(37, "left");
  eventMap.set(65, "left");
  eventMap.set("buttonLeft", "left");

  eventMap.set(38, "up");
  eventMap.set(87, "up");
  eventMap.set("buttonUp", "up");

  eventMap.set(39, "right");
  eventMap.set(68, "right");
  eventMap.set("buttonRight", "right");

  eventMap.set(40, "down");
  eventMap.set(83, "down");
  eventMap.set("buttonDown", "down");
}

// initialize button listeners for click, click-like, and keypress events
function initializeAllListeners() {
  // listen to arrow keys and wasd
  document.addEventListener("keydown", (event) => {
      //if (arrowKeycodes.includes(event.keyCode)) {
      if (eventMap.has(event.keyCode)) {
        event.preventDefault();
        console.log("key:" + event.keyCode);
        sendThingToTheSocketThing(eventMap.get(event.keyCode));
      }
  });

  // listen to clicks and touches
  const buttons = document.getElementsByClassName("buttondiv");
  for (let button of buttons) {
    for (eventName of buttonClickEventNames) {
      button.addEventListener(eventName, (event) => {
          console.log("button:" + button.id);
          sendThingToTheSocketThing(eventMap.get(button.id));
      });
    }
  }
}

function sendThingToTheSocketThing(direction) {
  console.log("sending message:" + direction);
  let inputMessage = new InputMessage(direction);
  screenClient.send(inputMessage);
}

function getUrlParameter(name) {
  name = name.replace(/[\[]/, '\\[').replace(/[\]]/, '\\]');
  var regex = new RegExp('[\\?&]' + name + '=([^&#]*)');
  var results = regex.exec(location.search);
  return results === null ? '' : decodeURIComponent(results[1].replace(/\+/g, ' '));
};

//// TODO: use the method in common.js
//function getWebSocketBase() {
//    const loc = window.location;
//    const path = loc.pathname.substring(0, loc.pathname.lastIndexOf('/'))
//    let protocol;
//    if (loc.protocol === "https:") {
//        protocol = "wss:";
//    } else {
//        protocol = "ws:";
//    }
//    return protocol + "//" + loc.host + "/";
//}