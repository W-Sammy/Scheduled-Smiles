//Intialize the Session page
//Open available appointments on load

const DATA_LOADED_EVENT_NAME = "dataLoaded"
const loaded = {
    treatments: false,
    appointments: false 
}
let allLoaded = () => Object.values(loaded).every(e => e != false)


window.onload = () => {
    document.body.addEventListener(DATA_LOADED_EVENT_NAME, dataLoadedListener)
    loadAppointments()
    loadTreatments()
    showDisplay(document.getElementById("appointmentList"))
    hideDisplay(document.getElementById("sessionForm"))
    hideDisplay(document.getElementById("sessionList"))
};

//Generic Show and Hide component functions
function showDisplay(id) {
    id.style.display = "flex"
}

function hideDisplay(id){
    id.style.display = "none"
}

function addNote() {
    let record = document.getElementById("recordList")
    let count = record.childNodes.length
    let div = document.createElement("div")
    div.innerHTML = "note " + (count + 1)
    div.classList = "appointment"
    record.appendChild(div)
    showNotification("Noted has been Added", 2)
}

//Generic Dom manipulation to control visual of Components
function openSession(){
    hideDisplay(document.getElementById("sessionForm"))
    showDisplay(document.getElementById("sessionList"))
}

function closeSession(){
    hideDisplay(document.getElementById("sessionList"))
    showDisplay(document.getElementById("sessionForm"))
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
    console.log(loaded)
}

function dataLoadedListener(e) {
    if (allLoaded()) {
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
    })
}

function loadAppointments() {
    getAppointments(getCookieValue("userID")).then(response => {
        Promise.all(Array.from(JSON.parse(response), appointment => {
            const {appointmentID, patientID, staffList, stationNumber, treatment, notes, startTime, isComplete, isCanceled, isPaid} = appointment
            const trimmedStaffList = staffList.filter(id => id && id.length && id.replace(/0/g, "").length)
            return Promise.all([
                Promise.resolve(appointment),
                Promise.resolve(trimmedStaffList),
                Promise.all([
                    Promise.resolve(getFullName(patientID)),
                    ...Array.from(trimmedStaffList, id => getFullName(id))
                ])
            ])
        })).then(infos => {
            const appts = []
            infos.forEach(info => {
                const [appointment, trimmedStaffList, names] = info
                const {appointmentID, patientID, staffList, stationNumber, treatment, notes, startTime, isComplete, isCanceled, isPaid} = appointment
                const [rawPatientName, ...rawStaffNames] = names
                const patientName = rawPatientName.join(" ")
                const staffNames = Array.from(rawStaffNames, n => n.join(" "))
                if (!isCanceled || (!isCanceled && !isComplete))
                    createAppointmentElement(staffNames, startTime, patientName, appointmentID)
                appts.push({
                    appointmentID: appointmentID,
                    patientID: patientID,
                    patientName: patientName,
                    staffList: trimmedStaffList,
                    staffNames: staffNames,
                    stationNumber: stationNumber,
                    treatment: treatment,
                    notes: notes,
                    startTime: startTime,
                    isComplete: isComplete,
                    isCanceled: isCanceled,
                    isPaid: isPaid
                })
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
    console.log("finished loading " + key)
    loaded[key] = value
    document.body.dispatchEvent(new Event(DATA_LOADED_EVENT_NAME))
}

function createAppointmentElement(staffNames, timestamp, patientName, appointmentID) {
    const dateObj = new Date(timestamp * 1000)
    const apptList = document.getElementById("appointmentList")
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
    // testing
    console.log(dateObj.toUTCString())
    patientEl.innerHTML = patientName
    timeEl.innerHTML = dateObj.toLocaleTimeString('en-US')
    staffEl.innerHTML = staffNames.join(", ")
    confirmEl.innerHTML = "Approve"
    denyEl.innerHTML = "Deny"

    confirmEl.setAttribute("onclick",`openAppt('${appointmentID}')`)
    denyEl.setAttribute("onclick",`denyAppt('${appointmentID}')`)
    
    apptList.appendChild(apptEl)
    apptEl.appendChild(infoEl)
    infoEl.appendChild(dateEl)
    infoEl.appendChild(patientEl)
    infoEl.appendChild(timeEl)
    infoEl.appendChild(staffEl)
    apptEl.appendChild(confirmEl)
    apptEl.appendChild(denyEl)
}

function openAppt(id) {
    if (!allLoaded())
        return
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