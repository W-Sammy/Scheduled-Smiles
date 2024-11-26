// Navigation
function redirectTo(location) {
    window.location.href = window.location.origin + "/" + location
}

function isUserSignedIn() {
    return checkCookieExists("userID") && getCookieValue("userID") != undefined && getCookieValue("userID") != null && getCookieValue("userID").length == 64
}

function back() {
    if(isUserSignedIn()) {
        redirectTo("dashboard")
    } else {
        redirectTo("")
    }
}
