// Constants (change before deploy)
const DASHBOARD_URL = window.location.origin + "/dashboard.html"

// Attaching event listeners
window.onload = () => {
    document.getElementById("signIn").onsubmit = signInSubmitted
    document.getElementById("register").onsubmit = registerSubmitted
    document.getElementById("forgotPassword").onsubmit = forgotPasswordSubmitted
    // document.getElementById("schedule-btn").onclick = () => { redirectTo("calendar") }
    // document.getElementById("make-payment-btn").onclick = () => { redirectTo("billing") }

}

// Form submissions
function signInSubmitted(event) {
    event.preventDefault()
    let formE = event.target
    let [email, pass] = Array.from(formE.elements, e => e.value)
    const [loadingEl, doneLoading] = createLoadingIcon(formE)
    accountEmailExists(email).then(success => {
        if (success) {
            return passwordCorrect(email, pass)
        } else {
            return false
        }
    }).then(validLogin => {
        if (validLogin) {
            return getAccount(email, pass)
        } else {
            return null
        }
    }).then(result => {
        if (result != null && result != undefined) {
            // Assume valid because of prior checks
            let accountInfo = JSON.parse(result)
            setUserSession(accountInfo["roleId"], accountInfo["userId"])
            redirectToDashboard()
        } else {
            // TODO: signin failed, failed to contact database?
            showWarning("Password incorrect!", 3)
        }
    }).catch(e => {
        showWarning("Error, couldn't contact server. Try again later", 5, "bottom", null, formE)
    }).finally(() => {
        doneLoading()
    })
}
function registerSubmitted(event) {
    event.preventDefault()
    let formE = event.target
    let [first, last, email, pass, phone, addr, dob, sex] = Array.from(formE.elements, e => e.value)
    // Kyle here, sorry for this monstrosity:
    // 1. Reformat given date string from YYYY-MM-DD to MM-DD-YYYY (can cause bugs, see: https://stackoverflow.com/a/31732581)
    // 2. Create Date Object, get time (returns as milliseconds, need to convert to seconds)
    // 3. Add timezone offset of client device (returns as minutes, need to convert to seconds)
    dob = Math.round((new Date(dob.replace(
        "([0-9]{4})-([0-9]{2})-([0-9]{2})",
        "$2-$3-$1"
    ))).getTime() / 1000) + ((new Date()).getTimezoneOffset() * 60)
    const [loadingEl, doneLoading] = createLoadingIcon()
    formE.appendChild(loadingEl)
    accountEmailExists(email).then(exists => {
        if (exists) {
            return false
        } else {
            return createAccount(first, last, addr, sex, phone, email, dob, pass)
        }
    }).then(validRegister => {
        if (validRegister) {
            getAccount(email, pass).then(result => {
                // Assume valid because of prior checks
                let accountInfo = JSON.parse(result)
                setUserSession(accountInfo.roleId, accountInfo.userId)
                redirectToDashboard()
            })
        } else {
            showWarning("Error, an account with that email already exists", 5, "bottom", null, formE)
        }
    }).catch(e => {
        // testing
        console.log(e)
    }).finally(() => {
        doneLoading()
    })
}
function forgotPasswordSubmitted(event) {
    event.preventDefault()
    // TODO: implement in backend
    showNotification("Account unsupported!", 5, "top", "10%")
}

// DOM manipulation functions
function hideElement(...elementIds) {
    for (let i = 0; i < elementIds.length; i++) {
        document.getElementById(elementIds[i]).style.display = "none";
    }
}
function showBlockElement(...elementIds) {
    for (let i = 0; i < elementIds.length; i++) {
        document.getElementById(elementIds[i]).style.display = "block";
    }
}

function openSignIn() {
    if(isUserSignedIn(false)) {
        redirectToDashboard()
    } else {
        showBlockElement("signInOverlay")
        hideElement("registerOverlay", "passwordOverlay","aboutUsOverlay")
    }
}
function openRegister() {
    showBlockElement("registerOverlay")
    hideElement("signInOverlay", "passwordOverlay")
}

function openForgotPassword() {
    showBlockElement("passwordOverlay")
    hideElement("signInOverlay", "registerOverlay")
}
function closeSignIn() {
    hideElement("signInOverlay")
}
// Navigation
function redirectToDashboard() {
    window.location.href = DASHBOARD_URL;
}

// We don't need all of the user's info for every page, so only store info that we can use to lookup the rest -Kyle
/**
 * Sets the cookie data for a logged in user.
 * @param {String} roleID The hex representation of the user's role ID.
 * @param {String} userID The hex representation of the user's account ID.
 * @returns {void}
 */
function setUserSession(roleID, userID) {
    setCookie("roleID", roleID, 31536000)
    setCookie("userID", userID, 31536000)
}

function openAboutUs() {
    showBlockElement("aboutUsOverlay")
    hideElement("signInOverlay","registerOverlay","passwordOverlay")
}

function closeAboutUs() {
    hideElement("aboutUsOverlay")
}


/*
  “roleId”: …,
  “userId”: …,
  “firstName”: …,
  “lastName”: …,
  “address”: …,
  “sex”: …,
  “phone”: …,
  “email”: …,
  “birthDate”: …
 */