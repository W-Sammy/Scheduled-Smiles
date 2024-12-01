/* bills = []
let paymentHistoryLoading = false

window.onload = () => {
    document.body.addEventListener(DATA_LOADED_EVENT_NAME, (e) => {
        if (Object.values(loaded).every(el => el)) {
            allDataLoaded()
            document.body.removeEventListener(DATA_LOADED_EVENT_NAME)
        }
    })
}
*/
/* BROKEN CODE RN
function loadCurrentBilling() {
    const tableBody = document.getElementById("payment-history").getElementsByTagName("tbody")[0]
    const currUserID = getCookieValue("userID")
    const queryString = `SELECT patientID, treatment, startTime, staff1ID FROM appointments WHERE patientID=UNHEX('${currUserID}') AND isCanceled = 0 AND isComplete = 1 AND isPaid = 0 LIMIT 1`
    request(`{"query": "${queryString}"}`, "/api/database/get", "POST")
        .then(response => {
            const data = JSON.parse(response)
            if (data && data.length > 0) {
                const appointmentData = data[0]
                const patientID = appointmentData[0]
                const treatment = appointmentData[1]
                const startTime = appointmentData[2]
                const staff1ID = appointmentData[3]

                var date = new Date(0)
                date.setUTCSeconds(startTime)
                document.getElementById("treatment-date").textContent = (1 + date.getMonth()).toString() + "/" + date.getDate().toString() + "/" + date.getFullYear();
                document.getElementById("treatment").textContent = treatment
                const pairs = JSON.parse(getTreatmentTypes());
                for (let key in pairs) {
                    if (pairs.hasOwnProperty(key)) {
                        console.log(`${key}: ${data[key]}`); 
                    }

                }
            }
            else {
                clearCurrentBilling()
            }
        })
}
*/

function clearCurrentBilling() {
    document.getElementById("treatment-date").textContent = ""
    document.getElementById("treatment").textContent = ""
    document.getElementById("amount-due").textContent = "$0.00"
}

function addToPaymentHistory() {
    const tableBody = document.getElementById("payment-history").getElementsByTagName("tbody")[0]
    const newRow = tableBody.insertRow(0)
    const date = newRow.insertCell(0)
    const treatment = newRow.insertCell(1)
    const primaryStaff = newRow.insertCell(2)
    const total = newRow.insertCell(3)

    date.textContent = document.getElementById("treatment-date").textContent
    treatment.textContent = document.getElementById("treatment").textContent
    primaryStaff.textContent = "test"
    total.textContent = document.getElementById("amount-due").textContent.substring(1)

    clearCurrentBilling()
    loadCurrentBilling()
}

function submitPayment() {
    addToPaymentHistory()
}

function openPayment() {
    document.getElementById('make-payment-box').style.display = 'flex';
}

function closePayment() {
    document.getElementById('make-payment-box').style.display = 'none';
}
