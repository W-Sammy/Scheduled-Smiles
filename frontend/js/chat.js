//
window.onload = () => {
    loadData()
}

function loadData() {
    if (checkCookieExists("userID"))
        return false
    getChats(getCookieValue("userID")).then(response => {
        JSON.parse(response).forEach({messages, pairID, receiverID, senderID} => {
            
        }
    })
}