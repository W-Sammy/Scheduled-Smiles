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
        JSON.parse(response).forEach(values => {
            const {messages, pairID, receiverID, senderID} = values
            const sortedMessages = messages.sort((a, b) => a.createdAt - b.createdAt)
            const otherContact = getOtherUser(senderID, receiverID)
            if (!chats.hasOwnProperty(otherContact)) {
                chats[otherContact] = {sent: [], recieved: []}
                loadContact(otherContact)
            }
            if (senderID == userID)
                chats[otherContact].sent = sortedMessages
            else
                chats[otherContact].recieved = sortedMessages
        })

        // Display values
        Object.keys(chats, id => {
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
        })
    })
}

function getOtherUser(user1ID, user2ID) {
    return (user1ID == getCookieValue("userID")) ? user2ID : user1ID
}

// current user always returned as reciever
function loadContact(userID) {
    getFullName(userID).then(response => {
        const fullName = JSON.parse(response)[0].join(" ")
        createContact(fullName)
    })
}

function createContact(fullName) {
    const contactList = document.getElementById("contacts-list")
    const divContactContainer = document.createElement("div")
    const divContact = document.createElement("div")
    const imgIcon = document.createElement("img")
    const spanName = document.createElement("span")
    
    divContactContainer.classList.add("contact")
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
    const chatWindow = document.querySelector('.chat-window')
    const messageInput = document.querySelector('.message-input')

    // Creates a new message as a div
    const newMessageDiv = document.createElement('div')
    newMessageDiv.classList.add('message') // adds classname 'message' for styling
    newMessageDiv.classList.add(type)
    newMessageDiv.textContent = messageContent;

    // Appends the new message div in .chat-window
    chatWindow.appendChild(newMessageDiv)
}

function appendNewMessage() {
    // gets the input value from .message-input
    const message = messageInput.value

    appendMessage(message, "right")

    messageInput.value = ''
}