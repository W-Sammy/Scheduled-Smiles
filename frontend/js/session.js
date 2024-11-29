//Intialize the Session page
//Open available appointments on load

const DATA_LOADED_EVENT_NAME = "dataLoaded"
const loaded = {
    treatments: false,
    appointments: false
}
const appointments = []


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
function showDisplay(id){
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
        dispatchLoadedEvent(loaded.treatments)
    })
}

function loadAppointments() {
    getAppointments(getCookieValue("userID")).then(response => {
        console.log(response)
    })
}

function createTreatment(treatmentName) {
    const e = document.createElement("option")
    e.value = treatmentName
    e.innerHTML = treatmentName
    return e
}

function dispatchLoadedEvent(value) {
    value = true
    document.body.dispatchEvent(new Event(DATA_LOADED_EVENT_NAME))
}