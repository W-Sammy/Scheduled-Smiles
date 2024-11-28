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
            const sortedMessages = messages.sort((a, b) => a.createdAt - b.createdAt)
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
            // Display values
            Object.keys(chats).forEach(id => {
                console.log(id)
                console.log(document.getElementById(id))
                document.getElementById(id).onclick = () => {
                    // Load chat function
                    document.getElementById("message-container").innerHTML = ""
                    document.querySelector("#current-contact .contact-name").innerHTML = document.getElementById(id).dataset.fullName
                    const sent = chats[id].sent
                    const recieved = chats[id].recieved
                    while (sent.length > 0 || recieved.length > 0) {
                        if (!sent.length)
                            appendMessage(recieved.pop().textContent, "left")
                        else if (!recieved.length)
                            appendMessage(sent.pop().textContent, "right")
                        else if (sent[sent.length - 1].createdAt > recieved[recieved.length - 1].createdAt)
                            appendMessage(sent.pop().textContent, "right")
                        else
                            appendMessage(recieved.pop().textContent, "left")
                    }
                }
            })
        })
    })
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
}

function appendMessage(messageContent, type) {
    const messageContainer = document.getElementById("message-container")

    // Creates a new message as a div
    const newMessageDiv = document.createElement('div')
    newMessageDiv.classList.add('message') // adds classname 'message' for styling
    newMessageDiv.classList.add(type)
    newMessageDiv.textContent = messageContent;

    // Appends the new message div in .chat-window
    messageContainer.appendChild(newMessageDiv)
}

function appendNewMessage() {
    // gets the input value from .message-input
    const message = messageInput.value

    appendMessage(message, "right")

    messageInput.value = ''
}