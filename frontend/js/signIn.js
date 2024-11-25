// Constants (change before deploy)
const DASHBOARD_URL = window.location.origin + "/dashboard.html"

// Attaching event listeners
window.onload = () => {
    document.getElementById("signIn").onsubmit = signInSubmitted
    document.getElementById("register").onsubmit = registerSubmitted
    document.getElementById("forgotPassword").onsubmit = forgotPasswordSubmitted
}

// Form submissions
function signInSubmitted(event) {
    event.preventDefault()
    let formE = event.target
    let [email, pass] = Array.from(formE.elements, e => e.value)
    // TODO: notify user, evaulating info
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
            // TODO: signin failed, notify user
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
        }
    }).catch(e => {
        // testing
        console.log(e)
    })
}
function registerSubmitted(event) {
    event.preventDefault()
    let formE = event.target
    let [first, last, email, pass, phone, addr, dob, sex] = Array.from(formE.elements, e => e.value)
    dob = dateStringToUtc(dob)
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
            // TODO: registeration failed, notify user acc already exists
        }
    }).catch(e => {
        // testing
        console.log(e)
    })
}
function forgotPasswordSubmitted(event) {
    event.preventDefault()
    // TODO: implement in backend
    alert("Action unsupported for now!")
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


//Overlay Pop-ops
//Open Login overlay
function openSignIn() {
    if(isUserSignedIn()) {
        redirectToDashboard()
    } else {
        showBlockElement("signInOverlay")
        hideElement("registerOverlay", "passwordOverlay")
    }
}

//Open Register overlay
function openRegister() {
    hideElement("signInOverlay", "passwordOverlay")
    showBlockElement("registerOverlay")
}

//Open Forgot Password Overlay
function openForgotPassword() {
    hideElement("signInOverlay", "registerOverlay")
    showBlockElement("passwordOverlay")
}

//Close Login overlay
function closeSignIn() {
    hideElement("signInOverlay")
}

// Navigation
function redirectToDashboard() {
    window.location.href = DASHBOARD_URL;
}


// 
function isUserSignedIn() {
    return checkCookieExists("userID") && getCookieValue("userID") != undefined && getCookieValue("userID") != null && getCookieValue("userID").length == 256
}

// We don't need all of the user's info for every page, so only store info that we can use to lookup the rest -Kyle
/**
 * Sets the cookie data for a logged in user.
 * @param {String} roleID The hex representation of the user's role ID.
 * @param {String} userID The hex representation of the user's account ID.
 * @returns {void}
 */
function setUserSession(roleID, userID) {
    setCookie("roleID", roleID)
    setCookie("userID", userID)
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