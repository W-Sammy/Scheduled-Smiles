// Navigation
function redirectTo(location = "") {
    window.location.href = window.location.origin + "/" + location
}

function isUserSignedIn() {
    return checkCookieExists("userID") && getCookieValue("userID") != undefined && getCookieValue("userID") != null && getCookieValue("userID").length == 64
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