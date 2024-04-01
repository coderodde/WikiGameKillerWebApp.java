let socket;
const socketUrl = constructWebSocketUrl();

function constructWebSocketUrl() {
    const host = document.location.host;
    const path = document.location.pathname;
    const webSocketUrl = `ws://${host}${path}search`;

    console.log(`Host: ${host}`);
    console.log(`Path: ${path}`);
    console.log(`WebSocket URL: ${webSocketUrl}`);

    return webSocketUrl;
}

function constructWebSocket() {

    socket = new WebSocket(constructWebSocketUrl());

    socket.onopen = (event) => {
        console.log("onopen. Event: ", event);
    };

    socket.onmessage = (event) => {
        const text = event.data;
        console.log(`Received text: ${text}.`);
    };

    socket.onclose = (event) => {
        console.log("onclose. Event: ", event);
    };

    socket.onerror = (event) => {
        console.log("onerror. Event: ", event);
    };

    return socket;
}
;

function resetParametersToDefaults() {
    document.getElementById("threadsInput")          .value = "256";
    document.getElementById("expansionDurationInput").value = "4000";
    document.getElementById("waitTimeoutInput")      .value = "2";
    document.getElementById("trialsInput")           .value = "200";
    document.getElementById("masterSleepInput")      .value = "15";
    document.getElementById("slaveSleepInput")       .value = "1";
}

function sendData(ws, json) {
    if (ws.readyState) {
        ws.send(json);
    } else {
        setTimeout(function () {
            sendData(ws, json)
        }, 100);
    }
}

function spawnSearch() {
    if (socket) {
        console.log(
                "Trying to spawn search while " +
                "the previous search process " +
                "is still running.");
        return;
    }

    socket = constructWebSocket();

    const searchObject = {
        "action": "search",
        "status": "ignoreParam",
        "searchParameters": {
            "sourceUrl": document.getElementById("sourceUrlInput").value,
            "targetUrl": document.getElementById("targetUrlInput").value,
            "numberOfThreads": Number(document.getElementById("threadsInput").value),
            "expansionDuration": Number(document.getElementById("expansionDurationInput").value),
            "waitTimeout": Number(document.getElementById("waitTimeoutInput").value),
            "masterTrials": Number(document.getElementById("trialsInput").value),
            "masterSleepDuration": Number(document.getElementById("masterSleepInput").value),
            "slaveSleepDuration": Number(document.getElementById("slaveSleepInput").value),
        },
        "errorMessages": [],
        "infoMessages": []
    };

    const json = JSON.stringify(searchObject);
    const webSocket = constructWebSocket();

    console.log("json: ", json);
    console.log("webSocket:", webSocket);

    sendData(webSocket, json);
}

function wikipediaUrlIsValid(url) {
    return /^(http(s)?:\/\/)?..\.wikipedia\.org\/wiki\/.+$/.test(url);
}

function isPositiveInteger(str) {
    if (/^\d+$/.test(str)) {
        return parseInt(str) > 0;
    }

    return false;
}

function colorInputBorder(elem) {
    document.getElementById("sourceUrlInput").style.borderColor = "red";
}

function onChangeThreads() {
    const input = document.getElementById("threadsInput");
    const currentValue = input.value;

    if (isPositiveInteger(currentValue)) {
        input.value = currentValue;
    } else {
        alert("shit");
    }
}

function halt() {
    if (socket === null) {
        console.log("Cannot halt. Socket is closed.");
        return;
    } else {
        console.log("Halting search on open WebSocket.");
    }

    const requestObject = {
        "action": "halt"
    };

    sendData(socket, JSON.stringify(requestObject));
    socket.close();
    socket = null;
}

function clearLog() {
    document.getElementById("log").innerHTML = "";
}

function logInfo(str) {
    const p = document.createElement("p");
    const text = document.createTextNode(str);
    p.appendChild(text);
    p.className = "logInfoClass";
    document.getElementById("log").appendChild(p);
}

function logError(str) {
    const p = document.createElement("p");
    const text = document.createTextNode(str);
    p.appendChild(text);
    p.className = "logErrorClass";
    document.getElementById("log").appendChild(p);
}

            logError("Error 1");
            logInfo("Info");
            logError("Error 2");