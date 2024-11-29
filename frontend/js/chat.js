const messagesLoadedEvent = new Event("messagesLoaded")

window.onload = () => {
    clearData()
    loadData()
}

function loadData() {
    if (!checkCookieExists("userID")) {
        showWarning("Not logged in!", 3, "bottom")
        return
    }
    const userID = getCookieValue("userID")
    getChats(userID).then(response => {
        const chats = {}
        // Merge chats
        Promise.all(Array.from(JSON.parse(response), values => {
            const {messages, pairID, receiverID, senderID} = values
            const sortedMessages = messages.filter(a => a.createdAt != 0).sort((a, b) => a.createdAt - b.createdAt)
            const otherContact = getOtherUser(senderID, receiverID)
            let loaded = true
            if (!chats.hasOwnProperty(otherContact)) {
                chats[otherContact] = {sent: [], recieved: []}
                loaded = false
            }
            if (senderID == userID)
                chats[otherContact].sent = sortedMessages
            else
                chats[otherContact].recieved = sortedMessages
            return (loaded) ? Promise.resolve(true) : loadContact(otherContact)
        })).then( _ => {
            document.getElementById("contacts-list").dispatchEvent(messagesLoadedEvent)
        })

        document.getElementById("contacts-list").addEventListener("messagesLoaded", (e) => {
            // All data loaded- start adding event listeners here

            // Send button doesn't work until everything is loaded
            document.getElementById("send").onclick = (e) => {
                e.preventDefault()
                sendNewMessage()
            }

            // Display values
            Object.keys(chats).forEach(id => {
                const e = document.getElementById(id)
                e.onclick = () => {
                    unsetActiveContact()
                    setActiveContact(e)
                    populateMessages(chats[id])
                }
                
            })
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

function populateMessages(chat) {
    const sent = [...chat.sent]
    const recieved = [...chat.recieved]
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

function getOtherUser(user1ID, user2ID) {
    return (user1ID == getCookieValue("userID")) ? user2ID : user1ID
}

// current user always returned as reciever
async function loadContact(userID) {
    return await getFullName(userID).then(response => {
        const fullName = JSON.parse(response)[0].join(" ")
        createContact(fullName, userID)
        return true
    })
}

function createContact(fullName, id) {
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
    imgIcon.src = "./assets/contact.png"
    imgIcon.alt = "Contact Image"
    spanName.classList.add("contact-name")
    spanName.innerHTML = fullName

    contactList.appendChild(divContactContainer)
    divContactContainer.appendChild(divContact)
    divContact.appendChild(imgIcon)
    divContact.appendChild(spanName)
}

function clearData() {
    document.getElementById("contacts-list").innerHTML = ""
    document.getElementById("message-container").innerHTML = ""
    document.querySelector("#current-contact .contact-name").innerHTML = ""
    document.querySelector("#current-contact .contact-icon img").src = ""
    document.querySelector("#current-contact .contact-icon img").alt = ""
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
    newMessageDiv.textContent = messageContent;

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
    console.log(`processing message:\n'${message}'\nReceipient:\n${receiverID}`)
    if (message.length <= 0 || !receiverID)
        return
    messageInput.value = ''
    sendMessage(senderID, receiverID, message).then(success => {
        if (success)
            appendNewMessage(decodeURIComponent(message), "right")
        else
            showWarning("Error: Failed to send message.", 3, "bottom")
    })
}