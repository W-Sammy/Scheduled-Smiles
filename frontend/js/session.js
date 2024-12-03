//Intialize the Session page
//Open available appointments on load

const DATA_LOADED_EVENT_NAME = "dataLoaded"
const loaded = {
    treatments: false,
    sessionForm: false,
    trackingBox: false,
    appointments: false
}
let allLoaded = () => Object.values(loaded).every(e => e != false)
let sessionNotes = []

window.onload = () => {
    if (isUserSignedIn()) {
        initSessionsPage()
        loadAppointments()
        loadTreatments()
    }
    showDisplay(document.getElementById("appointmentList"))
    hideDisplay(document.getElementById("sessionForm"))
    hideDisplay(document.getElementById("sessionList"))
};

// sets stuff up before loading
function initSessionsPage() {
    const [e, close] = createLoadingIcon()
    document.getElementById("appointmentList").appendChild(e)
    document.body.addEventListener(DATA_LOADED_EVENT_NAME, (e) => { dataLoadedListener(e, close) })
}

//Generic Show and Hide component functions
function showDisplay(id) {
    id.style.display = "flex"
}

function hideDisplay(id){
    id.style.display = "none"
}

function addNote() {
    const recordListEl = document.getElementById("recordList")
    const noteEl = document.getElementById("recordInput")
    const div = document.createElement("div")
    div.innerHTML = noteEl.value
    div.classList = "appointment"
    recordListEl.appendChild(div)
    sessionNotes.push(noteEl.value)
    noteEl.value = ""
    showNotification("Noted has been Added", 2)
}

//Generic Dom manipulation to control visual of Components
function openSession(){
    hideDisplay(document.getElementById("sessionForm"))
    showDisplay(document.getElementById("sessionList"))
}

function closeSession(){
    hideDisplay(document.getElementById("sessionList"))
    showDisplay(document.getElementById("appointmentList"))
}

function openAppointmentList(){
    showDisplay(document.getElementById("appointmentList"))
    hideDisplay(document.getElementById("sessionForm"))
}

function openForm(){
    hideDisplay(document.getElementById("appointmentList"))
    showDisplay(document.getElementById("sessionForm"))
}

// only runs when everything from DB is loaded
function allDataLoaded() {
    // console.log(loaded)
}

function dataLoadedListener(e, func) {
    if (allLoaded()) {
        func()
        allDataLoaded()
        e.target.removeEventListener(DATA_LOADED_EVENT_NAME, dataLoadedListener)
    }
}

function loadTreatments() {
    document.querySelectorAll("#treatmentList > option:not([disabled])").forEach(el => el.remove())
    const e = document.getElementById("treatmentList")
    getTreatmentTypes().then(treatmentTypes => {
        Object.keys(treatmentTypes).forEach(type => {
            e.appendChild(createTreatment(type))
        })
        dispatchLoadedEvent(treatmentTypes, "treatments")
        dispatchLoadedEvent(document.getElementById("sessionForm").innerHTML, "sessionForm")
        dispatchLoadedEvent(document.getElementById("tracking").innerHTML, "trackingBox")
    })
}

