let socket;
let searchSocket;
let randomizeSocket;

const inputState = {
     "threadsInput":            "256" ,
     "expansionDurationInput":  "4000",
     "waitTimeoutInput":        "1"   ,
     "trialsInput":             "2000",
     "masterSleepInput":        "1"   ,
     "slaveSleepInput":         "1"   
};

function constructWebSocketUrl(endpoint) {
    const host = document.location.host;
    const path = document.location.pathname;
    const webSocketUrl = `ws://${host}${path}${endpoint}`;

    console.log(`Host: ${host}`);
    console.log(`Path: ${path}`);
    console.log(`WebSocket URL: ${webSocketUrl}`);

    return webSocketUrl;
}

function constructWebSocket(endpoint) {

    socket = new WebSocket(constructWebSocketUrl(endpoint));

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
        } else if (obj["status"] === "solutionFound") {
            logInfo(`[STATISTICS] Duration: ${obj["duration"]} milliseconds.`);
            logInfo(`[STATISTICS] Number of expanded nodes: ${obj["numberOfExpandedNodes"]}.`)
            logLinkPath(obj["urlPath"], obj["languageCode"]);
        } else if (obj["status"] === "getRandomArticles") {
            console.log("hello rnadom");
        }
    };

    socket.onclose = (event) => {
        console.log("onclose. Event: ", event);
    };

    socket.onerror = (event) => {
        console.log("onerror. Event: ", event);
    };

    return socket;
}

function resetParametersToDefaults() {
    document.getElementById("threadsInput")          .value = "256";
    document.getElementById("expansionDurationInput").value = "4000";
    document.getElementById("waitTimeoutInput")      .value = "1";
    document.getElementById("trialsInput")           .value = "2000";
    document.getElementById("masterSleepInput")      .value = "1";
    document.getElementById("slaveSleepInput")       .value = "1";
    
    inputState = {
        "threadsInput":            "256" ,
        "expansionDurationInput":  "4000",
        "waitTimeoutInput":        "1"   ,
        "trialsInput":             "2000",
        "masterSleepInput":        "1"   ,
        "slaveSleepInput":         "1"   
    };
}

function getValue(str, oldValid) {
    if (str === "") {
        return "";
    }
    
    if (str.includes("e") || str.includes("E")) {
        return oldValid;
    }
    
    if (/^\d+$/.test(str) && Number(str) > 0) {
        return str;
    }
    
    return oldValid;
}

const inputMap = {
    "threadsInput":           document.getElementById("threadsInput"),
    "expansionDurationInput": document.getElementById("expansionDurationInput"),
    "waitTimeoutInput":       document.getElementById("waitTimeoutInput"),
    "trialsInput":            document.getElementById("trialsInput"),
    "masterSleepInput":       document.getElementById("masterSleepInput"),
    "slaveSleepInput":        document.getElementById("slaveSleepInput")
}

function tryUpdateNumericInputValue(inputId) {
    const element = document.getElementById(inputId);
    const newValue = element.value.trim();
    const nextValue = getValue(newValue, 
                               inputState[inputId]);
    
    if (nextValue === "") {
        // Show place holder:
        inputMap[inputId].value = "";
        validateInputForm();
        return;
    }
    
    const number = Number(nextValue);
    
    if (number > 0) {
        element.value = nextValue;
        inputState[inputId] = nextValue;
    } else {
        element.value = inputState[inputId]
    }
    
    validateInputForm();
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
    validateInputForm();
    
    if (socket) {
        console.log(
                "Trying to spawn search while " +
                "the previous search process " +
                "is still running.");
        return;
    }

    socket = constructWebSocket("search");

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

    console.log("json: ", json);
    console.log("socket:", socket);

    sendData(socket, json);
    socket = null;
}

function wikipediaUrlIsValid(url) {
    return /^(http(s)?:\/\/)?..\.wikipedia\.org\/wiki\/.+$/.test(url);
}

function colorInputBorder(elem) {
    document.getElementById("sourceUrlInput").style.borderColor = "red";
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
    const div = document.createElement("div");
    const text = document.createTextNode(str);
    div.appendChild(text);
    div.className = "logInfoClass";
    document.getElementById("log").appendChild(div);
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

function logLinkPath(links, languageCode) {
    const table = document.createElement("table");
    let lineNumber = 1;
    
    for (let index in links) {
        table.appendChild(
                createLink(
                    lineNumber++,
                    `https://${languageCode}.wikipedia.org/wiki/${links[index]}`));
    }
    
    document.getElementById("log").appendChild(table);
}

function glueUrl(title) {
    title = encodeURI(title);
    title = title.replaceAll("%20", "_");
    return `https://en.wikipedia.org/wiki/${title}`;
}

function getRandomArticles() {
    const socket = constructWebSocket("randomize");
    socket.onmessage = function(event) {
        const obj = JSON.parse(event.data);
        const title1 = obj["query"]["random"][0]["title"];
        const title2 = obj["query"]["random"][1]["title"];
        
        document.getElementById("sourceUrlInput").value = glueUrl(title1);
        document.getElementById("targetUrlInput").value = glueUrl(title2);
    };

    sendData(socket, "");
}

function validateInputForm() {
    for (const inputName in inputMap) {
        const inputObject = inputMap[inputName];
        
        if (inputObject.value === "") {
            inputObject.className = "paramInputError";
        } else {
            inputObject.className = "paramInput";
        }
    }
    
    const sourceUrlInput = document.getElementById("sourceUrlInput");
    const targetUrlInput = document.getElementById("targetUrlInput");
    
    if (wikipediaUrlIsValid(sourceUrlInput.value)) {
        sourceUrlInput.className = "paramInput";
    } else {
        sourceUrlInput.className = "paramInputError";
    }
    
    if (wikipediaUrlIsValid(targetUrlInput.value)) {
        targetUrlInput.className = "paramInput";
    } else {
        targetUrlInput.className = "paramInputError";
    }
}

validateInputForm();