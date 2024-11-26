// Module for handing cookies, retaining login info. This should be imported and called at the start of every page. -Kyle
// TODO: tryCookie/isCookiesEnabled -Kyle

// TODO: JSDocumentation -Kyle

// Global values
// Domain value does NOT work for localhost. See https://curl.se/rfc/cookie_spec.html
// Domains that do not have at least two "." are considered invalid. This means cookies with domains set to "localhost:port" will be silently ignored. -Kyle
const domainCookieString = (window.location.host.split(".").length - 1 < 2) ? null : `domain=${window.location.host}`

// Module functions
/**
 * Converts a cookie string into an Object mapped by key/value pairs.
 * @param {String} cookieString The cookie.
 * @returns {Object} An object with attribute values matching the cookie key/value pairs.
 */
function cookieStringToObject(cookieString) { // sorry dann and keav, this dont look so good -Kyle
    return Object.fromEntries(Array.from(cookieString.split(";"), entry => {
        let [key, value] = entry.trim().split("=")
        return [key, decodeURIComponent(value)]
    }))
}

/**
 * Creates a cookie for the hostname root and all subdomains.
 * @param {String} key The key of the cookie to create.
 * @param {String} value The value of the cookie to create.
 * @param {Number} [maxAge=600] The maximum lifetime of the cookie, in seconds.
 * @returns {void}
 */
function setCookie(key, value, maxAge = 600) {
    const cookieArray = [
        `${key}=${encodeURIComponent(value)}`,
        "path=/",
        (maxAge != null && maxAge > 0) ? `max-age=${maxAge}` : null, // in seconds, for once lol
        domainCookieString
    ]
    const cookieStr = cookieArray.filter(e => e != null && e.length >= 3).join("; ")
    document.cookie = cookieStr
}

/**
 * Clears a cookie's value in the hostname root and all subdomains.
 * @param {String} key The key of the cookie to clear.
 * @returns {void}
 */
function clearCookie(key) {
    const cookieArray = [
        `${key}=`,
        "path=/",
        domainCookieString
    ]
    const cookieStr = cookieArray.filter(e => e != null).join("; ")
    document.cookie = cookieStr
}

/**
 * Deletes a cookie from the hostname root and all subdomains.
 * @param {String} key The key of the cookie to delete.
 * @returns {void}
 */
function deleteCookie(key) {
    const cookieArray = [
        `${key}=`,
        "expires=Thu, 01 Jan 1970 00:00:00 UTC", // the beginning of time...
        "path=/",
        domainCookieString
    ]
    const cookieStr = cookieArray.filter(e => e != null).join("; ")
    document.cookie = cookieStr
}

/**
 * Deletes all cookies from the hostname root and all subdomains.
 * @returns {void}
 */
function deleteAllCookies() {
    Object.keys(getCookieAsObject()).forEach(key => {
        deleteCookie(key)
    })
}

/**
 * Checks if a cookie exists in the current session.
 * @param {String} key The key of the cookie to look for.
 * @returns {Boolean} True if a cookie with the given key exists, and false otherwise.
 */
function checkCookieExists(key) {
    return document.cookie.split(";").some(e => e.trim().startsWith(key+"="))
}

/**
 * Returns the value of a cookie with the given key.
 * @param {String} key The key of the cookie to look for.
 * @returns {String|null} The value of the cookie, or null if no cookie with the given key was found.
 */
function getCookieValue(key) {
    return (checkCookieExists(key)) ? document.cookie.split(";").filter(e => e.trim().startsWith(key+"="))[0].trim().substring(key.length+1) : null
}

/**
 * Returns the cookies from the current session as an Object mapped by key/value pairs.
 * @returns {Object} An object with attribute values matching the cookie key/value pairs.
 */
function getCookieAsObject() {
    return cookieStringToObject(document.cookie)
}

/**
 * Updates the cookies for the current hostname root and all subdomains with the key/value pairs found in the given Object entries.
 * If a cookie exists in the Object but not in the cookie, a new cookie is created.
 * @param {Object} cookieObj An object with attribute values matching cookie key/value pairs.
 * @param {Number} [maxAge=600] The maximum lifetime of the cookies created from the object, in seconds.
 * @returns {void}
 */
function setCookieFromObject(cookieObj, maxAge = 600) {
    for(const [key, value] of Object.entries(cookieObj)) {
        setCookie(key, value, maxAge)
    }
}