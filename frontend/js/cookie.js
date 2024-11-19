// Module for handing cookies, retaining login info. This should be imported and called at the start of every page. -Kyle
// TODO: tryCookie/isCookiesEnabled -Kyle

// TODO: JSDocumentation -Kyle
let newCookie = "domain=example.com";

// Helpers
function cookieStringToObject(cookieString) {
    return Object.fromEntries(Array.from(cookieString.split(";"), entry => entry.trim().split("=")))
}

// Module functions
export function getCookie() {
    return document.cookie
}

export function getCookieAsObject() {
    return cookieStringToObject(document.cookie)
}

export function setCookie(cookie) {
    document.cookie = cookie
}

export function setCookieFromObject(cookieObj) {
    let newCookie = ""
    for(const [key, value] of Object.entries(cookieObj)) {
        newCookie += `${key}=${value}`
        newCookie += "; "
    }
    document.cookie = newCookie.slice(0, -2); 
}

export function updateCookie(cookie) {
    const cookieObj = getCookieAsObject()
    const chipObj = cookieStringToObject(cookie)
    let newCookie = ""
    // Change keys that already exist
    for(const [key, value] of Object.entries(cookieObj)) {
        newCookie += `${key}=${(key in chipObj) ? chipObj[key] : value}`
        newCookie += "; "
    }
    // Add new keys that don't already exist
    for(const [key, value] of Object.entries(chipObj)) {
        if (!(key in cookieObj)) {
            newCookie += `${key}=${value}`
            newCookie += "; "
        }
    }
    document.cookie = newCookie.slice(0, -2);
}

export function updateCookieFromObject(cookieObj) {
    const oldCookieObj = getCookieAsObject()
    let newCookie = ""
    // Change keys that already exist
    for(const [key, value] of Object.entries(oldCookieObj)) {
        newCookie += `${key}=${(key in cookieObj) ? cookieObj[key] : value}`
        newCookie += "; "
    }
    // Add new keys that don't already exist
    for(const [key, value] of Object.entries(cookieObj)) {
        if (!(key in oldCookieObj)) {
            newCookie += `${key}=${value}`
            newCookie += "; "
        }
    }
    document.cookie = newCookie.slice(0, -2);
}