function loadAppointments() {
    getAppointments(getCookieValue("userID")).then(response => {
        Promise.all(Array.from(JSON.parse(response), appointment => {
            const {patientID, staff1ID, staff2ID, staff3ID} = appointment
            const trimmedStaffList = [staff1ID, staff2ID, staff3ID].filter(id => id && id.length && id.replace(/0/g, "").length)
            return Promise.all([
                Promise.resolve(appointment),
                Promise.all([
                    getFullName(patientID),
                    ((staff1ID) ? getFullName(staff1ID) : Promise.resolve(null)),
                    ((staff2ID) ? getFullName(staff2ID) : Promise.resolve(null)),
                    ((staff3ID) ? getFullName(staff3ID) : Promise.resolve(null))
                ])
            ])
        })).then(infos => {
            const apptListEl = document.getElementById("appointmentList")
            const appts = {}
            infos.forEach(info => {
                const [appointment, names] = info
                const {appointmentID, patientID, staff1ID, staff2ID, staff3ID, stationNumber, treatment, notes, startTime, isComplete, isCanceled, isPaid} = appointment
                const [rawPatientName, ...rawStaffNames] = names
                const patientName = rawPatientName.join(" ")
                const staffNames = Array.from(rawStaffNames, n => (n) ? n.join(" ") : '')
                if (!isCanceled || (!isCanceled && !isComplete))
                    apptListEl.appendChild(
                        createAppointmentElement(staffNames.filter(n => n.length), startTime, patientName, appointmentID)
                    )
                appts[appointmentID] = {
                    patientID: patientID,
                    patientName: patientName,
                    staff1ID: staff1ID,
                    staff2ID: staff2ID,
                    staff3ID: staff3ID,
                    staff1Name: staffNames[0],
                    staff2Name: staffNames[1],
                    staff3Name: staffNames[2],
                    stationNumber: stationNumber,
                    treatment: treatment,
                    notes: notes,
                    startTime: startTime,
                    isComplete: isComplete,
                    isCanceled: isCanceled,
                    isPaid: isPaid
                }
                // console.log(appts[appts.length - 1])
            })
            dispatchLoadedEvent(appts, "appointments")
        })
    })
}

function createTreatment(treatmentName) {
    const e = document.createElement("option")
    e.value = treatmentName
    e.innerHTML = treatmentName
    return e
}

function dispatchLoadedEvent(value, key) {
    // console.log("finished loading " + key)
    loaded[key] = value
    document.body.dispatchEvent(new Event(DATA_LOADED_EVENT_NAME))
}

function createAppointmentElement(staffNames, timestamp, patientName, appointmentID) {
    const dateObj = new Date(timestamp * 1000)
    const apptEl = document.createElement("div")
    const infoEl = document.createElement("div")
    const dateEl = document.createElement("span")
    const patientEl = document.createElement("span")
    const timeEl = document.createElement("span")
    const staffEl = document.createElement("span")
    const confirmEl = document.createElement("a")
    const denyEl = document.createElement("a")
    apptEl.classList.add("appointment")
    infoEl.classList.add("infoField")
    dateEl.classList.add("info-date")
    patientEl.classList.add("info-patient")
    timeEl.classList.add("info-time")
    staffEl.classList.add("info-staff")
    confirmEl.classList.add("confirm")
    denyEl.classList.add("deny")
    apptEl.dataset.id = appointmentID
    dateEl.innerHTML = dateObj.toLocaleDateString()
    patientEl.innerHTML = patientName
    timeEl.innerHTML = dateObj.toLocaleTimeString('en-US')
    staffEl.innerHTML = staffNames.join(", ")
    confirmEl.innerHTML = "Open"
    denyEl.innerHTML = "Cancel"

    confirmEl.setAttribute("onclick",`openAppt('${appointmentID}')`)
    denyEl.setAttribute("onclick",`denyAppt('${appointmentID}')`)
    
    apptEl.appendChild(infoEl)
    infoEl.appendChild(dateEl)
    infoEl.appendChild(patientEl)
    infoEl.appendChild(timeEl)
    infoEl.appendChild(staffEl)
    apptEl.appendChild(confirmEl)
    apptEl.appendChild(denyEl)

    return apptEl
}

// only call these after all data has been loaded, or the forms will be removed from DOM.
function resetSessionForm() {
    document.getElementById("sessionForm").innerHTML = loaded.sessionForm
    document.getElementById("infoForm").onsubmit = startSession
}
function resetTrackingBox() {
    document.getElementById("tracking").innerHTML = loaded.trackingBox
    submitOnEnter(document.getElementById("recordInput"), addNote)
}

function openAppt(id) {
    if (!allLoaded())
        return
    const appt = loaded.appointments[id]
    // testing
    // modify form based on appointment data
    resetSessionForm()
    if (appt.staff2ID) {
        let e = document.getElementById("staff2")
        e.readOnly = true
        e.disabled = true
        e.placeholder = appt.staff2Name
    }
    if (appt.staff3ID) {
        let e = document.getElementById("staff3")
        e.readOnly = true
        e.disabled = true
        e.placeholder = appt.staff3Name
    }
    document.getElementById("infoForm").dataset.id = id
    loadAppointmentHistory()
    openForm()
}

