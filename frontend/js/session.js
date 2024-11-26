window.onload = () => {
    let appointment =  document.getElementById("appointmentList")
    showDisplay(appointment) 
};

//window.onload = function(){ showDisplay(document.getElementById("appointmentList")) };

function showDisplay(id){
    id.style.display = "flex"
}

function hideDisplay(id){
    id.style.display = "none"
}

function deleteAppointment(e){
    e.parentElement.remove();
}

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


