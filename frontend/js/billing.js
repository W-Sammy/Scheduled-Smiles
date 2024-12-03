window.onload = () => {
    clearBilledAmount() // clear placeholders
    document.getElementById("make-payment-form").onsubmit = submitPayment
    if (isUserSignedIn()) {
        loadCurrentBilling()
    }
}

function loadCurrentBilling() {
    const tableBody = document.querySelector("#payment-history tbody")
    const [loadingEl, doneLoading] = createLoadingIcon(document.getElementById("payment-history"))
    Promise.all([
        getTreatmentTypes(),
        getAppointments(getCookieValue("userID"))
    ]).then(([treatments, response]) => {
        // redundant code, should be wrapped in its own function atp -Kyle
        const resp = JSON.parse(response)
        if (resp)
            return Promise.all([
                Promise.resolve(treatments),
                Promise.all(Array.from(
                    resp.filter(appt => appt.isComplete && !appt.isPaid),
                    appointment => {
                        return Promise.all([
                            Promise.resolve(appointment),
                            ((appointment.staff1ID) ? getFullName(appointment.staff1ID) : Promise.resolve(null))
                        ])
                    }
                ))
            ])
        return Promise.all([
            Promise.resolve(null),
            Promise.resolve(null)
        ])
    }).then(([treatments, appointments]) => {
        if (!treatments)
            return
        let totalBill = 0
        appointments.forEach(([appointment, staff1Name]) => {
            const dateStr = (new Date(appointment.startTime * 1000)).toLocaleDateString()
            const treatmentStr = appointment.treatment
            const staffStr = staff1Name.reverse().join(", ")
            const totalStr = (treatmentStr in treatments) ? "$" + treatments[treatmentStr] : " --- "
            const apptID = appointment.appointmentID
            totalBill += (treatmentStr in treatments) ? treatments[treatmentStr] : 0
            addToPaymentHistory(dateStr, treatmentStr, staffStr, totalStr, apptID)
        })
        setBilledAmount(totalBill)
    }).finally(() => {
        doneLoading()
    })
}

// expects FLOAT/DOUBLE value
function setBilledAmount(total) {
    document.getElementById("amount-due").innerHTML = toPriceString(total)
}

function getBilledAmount() {
    return parseFloat(document.getElementById("amount-due").innerHTML.substring(1))
}

function clearBilledAmount() {
    setBilledAmount(0)
}

function addToPaymentHistory(date, treatment, staff, total, apptID) {
    const tableBody = document.querySelector("#payment-history tbody")
    const newRow = tableBody.insertRow(0)
    const dateCell = newRow.insertCell(0)
    const treatmentCell = newRow.insertCell(1)
    const primaryStaffCell = newRow.insertCell(2)
    const totalCell = newRow.insertCell(3)
    const isPaidCell = newRow.insertCell(4)

    newRow.id = apptID
    dateCell.classList.add("date-cell")
    treatmentCell.classList.add("treatment-cell")
    primaryStaffCell.classList.add("staff-cell")
    totalCell.classList.add("total-cell")
    isPaidCell.classList.add("paid-cell")
    dateCell.innerHTML = date
    treatmentCell.innerHTML = treatment
    primaryStaffCell.innerHTML = staff
    totalCell.innerHTML = total
    isPaidCell.innerHTML = "UNPAID"
}

function openPayment() {
    document.getElementById('make-payment-box').style.display = 'flex';
}

function closePayment() {
    document.getElementById('make-payment-box').style.display = 'none';
}

// works with doubles, floats, ints, strings -Kyle
function toPriceString(amount) {
    return "$" + parseFloat(amount).toFixed(2)
}

function submitAppointmentsPaid() {
    const apptIDs = Array.from([...document.querySelectorAll("#payment-history > tbody > tr")].filter(apptEl => apptEl.querySelector(":scope > .paid-cell").innerHTML == "UNPAID"), apptEl => apptEl.id)
    console.log(apptIDs)
    let failureOccured = false
    apptIDs.forEach(id => {
        markAppointmentPaid(id, true).then(success => {
            console.log(id, success)
            if (success)
                setAppointmentPaid(id)
            else
                failureOccured = true
        })
    })
    if (failureOccured)
        showBigWarning("Sorry! An error occured when paying your appointment. Please try again later.", 3, "center")
}

function setAppointmentPaid(id) {
    console.log("marking", id)
    const apptEl = document.getElementById(id)
    const paidCell = apptEl.getElementsByClassName("paid-cell")[0] // querySelector doesnt work with the id for some reason
    let amountDue = apptEl.getElementsByClassName("total-cell")[0].innerHTML
    amountDue = (amountDue != " --- ") ? parseFloat(amountDue.substring(1)) : 0

    paidCell.innerHTML = "PAID"
    // changing total due
    setBilledAmount(getBilledAmount() - amountDue)
}

function submitPayment(event) {
    event.preventDefault()
    submitAppointmentsPaid()
    closePayment()
}