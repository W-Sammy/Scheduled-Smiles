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
        var responseText = (!responseStatus) ? null : JSON.stringify(JSON.parse(values[1]), null, 2)
        return responseText
    })
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
export function callApi(requestBody, endpoint, method, callback) {
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
        var responseText = (!responseStatus) ? null : JSON.stringify(JSON.parse(values[1]), null, 2)
        callback(responseStatus, responseText)
    })
}

/**
 * Returns if an account email already exists.
 * @param {String} email
 * @returns {Boolean} True if an account with the specified email can be found in the database, and false otherwise.
 */
export async function accountExists(email) {
    const requestBody = `{ query: "SELECT 1 WHERE email='${email}'" }`
    const endpoint = "/api/verify"
    const method = "POST"
    const response = await request(requestBody, endpoint, method)
    return response === "true"
}

/**
 * Returns if a given password for an email is correct.
 * @param {String} email
 * @param {String} password
 * @returns {Boolean} True if an account with the specified email can be found in the database and the password is correct- false otherwise.
 */
export async function passwordCorrect(email, password) {
    const requestBody = `{ email: "${email}", password: "${password}" }`
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
export async function getAccount(email, password) {
    const requestBody = `{ email: "${email}", password: "${password}" }`
    const endpoint = "/api/login"
    const method = "POST"
    const response = await request(requestBody, endpoint, method)
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
export async function createAccount(firstName, lastName, address, sex, phone, email, birthDate, password) {
    const requestBody = `{ firstName: "${firstName}", lastName: "${lastName}", address: "${address}", sex: "${sex}", phone: "${phone}", email: "${email}", birthDate: "${birthDate}", password: "${password}",  }`
    const endpoint = "/api/register"
    const method = "POST"
    const response = await request(requestBody, endpoint, method)
    return response === "true"
}
// TODO: getAppointment and getChat/getMessages -Kyle