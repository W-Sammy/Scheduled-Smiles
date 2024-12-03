const messagesLoadedEvent = new Event("messagesLoaded")
const chats = {}
let refreshID
let activeID = null
let messagesLoading = false

window.onload = () => {
    submitOnEnter(document.getElementById("message-input"), sendNewMessage)
    clearData()
    if (isUserSignedIn()) {
        setLoadedListener()
        loadData()
    }
}

function loadContactOptions() {
    const contactSelect = document.getElementById("newContactDropdown")
    const currUserID = getCookieValue("userID")
    const queryString = `SELECT userID, firstName, lastName FROM users WHERE NOT userID=UNHEX('${("0").repeat(64)}') AND NOT userID=UNHEX('${currUserID}') AND ` + Array.from(document.querySelectorAll("#contacts-list > .contact"), el => `NOT userID=UNHEX('${el.id}')`).join(" AND ")
    request(`{"query": "${queryString}"}`, "/api/database/get", "POST").then(response => {
        const ids = Array.from(JSON.parse(response), id => id[0])
        const names = Array.from(JSON.parse(response), id => id[1] + " " + id[2])
        JSON.parse(response).forEach(data => {
            contactSelect.appendChild(createNewContactOption(data[1] + " " + data[2], data[0]))
        })
    })
}

function createNewContact() {
    const contactE = document.querySelector("#newContactDropdown > option:checked")
    if (!contactE || contactE.value == "default")
        return
    const contactID = contactE.value
    loadContact(contactID).then(success => {
        document.getElementById("newContactDropdown").value = "default"
        contactE.remove()
    })
}

function createNewContactOption(name, id) {
    const e = document.createElement("option")
    e.value = id
    e.innerHTML = name
    return e
}

function stopRefreshing() {
    clearInterval(refreshID);
}

function setRefreshInterval() {
    refreshID = setInterval(function() {
        loadData()
    }, 0.75 * 1000) // Databse can't handle messages more than one second
}

function setLoadedListener() {
    const el = document.getElementById("contacts-list")
    const [loadingEl, doneLoading] = createLoadingIcon()
    el.appendChild(loadingEl)
    el.addEventListener("messagesLoaded", (e) => {
        doneLoading()
        // All data loaded- start adding things here
        setRefreshInterval()
        loadContactOptions()

        // Send button doesn't work until everything is loaded
        document.getElementById("send").onclick = (e) => {
            e.preventDefault()
            sendNewMessage()
        }
    }, { once: true })
}

function loadData() {
    if (messagesLoading) {
        return
    }
    messagesLoading = true
    const userID = getCookieValue("userID")
    getChats(userID).then(response => {
        // Merge chats
        if (JSON.parse(response) != null)
            Promise.all(Array.from(JSON.parse(response), values => {
                const {messages, pairID, receiverID, senderID} = values
                const sortedMessages = messages.filter(a => a.createdAt != 0).sort((a, b) => a.createdAt - b.createdAt)
                return loadContact(...((senderID == userID) ? [receiverID, sortedMessages, null] : [senderID, null, sortedMessages]))
            })).then( _ => {
                document.getElementById("contacts-list").dispatchEvent(messagesLoadedEvent)
                messagesLoading = false
                populateMessages()
            })
    })
}

function unsetActiveContact() {
    document.querySelectorAll("#contacts-list > .contact.active").forEach(e => e.classList.remove("active"))
    document.getElementById("message-container").innerHTML = ""
}

function setActiveContact(el) {
    document.getElementById("current-contact").innerHTML = el.innerHTML
    el.classList.add("active")
}

function populateMessages() {
    if (activeID == null)
        return
    const chat = chats[activeID]
    const sent = [...chat.sent]
    const recieved = [...chat.recieved]
    clearMessages()
    while (sent.length > 0 || recieved.length > 0) {
        if (!sent.length)
            appendMessage(decodeURIComponent(recieved.pop().textContent), "left")
        else if (!recieved.length)
            appendMessage(decodeURIComponent(sent.pop().textContent), "right")
        else if (sent[sent.length - 1].createdAt > recieved[recieved.length - 1].createdAt)
            appendMessage(decodeURIComponent(sent.pop().textContent), "right")
        else
            appendMessage(decodeURIComponent(recieved.pop().textContent), "left")
    }
}

