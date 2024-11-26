//Intialize the Session page
//Open available appointments on load

//Start with opening Appointment Component
window.onload = () => {
    let appointment =  document.getElementById("appointmentList")
    showDisplay(appointment) 
};

//Generic Show and Hide component functions
function showDisplay(id){
    id.style.display = "flex"
}

function hideDisplay(id){
    id.style.display = "none"
}


//generic Remove element 
// - used to remove appointment elements
// Need to connect to server to remove data as well
function deleteAppointment(e){
    e.parentElement.remove();
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


