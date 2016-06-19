'use strict';

/**
 * Service to communicate with the rover via websockets and JSON-RPC.
 */
angular.module("myApp.roverService", ['ngWebSocket', 'ngMaterial'])
    .factory("roverService", function ($websocket, $location, $mdToast, $mdDialog) {

      var wsURL = 'ws://' + $location.host() + ':' + $location.port() + '/rover';
      var ws = $websocket(getWsURL());

      var lastId = 0;
      var responses = [];
      var errorResponses = [];
      var notifications = [];
      var desiredSpeed = 500;
      var turnRate = 300;
      var cameraMoveStep = 20;
      var lastSendMsg;
      var lastErrorResponse;
      var clientId = 0;
      var roverState = {
        isDriverAvailable: true,
        isKillswitchEnabled: false
      };
      var killswitch = {
        enabled: false
      };
      var collisionDetection = {
        frontLeft: false,
        frontRight: false,
        backLeft: false,
        backRight: false
      };
      var snapshotCallback;
      var logEntriesCallback;
      var systemUpTimeCallback;
        var connectedUsers = {
            list: []
        }
      var blockedUsers = {
        list: []
      }
      var myIp = {
        ipAddress: "",
        isBlocked: false
      };
        var clientJs = new ClientJS();


      /**
       * Get URL for websocket connection depending on used protocol (http or https)
       *
       * At the moment websocket connections over https are not provided by the backend,
       * so this function returns an emtpy string and no websocket connection will be established.
       * Trying to connect to the backend via https will crash the webapp.
       *
       * TODO: Provide https websocket in the backand or prevent crash of the webapp
       *
       * @returns {string} websocket url, if protocol is https returning empty string
       */
      function getWsURL() {
        var port = $location.port();
        var url = "";
        if (port == 443) {
          console.log('Can not use websockets with https');
        } else {
          url = wsURL;
        }
        return url;
      }

      function generateMessage(method, params) {
        return {
          "jsonrpc": "2.0",
          "method": method,
          "params": params,
          "id": ++lastId
        };
      }

      function send(method, params) {
        console.log("send JRPC");
        var msg = JSON.stringify(generateMessage(method, params));
        console.log(msg);
        ws.send(msg);
        lastSendMsg = msg;
      }
      /**
       * Send information about the client to the backend
       * so that developer has information about the users
       */
      function sendClientInformation(){
          var fingerprint = clientJs.getFingerprint();
          var userBrowser = clientJs.getBrowser();
          var operatingSystem = clientJs.getOS();
          send("setClientInformation", [clientId, fingerprint.toString(), userBrowser.toString(), operatingSystem.toString()]);
      }

      ws.onError(function (event) {
        console.log('connection Error', event);
      });

      ws.onClose(function (event) {
        console.log('connection closed', event);
      });

      ws.onOpen(function () {
        console.log('connection open to ' + wsURL);
      });

      ws.onMessage(function (message) {
        var msgData = JSON.parse(message.data);
        if (msgData.method && msgData.method === "incomingSnapshot") {
          console.log('new Msg: Image received');
        } else if (msgData.method && msgData.method === "incomingLogEntries") {
          console.log('new Msg: Log entries received');
        } else {
            console.log('new Msg:' + message.data);
        }

        if (msgData.method) {
          handleMethodCall(msgData);
        } else if (msgData.result) {
          handlerResponse(msgData);
        } else if (msgData.error) {
          handleError(msgData);
        }
      });

      /**
       * Handles JSON-RPC method calls
       */
      function handleMethodCall(request) {
        switch (request.method) {
          case 'setClientId':
            setClientId(request.params[0]);
            break;
          case 'incomingNotification':
            incomingNotification(request.params[0]);
            break;
          case 'updateKillswitchEnabled':
            updateKillswitchEnabled(request.params[0]);
            break;
          case 'updateCollisionInformation':
            updateCollisionInformation(request.params);
            break;
          case 'updateConnectedUsers':
              updateConnectedUsers(request.params[0],request.params[1]);
              break;
          case 'incomingSnapshot':
            incomingSnapshot(request.params);
            break;
          case 'showAlertNotification':
            showAlertNotification(request.params[0]);
            break;
          case 'showErrorNotification':
            showErrorNotification(request.params[0]);
            break;
          case 'updateRoverState':
            updateRoverState(request.params[0]);
            break;
          case 'setMyBlockingState':
            setMyBlockingState(request.params[0], request.params[1]);
            break;
          case 'incomingLogEntries':
            incomingLogEntries(request.params);
            break;
          case 'incomingSystemUpTime':
            incomingSystemUpTime(request.params[0]);
          default:
            console.log('error on handleMethodCall: call function ' + request.method + ' is not allowed.');
        }
      }

      /**
       * Handles JSON-RPC response
       */
      function handlerResponse(response) {
        responses.push(response);
      }

      /**
       * Handles JSON-RPC error response
       */
      function handleError(error) {
        lastErrorResponse = error;
        errorResponses.push(error);
      }

    /**
     * Set id of client given by the server.
     * Also triggers sendClientInformation to provide information
     * about this client to the server
     */
    function setClientId(id) {
      clientId = id;
      console.log("ID of this client is now " + id);
      sendClientInformation();
    }

      /**
       * Add new notification to notifications list pushed by the server.
       */
      function incomingNotification(msg) {
        notifications.push(msg);
        console.log("new notification: " + msg);
        $mdToast.show($mdToast.simple().textContent(msg).position('top right').hideDelay(4000));
      }

      /**
       * Show a error notification as toast
       * @param msg text message
       */
      function showAlertNotification(msg) {
        notifications.push(msg);
        console.log("show error notification: " + msg);
        $mdToast.show($mdToast.simple().textContent(msg).position('top right').theme('alert-toast').hideDelay(4000));
      }

      /**
       * Show error notification as toast
       * @param msg text message
       */
      function showErrorNotification(msg) {
        notifications.push(msg);
        console.log("show error notification: " + msg);
        $mdToast.show($mdToast.simple().textContent(msg).position('top right').theme('error-toast').hideDelay(4000));
      }

      function updateKillswitchEnabled(state) {
        killswitch.enabled = state;
        console.log("Updating Killswitch state received from server to: " + state);
      }

      /**
       * Update collision detection information by the server.
       */
      function updateCollisionInformation(collisionInfo) {
        var collisionState = collisionInfo[0];

        collisionDetection.frontLeft = !!collisionState.frontLeft;
        collisionDetection.frontRight = !!collisionState.frontRight;
        collisionDetection.backLeft = !!collisionState.backLeft;
        collisionDetection.backRight = !!collisionState.backRight;
      }

      /**
       * Update rover state
       *    -> isDriverMode available
       *    -> isKillswitch enabled
       */
      function updateRoverState(receivedRoverState) {
        if (receivedRoverState.currentDriverId) {
          setDriverAvailable(receivedRoverState.currentDriverId);
        }

        if (receivedRoverState.isKillswitchEnabled) {
          roverState.isKillswitchEnabled = receivedRoverState.isKillswitchEnabled;
        }
      }

      /**
      * Set availability of driver mode.
      * when currentDriverId is my clientId, Im the driver
      * when currentDriverId is -1 nobody is driver at the moment
      * when currentDriverId is differnet to mine, driver mode is unavailable to me
      */
      function setDriverAvailable(currentDriverId) {
       if (currentDriverId == clientId) {
         // im the driver
         roverState.isDriverAvailable = true;
         console.log('driver mode is available');
       } else if (currentDriverId == -1) {
         // nobody is driver, when im already on driver page i must reaquire the driver mode
         if ($location.path() == '/drive' || $location.path() == '/roverMaster') {
           roverState.isDriverAvailable = false;
           send('enterDriverMode', [clientId]);
         } else {
           roverState.isDriverAvailable = true;
           console.log('driver mode is available');
         }
       } else {
         // somebody else is driver at the moment
         roverState.isDriverAvailable = false;
         console.log('driver mode not available, because client with id ' +currentDriverId+ ' is in driver mode.');
       }
      }

      /**
       * Update connected users
       */
      function updateConnectedUsers(connectedList,blockedList) {
        connectedUsers.list = connectedList;
        blockedUsers.list = blockedList;
      }

      /**
       * Receive image data and invoke callback function
       */
      function incomingSnapshot(imageData) {
        snapshotCallback(imageData);
      }

        /**
         * setBlocking State and disply message to client if he got blocked
         * @param blockingState
         */
      function setMyBlockingState(ipAddress, blockingState){
          myIp.ipAddress = ipAddress;
          if(!(blockingState == myIp.isBlocked)){
            console.log("changed blocking state");
            if(blockingState == true){
              var msg = 'A developer blocked you, no further interaction with the rover possible';
              $mdDialog.show({
                clickOutsideToClose: false,
                template: '<md-dialog aria-label="Blocked"  ng-cloak>' +
                '<md-toolbar>'+
                  '<div class="md-toolbar-tools">'+
                    '<h2>Blocked</h2>'+
                    '<span flex></span>'+
                  '</div>'+
                '</md-toolbar>'+
                '<md-dialog-content>' +
                  '<div class="md-dialog-content">'+
                    '<p>'+ msg +'<\p>'+
                  '<\div>'+
                '</md-dialog-content>' +
                '</md-dialog>'
              });

            }
            else{
              $mdDialog.hide();
            }

          }
          myIp.isBlocked = blockingState;
      }

      /**
       * Receive response for log entries request and invoke callback function
       */
      function incomingLogEntries(params) {
        logEntriesCallback(params);
      }

      function incomingSystemUpTime(param) {
        systemUpTimeCallback(param);
      }

      return {
        /**
         * Get the state of the websocket connection.
         * @return {number} 0 = CONNECTING, 1 = OPEN, 2 = CLOSING, 3 = CLOSED.
         */
        readyState: function () {
          return ws.readyState;
        },
        /**
         * Check for driver mode is available.
         * @return {boolean} true if driver mode is available.
         */
        isdriverModeAvailable: function () {
          return true;
        },
        /**
         * Get the id of the client given by the server
         * @return {number} id.
         */
        getClientId: function () {
          return clientId;
        },
        responses: responses,
        notifications: notifications,
        killswitch: killswitch,
        roverState: roverState,
        collisions: collisionDetection,
        connectedUsers: connectedUsers,
        blockedUsers: blockedUsers,
        clientJs: clientJs,
        errors: errorResponses,
        myIp: myIp,
        getLastErrorResponse: function () {
          return lastErrorResponse;
        },
        getLastSendMsg: function () {
          return lastSendMsg;
        },
        sendPing: function () {
          send("ping", [lastId]);
        },
        /**
         * Stop rover movements
         */
        stop: function () {
          send("stop", []);
        },
        /**
         * Drive rover forward
         */
        driveForward: function () {
          send("driveForward", [desiredSpeed]);
        },
        /**
         * Drive rover backward
         */
        driveBackward: function () {
          send("driveBackward", [desiredSpeed]);
        },
        /**
         * Turn rover left
         */
        turnLeft: function () {
          send("turnLeft", [turnRate]);
        },
        /**
         * Turn rover right
         */
        turnRight: function () {
          send("turnRight", [turnRate]);
        },
        /**
         * Move camera up
         */
        cameraMoveUp: function () {
          send("turnHeadUp", [cameraMoveStep]);
        },
        /**
         * Move camera down
         */
        cameraMoveDown: function () {
          send("turnHeadDown", [cameraMoveStep]);
        },
        /**
         * Move camera left
         */
        cameraMoveLeft: function () {
          send("turnHeadLeft", [cameraMoveStep]);
        },
        /**
         * Move camera right
         */
        cameraMoveRight: function () {
          send("turnHeadRight", [cameraMoveStep]);
        },
        cameraResetPosition: function () {
          send("resetHeadPosition", []);
        },
        /**
         * block or unblock the rover movements (depends on variable isBlocked)
         */
        setKillswitch: function (killswitchEnabled, notificationMessage) {
          send("setKillswitch", [killswitchEnabled, notificationMessage]);
        },
        /**
         * block all interactions with the rover from this ipAddress
         */
        blockIp: function (ipAddress) {
          send("blockIp", [ipAddress]);
        },
        /**
         * allow all interactions with the rover from this ipAddress
         */
        unblockIp: function (ipAddress) {
          send("unblockIp", [ipAddress]);
        },
        /**
         * check whether developer blocked user interaction with rover
         */
        getKillswitchState: function () {
          send("sendKillswitchState", []);
        },
        /**
         * show an alert notification to the user
         */
        showAlertNotification: function (msg) {
          showAlertNotification(msg);
        },
        /**
         * Request for a snapshot
         */
        getCameraSnapshot: function (callback) {
          snapshotCallback = callback;
          send("getCameraSnapshot", [clientId]);
        },
        /**
         * Request log file entries which are newer than the lastLogEntry parameter.
         * If lastLogEntry is null or empty the backend will send all log file entries.
         */
        getLoggingEntries: function (lastLogEntry, callback) {
          logEntriesCallback = callback;
          console.log(lastLogEntry);
          send("getLoggingEntries", [clientId, lastLogEntry]);
        },
        /**
         * Requests the rovers system uptime
         */
        getSystemUpTime: function (callback) {
          if(clientId) {
            systemUpTimeCallback = callback;
            send("getSystemUpTime", [clientId]);
          } else {
            showErrorNotification("Could not fetch systems uptime because connecting to the rover is still in progress.")
          }
        },
        /**
         * Send a alert notification to backend which will
         * it distribute to all users
         */
        sendAlertNotification: function (alertMsg) {
          send("distributeAlertNotification", [alertMsg]);
        },
        /**
         * Enter driver mode --> there can only be one driver at time
         * needs the clientId to register driver, uses a promise object to wait for the clientId being set by the backend
         */
        enterDriverMode: function () {

          //TODO: MFischer should driver mode be available when promise is rejected?

          var clientIdPromise = new Promise(function (resolve, reject) {

            // wait max 5 second for clientID
            var maxTimeout = setTimeout(function () {
              reject(clientId);
            }, 1000);

            // check clientId cyclic every 100 ms
            var checkClientIdInterval = setInterval(checkClientId, 100);

            function checkClientId() {
              if (clientId != 0) {
                clearInterval(checkClientIdInterval);
                clearTimeout(maxTimeout);
                resolve(clientId);
              }
            };
          });

          clientIdPromise.then(function (fulfilledClientId) {
            send("enterDriverMode", [fulfilledClientId]);
            console.log('enterDriverMode promise fulfilled: ' + fulfilledClientId);
          }, function (rejectedClientId) {
            console.log('enterDriverMode promise rejected ' + rejectedClientId);
          });

          return clientIdPromise;

        },

        /**
         * Exit driver mode --> backend should notify all clients that driver mode is now available
         */
        exitDriverMode: function () {
          send("exitDriverMode", [clientId]);
        }
      };
    })
;
