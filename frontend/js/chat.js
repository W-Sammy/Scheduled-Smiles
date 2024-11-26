window.onload = () => {
    clearData()
    loadData()
}

function loadData() {
    if (!checkCookieExists("userID")) {
        showWarning("Not logged in!", 3, "bottom")
        return
    }
    getChats(getCookieValue("userID")).then(response => {
        // this code wont work
        // TODO: find out how to match this fuckass format of pairID,
        //  one chat has personA->personB, another chat has personB->personA
        //  need to figure how to pair up the array values for corrosponding chats
        JSON.parse(response).forEach(values => {
            const {messages, pairID, receiverID, senderID} = values
            console.log(pairID, receiverID, senderID)
            const contactID = getOtherUser(receiverID, senderID)
            loadContact(contactID)
            messages.sort((a, b) => a.createdAt - b.createdAt).forEach(message => {
                message.content
            })
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
}