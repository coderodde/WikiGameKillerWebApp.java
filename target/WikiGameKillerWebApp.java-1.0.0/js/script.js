let searchSocket;
let randomizeSocket;
let sourceUrl;
let targetUrl;

const inputState = {
     "threadsInput":            "256" ,
     "expansionDurationInput":  "4000",
     "waitTimeoutInput":        "1"   ,
     "trialsInput":             "2000",
     "masterSleepInput":        "1"   ,
     "slaveSleepInput":         "1"   
};

const inputNames = {
     "threadsInput":            "Number of threads"         ,
     "expansionDurationInput":  "Maximum expansion duration",
     "waitTimeoutInput":        "Maximum mutex timeout"     ,
     "trialsInput":             "Number of master trials"   ,
     "masterSleepInput":        "Master sleep duration"     ,
     "slaveSleepInput":         "Slave sleep duration"   
};

function constructWebSocketUrl(endpoint) {
    const host = document.location.host;
    const path = document.location.pathname;
    return `ws://${host}${path}${endpoint}`;
}

function searchOnMessageCallback(event) {
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
    } else if (obj["status"] === "halted") {
        logError("[STATISTICS] Search halted.");
        logError(`[STATISTICS] Duration: ${obj["duration"]} milliseconds.`);
        logError(`[STATISTICS] Number of expanded nodes: ${obj["numberOfExpandedNodes"]}.`)
    }

    setSearchReadyButtons();
    searchSocket.close();
    searchSocket = null;
}

function constructWebSocket(endpoint, onMessageCallback) {

    const socket = new WebSocket(constructWebSocketUrl(endpoint));

    socket.onopen = (event) => {
        console.log("onopen. Event: ", event);
    };

    socket.onmessage = onMessageCallback;
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
    if (searchSocket) {
        setMessageBox(
                "Trying to spawn search while " +
                "the previous search process " +
                "is still running.");
        return;
    }
    
    if (!validateInputForm()) {
        setOnInvalidInputForm();
        return;
    }

    setOnSearchButtons();
    searchSocket = constructWebSocket("search", searchOnMessageCallback);

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
    
    sourceUrl = document.getElementById("sourceUrlInput").value;
    targetUrl = document.getElementById("targetUrlInput").value;

    const json = JSON.stringify(searchObject);
    sendData(searchSocket, json);
}

function wikipediaUrlIsValid(url) {
    return /^(http(s)?:\/\/)?..\.wikipedia\.org\/wiki\/.+$/.test(url);
}

function colorInputBorder(elem) {
    document.getElementById("sourceUrlInput").style.borderColor = "red";
}

function halt() {
    if (searchSocket === null) {
        console.log("Cannot halt. Socket is closed.");
        return;
    } else {
        console.log("Halting search on open WebSocket.");
    }

    const requestObject = {
        "action": "halt",
        "searchParameters": {
            "sourceUrl": sourceUrl,
            "targetUrl": targetUrl
        }
    };

    sendData(searchSocket, JSON.stringify(requestObject));
    setSearchReadyButtons();
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
    title = title.replaceAll(" ", "_");
    return `https://en.wikipedia.org/wiki/${title}`;
}

function getRandomArticles() {
    const socket = constructWebSocket("randomize");
    socket.onmessage = function(event) {
        const obj = JSON.parse(event.data);
        let title1 = obj["query"]["random"][0]["title"];
        let title2 = obj["query"]["random"][1]["title"];
        
        title1 = decodeURI(title1);
        title2 = decodeURI(title2);
        
        document.getElementById("sourceUrlInput").value = glueUrl(title1);
        document.getElementById("targetUrlInput").value = glueUrl(title2);
        validateInputForm();
    };

    sendData(socket, "");
}

function getLanguageCode(url) {
    if (url.startsWith("https://")) {
        url = url.substring("https://".length);
    } else if (url.startsWith("http://")) {
        url = url.substring("http://").length;
    }
    
    return url.substring(0, 2);
}

function setSearchReadyButtons() {
    document.getElementById("doSearchButton")         .disabled = false;
    document.getElementById("setDefaultsButton")      .disabled = false;
    document.getElementById("haltButton")             .disabled = true;
    document.getElementById("getRandomArticlesButton").disabled = false;
}

function setOnSearchButtons() {
    document.getElementById("doSearchButton")         .disabled = true;
    document.getElementById("setDefaultsButton")      .disabled = true;
    document.getElementById("haltButton")             .disabled = false;
    document.getElementById("getRandomArticlesButton").disabled = true;
}

function setOnInvalidInputForm() {
    document.getElementById("doSearchButton")         .disabled = true;
    document.getElementById("setDefaultsButton")      .disabled = false;
    document.getElementById("haltButton")             .disabled = true;
    document.getElementById("getRandomArticlesButton").disabled = false;
}

function validateInputForm() {
    let emptyInputName = null;
    
    for (const inputName in inputMap) {
        const inputObject = inputMap[inputName];
        
        if (inputObject.value === "") {
            if (!emptyInputName) {
                emptyInputName = inputName;
            }
            
            inputObject.className = "paramInputError";
        } else {
            inputObject.className = "paramInput";
        }
    }
    
    if (emptyInputName) {
        setOnInvalidInputForm();
        setMessageBox(`${inputNames[emptyInputName]} is empty.`);
    }
    
    const sourceUrlInput = document.getElementById("sourceUrlInput");
    const targetUrlInput = document.getElementById("targetUrlInput");
    let sourcePass;
    let targetPass;
    
    if (sourcePass = wikipediaUrlIsValid(sourceUrlInput.value)) {
        sourceUrlInput.className = "paramInput";
    } else {
        sourceUrlInput.className = "paramInputError";
    }
    
    if (targetPass = wikipediaUrlIsValid(targetUrlInput.value)) {
        targetUrlInput.className = "paramInput";
    } else {
        targetUrlInput.className = "paramInputError";
    }
    
    if (sourcePass && targetPass) {
        const sourceLanguageCode = getLanguageCode(sourceUrlInput.value);
        const targetLanguageCode = getLanguageCode(targetUrlInput.value);
        
        if (sourceLanguageCode !== targetLanguageCode) {
            sourceUrlInput.className = "paramInputError";
            targetUrlInput.className = "paramInputError";
            setMessageBox(
                    `Language mismatch: \"
                    ${sourceLanguageCode}
                    \" vs \"
                    ${targetLanguageCode}
                    \".`);
            
            setOnInvalidInputForm();
            return false;
        } else if (sourceUrlInput.value === targetUrlInput.value) {
            setMessageBox(`Both source URL and target URL (${sourceUrlInput.value}) are the same.`);
            setOnInvalidInputForm();
            return false;
        } else if (!emptyInputName) {
            setSearchReadyButtons();
            clearMessageBox();   
            return true;
        }
    } else if (sourcePass) {
        setMessageBox(`Invalid target URL: \"${targetUrlInput.value}\".`);
        setOnInvalidInputForm();
        return false;
    } else if (targetPass) {
        setMessageBox(`Invalid source URL: \"${sourceUrlInput.value}\".`);
        setOnInvalidInputForm();
        return false;
    } else {
        setMessageBox(`Both source URL (${sourceUrlInput.value}) and target URL (${targetUrlInput.value}) are invalid.`);
        setOnInvalidInputForm();
        return false;
    }
}

function setMessageBox(str) {
    const messageBox = document.getElementById("message-box");
    messageBox.innerHTML = str;
}

function clearMessageBox() {
    setMessageBox("");
}

validateInputForm();