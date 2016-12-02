document.getElementById("keyLeft").addEventListener("click", function(){ document.getElementById("demo").innerHTML="changed"; });



  var airconsole = new AirConsole();

  var dpad = new DPad("dpad-0", {
    relative: true,
    distance: { x: 10, y: 10},
    "directionchange": function(key, pressed) {
      airconsole.message(AirConsole.SCREEN, {
        "dpad0": {
          "directionchange": {
            "key": key,
            "pressed": pressed
          }
        }
      });
    },
    "touchstart": function() {
      airconsole.message(AirConsole.SCREEN, {
        "dpad0": {
          "touch": true
        }
      });
    },
    "touchend": function(had_direction) {
      airconsole.message(AirConsole.SCREEN, {
        "dpad0": {
          "touch": false,
          "had_direction": had_direction
        }
      });
    }
  });
  var dpad1 = new DPad("dpad-1", {
    relative: true,
    distance: { x: 10, y: 10},
    "directionchange": function(key, pressed) {
      airconsole.message(AirConsole.SCREEN, {
        "dpad1": {
          "directionchange": {
            "key": key,
            "pressed": pressed
          }
        }
      });
    },
    "touchstart": function() {
      airconsole.message(AirConsole.SCREEN, {
        "dpad1": {
          "touch": true
        }
      });
    },
    "touchend": function(had_direction) {
      airconsole.message(AirConsole.SCREEN, {
        "dpad1": {
          "touch": false,
          "had_direction": had_direction
        }
      });
    },
    "diagonal": true
  });
  var dpad2 = new DPad("dpad-2", {
    "directionchange": function(key, pressed) {
      airconsole.message(AirConsole.SCREEN, {
        "dpad2": {
          "directionchange": {
            "key": key,
            "pressed": pressed
          }
        }
      });
    },
    "touchstart": function() {
      airconsole.message(AirConsole.SCREEN, {
        "dpad2": {
          "touch": true
        }
      });
    },
    "touchend": function(had_direction) {
      airconsole.message(AirConsole.SCREEN, {
        "dpad2": {
          "touch": false,
          "had_direction": had_direction
        }
      });
    }
  });
  var dpad3 = new DPad("dpad-3", {
    diagonal: true,
    "directionchange": function(key, pressed) {
      airconsole.message(AirConsole.SCREEN, {
        "dpad3": {
          "directionchange": {
            "key": key,
            "pressed": pressed
          }
        }
      });
    },
    "touchstart": function() {
      airconsole.message(AirConsole.SCREEN, {
        "dpad3": {
          "touch": true
        }
      });
    },
    "touchend": function(had_direction) {
      airconsole.message(AirConsole.SCREEN, {
        "dpad3": {
          "touch": false,
          "had_direction": had_direction
        }
      });
    }
  });