function denyAppt(id) {
    if (!allLoaded())
        return
    const e = [...document.querySelectorAll("#appointmentList .appointment")].filter(el => el.dataset.id && el.dataset.id == id)[0]
    const onclickStr = e.getAttribute("onclick")
    e.setAttribute("onclick", "") // prevent spamming the button (idiot proofing)
    // cancel appt in DB
    markAppointmentCanceled(id, true).then(success => {
        if (success) {
            // remove appt in frontend
            e.remove()
            showNotification("Appointment cancelled.", 3)
        } else {
            e.setAttribute("onclick", onclickStr)
            showWarning("Error: failed to cancel appointment. Please try again later.", 3)
        }
    })
}

function startSession(e) {
    e.preventDefault()
    resetTrackingBox()
    openSession()
    populateAppointmentBox(e.target.dataset.id)
}

// wtf man
function populateAppointmentBox(appointmentID) {
    const appointment = loaded.appointments[appointmentID]
    const trackingBoxEl = document.getElementById("tracking")
    const displayStaffEl = document.getElementById("staff")
    const {station, treatment} = getAppointmentData() // notes isnt populated yet
    const [loadingEl, doneLoading] = createLoadingIcon(trackingBoxEl)
    getIdAccount(appointment.patientID).then(patientData => {
        const patientInfo = JSON.parse(patientData)
        const notes = (patientInfo == null) ? "Error: failed to load patient notes." : (patientInfo.detail.length) ? decodeURIComponent(patientInfo.detail) : "No notes."
        document.getElementById("record").innerHTML = notes
        document.getElementById("patient").innerHTML += " " + appointment.patientName
        displayStaffEl.innerHTML = `<h2>${appointment.staff1Name}</h2>`
        displayStaffEl.innerHTML += `<h2>${appointment.staff2Name}</h2>`
        displayStaffEl.innerHTML += `<h2>${appointment.staff3Name}</h2>`
        document.getElementById("stationNumber").innerHTML += " " + station
        document.getElementById("treatmentMarked").innerHTML += " " + treatment
    }).finally(() => {
        doneLoading()
    })
}

// get data from input fields in the DOM
function getAppointmentData() {
    const info = document.getElementById("infoForm")
    const [treatment, station] = [info.elements["treatments"].value, info.elements["station"].value]
    return {station: parseInt(station), treatment: treatment, notes: sessionNotes.join(", ")}
}

function loadAppointmentHistory() {
    console.log("Loading appt history")
    const historyEl = document.getElementById("historyList")
    for (let appointmentID of Object.keys(loaded.appointments).filter(apptID => loaded.appointments[apptID].isComplete && !(loaded.appointments[apptID].isCanceled))) {
        const appointment = loaded.appointments[appointmentID]
        const {staff1Name, staff2Name, staff3Name, startTime, patientName} = appointment
        const staffNames = [staff1Name, staff2Name, staff3Name].filter(n => n.length)
        historyEl.appendChild(
            createAppointmentElement(staffNames, startTime, patientName, appointmentID)
        )
    }
}

function submitSession() {
    const appointmentID = document.getElementById("infoForm").dataset.id
    const {station, treatment, notes} = getAppointmentData()
    const [loadingEl, doneLoading] = createLoadingIcon()
    Promise.all([
        updateAppointment(appointmentID, station, treatment, notes),
        markAppointmentDone(appointmentID, true)
    ]).then(success => {
        if (success.every(s => s)) {
            [...document.querySelectorAll("#appointmentList .appointment")].filter(el => el.dataset.id && el.dataset.id == id)[0].remove()
            showNotification("Appointment updated!", 3)
        } else {
            showWarning("Error: Failed to save appointment data. Please try again.", 3)
        }
    }).finally(() => {
        doneLoading()
        closeSession()
    })
}