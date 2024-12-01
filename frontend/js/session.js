//Intialize the Session page
//Open available appointments on load

const DATA_LOADED_EVENT_NAME = "dataLoaded"
const loaded = {
    treatments: false,
    appointments: false
}

window.onload = () => {
    document.body.addEventListener(DATA_LOADED_EVENT_NAME, (e) => {
        if (Object.values(loaded).every(el => el)) {
            allDataLoaded()
            document.body.removeEventListener(DATA_LOADED_EVENT_NAME)
        }
    })
    loadAppointments()
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

//generic Remove element 
// - used to remove appointment elements
// Need to connect to server to remove data as well
function deleteAppointment(e){
    e.parentElement.remove();
    showWarning("Appointment Deleted", 2.5, "top","11.5%")
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
    
}

function loadTreatments() {
    document.querySelectorAll("#treatmentList > option:not([disabled])").forEach(el => el.remove())
    const e = document.getElementById("treatmentList")
    getTreatmentTypes().then(treatmentTypes => {
        Object.keys(treatmentTypes).forEach(type => {
            e.appendChild(createTreatment(type))
        })
        dispatchLoadedEvent(treatmentTypes, loaded.treatments)
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
                createAppointmentElement(staffNames, startTime, patientName)
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
            dispatchLoadedEvent(appts, loaded.appointments)
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
    key = value
    document.body.dispatchEvent(new Event(DATA_LOADED_EVENT_NAME))
}

function createAppointmentElement(staffNames, timestamp, patientName) {
    const dateObj = new Date(timestamp)
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
    dateEl.innerHTML = dateObj.toDateString()
    patientEl.innerHTML = patientName
    timeEl.innerHTML = dateObj.toLocaleTimeString('en-US')
    staffEl.innerHTML = staffNames.join(", ")
    confirmEl.innerHTML = "Approve"
    denyEl.innerHTML = "Deny"

    confirmEl.setAttribute("onclick","openForm();");
    denyEl.setAttribute("onclick","deleteAppointment(this);");
    
    apptList.appendChild(apptEl)
    apptEl.appendChild(infoEl)
    infoEl.appendChild(dateEl)
    infoEl.appendChild(patientEl)
    infoEl.appendChild(timeEl)
    infoEl.appendChild(staffEl)
    apptEl.appendChild(confirmEl)
    apptEl.appendChild(denyEl)
}