// Functions to interact with our API -Kyle


// JSDoc definitions
/**
 * A callback function to use after an API response is returned.
 * @callback apiCallback
 * @param {Boolean} True if the call was made successfully, and false otherwise. Note that this does not actually mean that the operation finished without error- only that the request was made successfully.
 * @param {String|null} A JSON formatted response body from the endpoint, or null if something went wrong/
 */

// Helpers
/**
 * Sends a query to the database.
 * @param {String} requestBody The body of the request to send to the endpoint.
 * @param {String} endpoint
 * @param {String} method
 * @returns {Promise<String|null>} The response from the server, or null if something went wrong.
 */
async function request(requestBody, endpoint, method) {
    return await fetch(window.location.origin + endpoint, {
        method: method,
        mode: "no-cors",
        body: JSON.stringify(JSON.parse(requestBody)),
        headers: {
            'Accept': 'application/json',
            "Content-type": "application/json; charset=UTF-8"
        }
    }).then(response => {
        return Promise.all([response.status, response.text()])
    }, networkError => {
        console.log(networkError.message)
        return null
    }).then((values) => {
        var responseStatus = values[0] < 400
        var responseText = (!responseStatus) ? null : JSON.stringify(JSON.parse(values[1]))
        return responseText
    })
}

/**
 * Converts a date string to UTC timestamp.
 * @param {String} dateString Ex: "2024-11-20"
 * @returns {Number} The UTC timestamp corrosponding to the given date.
 */
function dateStringToUtc(dateString) {
    const date = new Date(dateString + "Z")
    return Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), date.getUTCHours(), date.getUTCMinutes(), date.getUTCSeconds())
}

// Module functions
/**
 * Sends a query to the database.
 * @param {String} requestBody The body of the request to send to the endpoint.
 * @param {String} endpoint
 * @param {String} method
 * @param {apiCallback} callback The function to call with the result.
 * @returns {void}
 */
function callApi(requestBody, endpoint, method, callback) {
    fetch(window.location.origin + endpoint, {
        method: method,
        mode: "no-cors",
        body: JSON.stringify(JSON.parse(requestBody)),
        headers: {
            'Accept': 'application/json',
            "Content-type": "application/json; charset=UTF-8"
        }
    }).then(response => {
        return Promise.all([response.status, response.text()])
    }, networkError => {
        console.log(networkError.message)
        callback(false, null)
    }).then((values) => {
        var responseStatus = values[0] < 400
        var responseText = (!responseStatus) ? null : JSON.stringify(JSON.parse(values[1]))
        callback(responseStatus, responseText)
    })
}

/**
 * Returns if an account email already exists.
 * @param {String} email
 * @returns {Boolean} True if an account with the specified email can be found in the database, and false otherwise.
 */
async function accountEmailExists(email) {
    const requestBody = `{ "query": "SELECT 1 FROM users WHERE email='${email}'" }`
    const endpoint = "/api/verify"
    const method = "POST"
    const response = await request(requestBody, endpoint, method)
    return response === "true"
}

/**
 * Returns if an account ID already exists.
 * @param {String} id The hex representation of the user's ID.
 * @returns {Boolean} True if an account with the specified email can be found in the database, and false otherwise.
 */
async function accountIdExists(id) {
    const requestBody = `{ "query": "SELECT 1 FROM users WHERE userID=UNHEX('${id}')" }`
    const endpoint = "/api/verify"
    const method = "POST"
    const response = await request(requestBody, endpoint, method)
    return response === "true"
}

/**
 * Returns if the details of the account with the specified ID.
 * @param {String} id The hex representation of the user's ID.
 * @returns {String|null} A JSON formatted body of the account details, and null if something went wrong.
 */
async function getIdAccount(id) {
    const requestBody = `{ "userID": "${id}" }`
    const endpoint = "/api/login"
    const method = "POST"
    const response = await request(requestBody, endpoint, method)
    return (response !== false) ? response : null
}

/**
 * Returns if a given password for an email is correct.
 * @param {String} email
 * @param {String} password
 * @returns {Boolean} True if an account with the specified email can be found in the database and the password is correct- false otherwise.
 */
async function passwordCorrect(email, password) {
    const requestBody = `{ "email": "${email}", "password": "${password}" }`
    const endpoint = "/api/login"
    const method = "POST"
    const response = await request(requestBody, endpoint, method)
    return response !== "false"
}

/**
 * Returns account data for a given account.
 * @param {String} email
 * @param {String} password
 * @returns {String|null} The JSON formatted body of details from the given account, or null if the email and password do not match.
 */
async function getAccount(email, password) {
    const requestBody = `{ "email": "${email}", "password": "${password}" }`
    const endpoint = "/api/login"
    const method = "POST"
    let response = await request(requestBody, endpoint, method)
    return (response !== false) ? response : null
}

/**
 * Registers a new user.
 * @param {String} firstName The first name of the user. Must be within 35 characters.
 * @param {String} lastName The first name of the user. Must be within 35 characters.
 * @param {String} address The current mailing address of the user. Must be within 100 characters.
 * @param {('M'|'F')} sex The sex of the user. Must be "M" or "F"
 * @param {String} phone The phone number to register with the new account. Must be 10 characters long.
 * @param {String} email The current email address to contact the user. Must be within 100 characters.
 * @param {Number} birthDate The UTC formatted timestamp of the user's date of birth.
 * @param {String} password The password to use for the new account.
 * @returns {Boolean} True if the account was created successfully and false if something went wrong.
 */
async function createAccount(firstName, lastName, address, sex, phone, email, birthDate, password) {
    const requestBody = `{ "firstName": "${firstName}", "lastName": "${lastName}", "address": "${address}", "sex": "${sex}", "phone": "${phone}", "email": "${email}", "birthDate": ${birthDate}, "password": "${password}" }`
    const endpoint = "/api/register"
    const method = "POST"
    const response = await request(requestBody, endpoint, method)
    return response === "true"
}

/**
 * Returns role name for a given role ID.
 * @param {String} roleId The hex representation of the roleId.
 * @returns {String|null} A JSON formatted body containing the role name, or null if no role was foumd.
 */
async function getRoleName(roleId) {
    const requestBody = `{ "roleId": "${roleId}" }`
    const endpoint = "/api/lookup"
    const method = "POST"
    let response = await request(requestBody, endpoint, method)
    return (response !== false) ? response : null
}
// TODO: getAppointment and getChat/getMessages -Kyle