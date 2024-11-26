window.onload = () => {
    loadTabs()
    loadFields()
}

function logout() {
    deleteAllCookies()
    window.location.href = window.location.origin;
}

function loadTabs() {
    if (!checkCookieExists("roleID")) {
        showBigWarning("Please Login to see dashboard information!", 0, "top", "40%")
        return
    }
    getRoleName(getCookieValue("roleID")).then(response => {
        if (response == null) {
            showBigWarning("Please Login to see dashboard information!", 0, "top", "40%")
        } else {
            let res = response.toLowerCase()
            // Equality operators don't work normal, probably a dumb effing encoding issue -Kyle
            if (res.includes( "patient")) {
                addTabToNav("Billing", "billing", "billing")
                addTabToNav("Schedule", "scheduler", "calendar")
            }
            if (res.includes("patient") || res.includes("staff")) {
                addTabToNav("Appointment", "session", "session")
                addTabToNav("History", "history", "history")
            } else if (res.includes("admin")) {
                addTabToNav("Payroll", "payroll", "payroll")
            } else {
                showBigWarning("Please Login to see dashboard information!", 0, "top", "40%")
            }
        }
    })
}

function addTabToNav(text, id, href) {
    let navE = document.getElementById("navigation")
    let div = document.createElement("div")
    let a = document.createElement("a")
    div.classList.add("tab")
    div.setAttribute("id", id)
    a.setAttribute("href", href)
    a.innerHTML = text
    navE.appendChild(div)
    div.appendChild(a)
}

function loadFields() {
    let welcome = document.getElementById("welcome")
    let bday = document.getElementById("dob")
    let name = document.getElementById("name")
    let email = document.getElementById("email")
    getIdAccount(getCookieValue("userID")).then(response => {
        if (response != "false") {
            let account = JSON.parse(response)
            const dob = new Date(0)
            dob.setUTCSeconds(account.birthDate)
            welcome.innerHTML += `, ${account.firstName}!`
            bday.innerHTML = dob.toDateString()
            name.innerHTML = `${account.firstName}, ${account.lastName}`
            email.innerHTML = account.email
        }
    })
}