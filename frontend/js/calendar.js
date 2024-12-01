let isStaff = true

window.onload = () => {
    loadMenu()
    loadCalendars()
}

function loadMenu() {
    if (!checkCookieExists("roleID")) {
        showBigWarning("Login to schedule an appointment!", 5)
        document.getElementById("staffMenu").remove()
        document.getElementById("patientMenu").remove()
        return
    }
    

    getRoleName(getCookieValue("roleID")).then(response => {
        const roleName = JSON.parse(response)
        if (response != null) {
            if (roleName == "Patient") {
                isStaff = false
                document.getElementById("staffMenu").remove()
                return
            } else if (roleName == "Staff") {
                document.getElementById("patientMenu").remove()
                return
            }
        }
        console.log("Error: could not determine valid roleID: " + roleName)
        document.getElementById("staffMenu").remove()
        document.getElementById("patientMenu").remove()
    })
}

function scheduleAppointment() {
    if (!checkCookieExists("userID")) {
        showBigWarning("Login to schedule an appointment!", 5)
        return
    }
    if (document.querySelector(".staff-select-row > .staff-select > option:checked").value == "default") {
        showWarning("Choose a staff member to schedule an appointment with!", 3)
        return
    }
    submitCalendar(document.getElementById("cal1")) // only one calendar on the page
    populateStaffLists()
}

function submitCalendar(calendarElement) {
    const timestamp = getCalendarData(calendarElement)
    if (timestamp) {
        if (!isStaff) {
            const patientID = getCookieValue("userID")
            bookAppointment(patientID, (document.querySelectorAll(".staff-select-row > .staff-select > option:checked")[0].value == "default") ? null : document.querySelectorAll(".staff-select-row > .staff-select > option:checked")[0].value, null, null, timestamp).then(success => {
                if (success) {
                    showNotification("Appointment booked!", 3)
                } else if (success != null) {
                    showWarning("Error: appointment could not be booked. Please try again later.", 3)
                }
            })
        } else {
            // get ID for patient
            const patientEmail = document.getElementById("patientEmail").value
            accountEmailExists(patientEmail).then(emailExists => {
                if (emailExists) {
                    return getUserId(patientEmail)
                } else {
                    showWarning("Patient email does not exist!", 5)
                    return null
                }
            }).then(patientID => {
                if (!patientID)
                    return null
                const staff1ID = getCookieValue("userID")
                const [staff2ID, staff3ID] = Array.from(document.querySelectorAll(".staff-select-row > .staff-select > option:checked"), e => (e.value == "default") ? null : e.value)
                return bookAppointment(patientID, staff1ID, staff2ID, staff3ID, timestamp)
            }).then(success => {
                if (success) {
                    showNotification("Appointment booked!", 3)
                } else if (success != null) {
                    showWarning("Error: appointment could not be booked. Please try again later.", 3)
                }
            })
        }
    } else {
        showWarning("Please select a time!", 5)
    }
}

function getCalendarData(calendarElement) {
    const days = Array.from(calendarElement.querySelectorAll(':scope .day-numbers input:checked'), (e) => parseInt(e.value) ) // returns an array (if using checkboxes, will return all selected options)
    if (days.length) {
        const day = days[0] + 1
        const month = parseInt(calendarElement.querySelector(":scope .month .active").dataset.month)
        const year = parseInt(calendarElement.querySelector(":scope .year").innerHTML)
        const hour = parseInt(document.getElementsByClassName("hour")[0].value) // there should only be one
        const timezoneOffset = (new Date()).getTimezoneOffset() * 60
        const timestamp = (Date.UTC(year, month - 1, day, hour) / 1000) - timezoneOffset
        return timestamp
    } else {
        return null
    }
}

function loadCalendars() {
    var calendarElements = document.getElementsByClassName("calendar")
    for (const e of calendarElements) {
        loadCalendar(e)
    }
}

function loadCalendar(calendarElement) {
    loadCalendarButtons(calendarElement)
    loadCalendarYear(calendarElement)
    reloadCalendar(calendarElement)
}

// Do NOT add new event listners if we are just trying to refresh the calendar
function reloadCalendar(calendarElement) {
    _fixFebuary(calendarElement)
    loadCalendarDays(calendarElement)
}

// Doesn't really "load" per-se, just adds event listeners
function loadCalendarButtons(calendarElement) {
    // Month buttons
    let headerElementsContainer = calendarElement.querySelector(":scope .calendar-header")
    let prevMonthButtonElement = headerElementsContainer.querySelector(":scope .month-buttons .prev")
    let nextMonthButtonElement = headerElementsContainer.querySelector(":scope .month-buttons .next")
    prevMonthButtonElement.onclick = () => { prevMonth(calendarElement); reloadCalendar(calendarElement) }
    nextMonthButtonElement.onclick = () => { nextMonth(calendarElement); reloadCalendar(calendarElement) }
    // Year buttons
    let prevYearButtonElement = headerElementsContainer.querySelector(":scope .year-buttons .prev")
    let nextYearButtonElement = headerElementsContainer.querySelector(":scope .year-buttons .next")
    prevYearButtonElement.onclick = () => { prevYear(calendarElement); reloadCalendar(calendarElement) }
    nextYearButtonElement.onclick = () => { nextYear(calendarElement); reloadCalendar(calendarElement) }
    // Submit buttons
    [...document.getElementsByClassName("calendar-submit")].forEach(e => {
        if (e.dataset.calendarId == calendarElement.id) {
            e.onclick = () => { submitCalendar(calendarElement) }
        }
    });
    // any buttons
    [...document.querySelectorAll("select:not(.staff-select)")].forEach(e => {
        e.onchange = () => { populateStaffLists() }
    })
}

