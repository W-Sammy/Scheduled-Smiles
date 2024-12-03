// Navigation
function redirectTo(location = "") {
    window.location.href = window.location.origin + "/" + location
}

function back() {
    if(isUserSignedIn()) {
        redirectTo("dashboard")
    } else {
        redirectTo()
    }
}

function logout() {
    deleteAllCookies()
    redirectTo()
}

function submitOnEnter(targetEl, func) {
    targetEl.addEventListener("keydown", (e) => {
        const key = window.event.keyCode
        // If the user has pressed enter
        if (key === 13) {
            e.preventDefault()
            func()
        }
    })
}

// displays login error message in the page if user is not signed in
function isUserSignedIn(showMessage = true) {
    if (checkCookieExists("userID") && getCookieValue("userID") != undefined && getCookieValue("userID") != null && getCookieValue("userID").length == 64) {
        return true
    }
    if (showMessage)
        showBigWarning("Not logged in!", 3, "bottom")
    return false
}