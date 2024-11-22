window.onload = () => {
    loadCalendars()
}

// Plceholder function
function submitCalendar(calendarElement) {
    alert("Test output from calendar:\n" + JSON.stringify(getCalendarData(calendarElement), null, " "))
    console.log(getCalendarData(calendarElement))
}

function getCalendarData(calendarElement) {
    const month = parseInt(calendarElement.querySelector(":scope .month .active").dataset.month)
    const days = Array.from(calendarElement.querySelectorAll(':scope .day-numbers input:checked'), (e) => parseInt(e.value) ) // returns an array (if using checkboxes, will return all selected options)
    const year = parseInt(calendarElement.querySelector(":scope .year").innerHTML)
    return {month: month, days: days, year: year}
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