function loadCalendarDays(calendarElement) {
    // We rely on the data attribute "days" to determine the number of days per month.
    // This should be simpler than evaluating the name of the month in a massive
    // switch statement. The drawback is that the calendar can be tampered with
    // by a user in the browser developer tools, so this is really just for speed. -Kyle
    const selectype = "radio" // either radio or checkbox
    const days = parseInt(calendarElement.querySelector(":scope .month .active").dataset.days)
    let dayElementsContainer = calendarElement.querySelector(":scope .day-numbers")
    // Wipe any old elements (and dev comments)
    dayElementsContainer.innerHTML = "";
    for (let i = 1; i <= days; i++) {
        var li = document.createElement("li")
        var e = document.createElement("input")
        e.type = selectype
        e.name = "day"
        e.value = i
        var l = document.createElement("label")
        l.innerHTML = i
        li.onclick = populateStaffLists
        dayElementsContainer.append(li)
        li.append(e)
        li.append(l)
    }
}

function loadCalendarYear(calendarElement) {
    // Our timestamps in backend only support dates up to Jan, 2038 -Kyle
    let yearElement = calendarElement.querySelector(":scope .year")
    yearElement.innerHTML = getYear()
}

function nextYear(calendarElement) {
    let yearElement = calendarElement.querySelector(":scope .year")
    const activeYear = parseInt(yearElement.innerHTML)
    yearElement.innerHTML = (activeYear >= yearElement.dataset.max) ?
        getYear() : activeYear + 1;
}

function prevYear(calendarElement) {
    let yearElement = calendarElement.querySelector(":scope .year")
    const activeYear = parseInt(yearElement.innerHTML)
    const currentYear = getYear()
    yearElement.innerHTML = (activeYear <= currentYear) ?
        yearElement.dataset.max : activeYear - 1;
}

function nextMonth(calendarElement) {
    let monthElementsContainer = calendarElement.querySelector(":scope .month")
    let oldActiveMonth = monthElementsContainer.querySelector(":scope .active")
    let newActiveMonth = (oldActiveMonth.nextElementSibling == null) ? monthElementsContainer.firstElementChild : oldActiveMonth.nextElementSibling
    oldActiveMonth.classList.remove("active")
    newActiveMonth.classList.add("active")
}

function prevMonth(calendarElement) {
    let monthElementsContainer = calendarElement.querySelector(":scope .month")
    let oldActiveMonth = monthElementsContainer.querySelector(":scope .active")
    let newActiveMonth = (oldActiveMonth.previousElementSibling == null) ? monthElementsContainer.lastElementChild : oldActiveMonth.previousElementSibling
    oldActiveMonth.classList.remove("active")
    newActiveMonth.classList.add("active")
}

// fucking febuary man
function _fixFebuary(calendarElement) {
    // Check for febuary in this function to avoid clutter in caller function(s)
    const activeYear = parseInt(calendarElement.querySelector(":scope .year").innerHTML)
    let monthElementsContainer = calendarElement.querySelector(":scope .month")
    let activeMonth = monthElementsContainer.querySelector(":scope .active")
    if (parseInt(activeMonth.dataset.days) < 30) {
        activeMonth.dataset.days = (_isLeapYear(activeYear)) ? 29 : 28
    }
}

function getYear() {
    return new Date(Date.now()).getFullYear()
}

function _isLeapYear(year)
{
  return ((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0);
}

// TYSM DANN THE GOAT -Kyle
// populates dummy data
function populateStaffLists() {
    // convert calendar time to utc timestamps
    const timestamp = getCalendarData(document.getElementById("cal1")) // only one calendar on the page
    if (!timestamp) // set available staff to none / blank if no day is selected
        return
    getAvailableStaff(timestamp).then(response => {
        const staffIDs = JSON.parse(response)
        return Promise.all(Array.from(staffIDs, id => Promise.all([id, getFullName(id)])))
    }).then(response => {
        const staffList = Array.from(response, (arrStr) => [arrStr[0], arrStr[1].join(" ")])
        // selects all dropdown elements with class "staff-select"
        const staffDropdowns = document.querySelectorAll(".staff-select");
        staffDropdowns.forEach((dropdown) => {
            dropdown.innerHTML = "<option value=\"default\" selected disabled hidden>None</option>" // quick and dirty
            staffList.forEach((staff) => {
                const option = document.createElement("option");
                option.value = staff[0];
                option.textContent = staff[1];
                dropdown.appendChild(option);
            })
        })
    })
}