// current user always returned as reciever
async function loadContact(userID, sent = null, recieved = null) {
    if (!chats.hasOwnProperty(userID))
        chats[userID] = {sent: [], recieved: []}
    if (sent)
        chats[userID].sent = sent
    if (recieved)
        chats[userID].recieved = recieved
    if (document.getElementById(userID) == null) {
        return await getFullName(userID).then(response => {
            const fullName = response.join(" ")
            createContact(fullName, userID)
        })
    }
    return true
}

function createContact(fullName, id) {
    const existingE = document.getElementById(id)
    if (existingE)
        return existingE
    const contactList = document.getElementById("contacts-list")
    const divContactContainer = document.createElement("div")
    const divContact = document.createElement("div")
    const imgIcon = document.createElement("img")
    const spanName = document.createElement("span")

    divContactContainer.classList.add("contact")
    divContactContainer.id = id
    divContactContainer.dataset.fullName = fullName
    divContact.classList.add("contact-icon")
    divContact.dataset.id = id // messy...
    imgIcon.draggable = "false" 
    imgIcon.src = "./assets/contact.png"
    imgIcon.alt = "Contact Image"
    spanName.classList.add("contact-name")
    spanName.innerHTML = fullName

    contactList.appendChild(divContactContainer)
    divContactContainer.appendChild(divContact)
    divContact.appendChild(imgIcon)
    divContact.appendChild(spanName)

    divContactContainer.onclick = () => {
        activeID = id
        unsetActiveContact()
        setActiveContact(divContactContainer)
        populateMessages()
    }
    return divContactContainer
}

function clearData() {
    document.getElementById("contacts-list").innerHTML = ""
    document.querySelector("#current-contact .contact-name").innerHTML = ""
    document.querySelector("#current-contact .contact-icon img").draggable = ""
    document.querySelector("#current-contact .contact-icon img").src = ""
    document.querySelector("#current-contact .contact-icon img").alt = ""
    clearMessages()
}

function clearMessages() {
    document.getElementById("message-container").innerHTML = ""
}

function appendMessage(messageContent, type) {
    const messageContainer = document.getElementById("message-container")

    // Creates a new message as a div
    const newMessageDiv = document.createElement('div')
    newMessageDiv.classList.add('message') // adds classname 'message' for styling
    newMessageDiv.classList.add(type)
    newMessageDiv.innerText = messageContent;

    // Appends the new message div in .chat-window
    messageContainer.appendChild(newMessageDiv)
}

// literally the same as appendMessage except it inserts the message element at the top of the column instead of the bottom
function appendNewMessage(messageContent, type) {
    const messageContainer = document.getElementById("message-container")

    // Creates a new message as a div
    const newMessageDiv = document.createElement('div')
    newMessageDiv.classList.add('message') // adds classname 'message' for styling
    newMessageDiv.classList.add(type)
    newMessageDiv.textContent = messageContent

    // Appends the new message div in .chat-window
    messageContainer.insertBefore(newMessageDiv, messageContainer.firstChild)
}

function sendNewMessage() {
    // gets the input value from #message-input
    const messageInput = document.getElementById("message-input")
    const receiverID = document.querySelector("#current-contact > div").dataset.id
    const senderID = getCookieValue("userID")
    // escape all control chars before JSON parsing
    const message = encodeURIComponent(messageInput.value)
    if (message.length <= 0 || !receiverID)
        return
    messageInput.value = ''
    sendMessage(senderID, receiverID, message).then(success => {
        if (success)
            if (receiverID == document.querySelector("#current-contact > div").dataset.id)
                appendNewMessage(decodeURIComponent(message), "right")
        else
            showWarning("Error: Failed to send message.", 3, "bottom")
    })
}