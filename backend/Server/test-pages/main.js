function main() {
    var [endpoint, method, body] = getData()
    sendRequest(endpoint, method, body)
    
}

function getData() {
    const requestBodyArea = document.getElementById("requestBody")
    const endpointArea = document.getElementById("endpoint")
    const methodsSelect = document.getElementById("methods")
    var requestBody = requestBodyArea.value
    var method = methodsSelect.value
    var endpoint = endpointArea.value
    return [endpoint, method, requestBody]
}

function sendRequest(endpoint, method, requestBody) {
    console.log("Sending " + method + " to " + endpoint + ":\n" + requestBody);
    fetch(window.location.origin + endpoint, {
        method: method,
        body: JSON.stringify(JSON.parse(requestBody)),
        headers: {
            'Accept': 'application/json',
            "Content-type": "application/json; charset=UTF-8"
        }
    }).then(response => {
        if(response.ok){
            return Promise.all([response.status, response.text()])
        }
        throw new Error('Request failed!')
    }, networkError => {
      console.log(networkError.message) 
    }).then((values) => {
      console.log(values)
      loadResponse(...values)
    })
}

// Dev only. remove before deployment. Requires import of the marked JS library -Kyle
function renderApiDoc() {
    const md = markdownit({
      html: true,
      linkify: true,
      typographer: true
    })
  fetch("https://raw.githubusercontent.com/W-Sammy/Scheduled-Smiles/refs/heads/backend/docs/API_REFERENCE.md")      // The path to the raw Markdown file
    .then(response => response.text())
    .then(markdown => {
      document.getElementById("apiDoc").innerHTML = md.render(markdown)//marked.parse(markdown)
    })
}

window.onload = () => {
    renderApiDoc()
}
    
function loadResponse(responseStatus, responseText) {
    const responseBodyArea = document.getElementById("responseBody")
    const responseStatusArea = document.getElementById("responseStatus")
    responseBodyArea.value = JSON.stringify(JSON.parse(responseText), null, 2)
    responseStatusArea.innerHTML = responseStatus
}