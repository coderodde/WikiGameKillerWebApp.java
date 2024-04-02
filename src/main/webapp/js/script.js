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
        const obj = JSON.parse(text);
        
        if (obj["status"] === "error") {
            const errorMessages = obj["errorMessages"];
            
            for (let index in errorMessages) {
                logError(errorMessages[index]);
            }
        } else {
            
        }
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
    const div = document.createElement("div");
    const text = document.createTextNode(str);
    div.appendChild(text);
    div.className = "logErrorClass";
    document.getElementById("log").appendChild(div);
}

function logLink(link) {
    const div = document.createElement("div");
    const text = document.createTextNode(link);
    div.appendChild(text);
    div.className = "logLinkClass";
    document.getElementById("log").appendChild(div);
}

function getTitle(link) {
    return decodeURI(link.substring(link.lastIndexOf("/") + 1));
}

function createLink(lineNumber, link) {
    const tr           = document.createElement("tr");
    const tdLineNumber = document.createElement("td");
    const tdLink       = document.createElement("td");
    const tdTitle      = document.createElement("td");
    const a            = document.createElement("a");
    const title        = getTitle(link);
    
    tdLineNumber.innerHTML = `${lineNumber}.`;
    a.href = link;
    a.innerHTML = link;
    tdLink.appendChild(a);
    tdTitle.innerHTML = title;
    
    tr.appendChild(tdLineNumber);
    tr.appendChild(tdLink);
    tr.appendChild(tdTitle);
    
    return tr;
}

function logLinkPath(links) {
    const table = document.createElement("table");
    let lineNumber = 1;
    
    for (let index in links) {
        table.appendChild(
                createLink(
                    lineNumber++,
                    `https://${links[index]}`));
    }
    
    document.getElementById("log").appendChild(table);
}

logLinkPath(["en.wikipedia.org/wiki/Audi", 
             "ru.wikipedia.org/wiki/Василевка_(Болградский_район)"]);
