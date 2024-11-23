function openSignIn() {
    document.getElementById("signInOverlay").style.display = "block";
    document.getElementById("registerOverlay").style.display = "none";
    document.getElementById("passwordOverlay").style.display = "none";
}

function openRegister() {
    document.getElementById("signInOverlay").style.display = "none";
    document.getElementById("registerOverlay").style.display = "block";
}

function openForgotPassword() {
    document.getElementById("signInOverlay").style.display = "none";
    document.getElementById("passwordOverlay").style.display = "block";
}

function closeSignIn() {
    document.getElementById("signInOverlay").style.display = "none";
}