// These are features that were either never properly implemeted as css classes or something I was too lazy to write in html on every page. -Kyle
const NOTIF_CLASS = "2w3rbanemtnqwERB434WnjewrwBV"
const WARN_CLASS = "ybuigoklnbuhioo8higbouhbnr9h"

function fadeOut(element, seconds = 0.75) {
    element.style.transition = "opacity " + seconds + "s ease"
    element.style.opacity = 0
    setTimeout(function() {
        element.remove()
    }, seconds * 1000)
}

function createBasicBox(position = "top", margin = "10%") {
    const box = document.createElement("div")
    box.style.position = "absolute"
    box.style.zIndex = "100" // max value is way higher
    box.style.borderRadius = "15px"
    box.style.borderWidth = "0.2em"
    box.style.borderStyle = " solid"
    box.style.boxSizing = "border-box"
    box.style.backgroundOrigin = "border-box"
    box.style.padding = "0.4em"
    box.style.textAlign = "center"
    box.style.minWidth = "fit-content"
    box.style.margin = "0 0 0 0"
    box.style.boxShadow = "none"
    // positioning
    let translateX = "-50"
    let translateY = "-50"
    box.style.top = "50%"
    box.style.left = "50%"
    const positions = position.split(" ")
    for (const i in positions) {
        switch (positions[i]) {
            case "top":
                box.style.top = margin
                box.style.bottom = "auto"
                translateY = "0"
            break;
            case "left":
                box.style.left = margin
                box.style.right = "auto"
                translateX = "0"
            break;
            case "bottom":
                box.style.bottom = margin
                box.style.top = "auto"
                translateY = "0"
            break;
            case "right":
                box.style.right = margin
                box.style.left = "auto"
                translateX = "0"
            break;
            case "center":
            default:
                box.style.top = "50%"
                box.style.left = "50%"
                translateX = "-50"
                translateY = "-50"
        }
    }
    box.style.transform = `translate(${translateX}%, ${translateY}%)`
    box.onclick = (e) => {
        fadeOut(box)
    }
    return box
}


// Display a NON-BLOCKING message to the user
// set duration to 0 seconds to make it last indefinitely
function showNotification(text, duration = 0, position = "top", margin = "10%", parentElement = document.body) {
    duration = (duration != null) ? duration : 0
    position = (position != null) ? position : "top"
    margin = (margin != null) ? margin : "10%"
    const box = createBasicBox(position, margin)
    box.classList.add(NOTIF_CLASS)
    box.innerHTML = text
    // apply styling
    box.style.backgroundColor = "#A6B9AD"
    box.style.color ="#FFFCEE"
    box.style.borderColor = "#2D6B6D"
    // remove dupes
    const boxes = document.getElementsByClassName(NOTIF_CLASS)
    for (i = 0; i < boxes.length; i++)
        if (boxes[i].outerHTML == box.outerHTML)
            return
    parentElement.appendChild(box)
    if (duration > 0)
        setTimeout(function() {
            fadeOut(box, duration / 5)
        }, (duration - (duration / 5)) * 1000)
}

function showBigNotification(text, duration = 0, position = "top", margin = "10%", parentElement = document.body) {
    duration = (duration != null) ? duration : 0
    position = (position != null) ? position : "top"
    margin = (margin != null) ? margin : "10%"
    const box = createBasicBox(position, margin)
    box.classList.add(NOTIF_CLASS)
    box.innerHTML = text
    // apply styling
    box.style.backgroundColor = "#A6B9AD"
    box.style.color ="#FFFCEE"
    box.style.borderColor = "#2D6B6D"
    box.style.fontSize = '1.7em'
    // remove dupes
    const boxes = document.getElementsByClassName(NOTIF_CLASS)
    for (i = 0; i < boxes.length; i++)
        if (boxes[i].outerHTML == box.outerHTML)
            return
    parentElement.appendChild(box)
    if (duration > 0)
        setTimeout(function() {
            fadeOut(box, duration / 5)
        }, (duration - (duration / 5)) * 1000)
}

function showWarning(text, duration = 0, position = "top", margin = "10%", parentElement = document.body) {
    duration = (duration != null) ? duration : 0
    position = (position != null) ? position : "top"
    margin = (margin != null) ? margin : "10%"
    const box = createBasicBox(position, margin)
    box.classList.add(WARN_CLASS)
    box.innerHTML = text
    // apply styling
    box.style.backgroundColor = "#FEBDB9"
    box.style.color ="#FFFCEE"
    box.style.borderColor = "#920700"
    // remove dupes
    const boxes = document.getElementsByClassName(WARN_CLASS)
    for (i = 0; i < boxes.length; i++)
        if (boxes[i].outerHTML == box.outerHTML)
            return
    parentElement.appendChild(box)
    if (duration > 0)
        setTimeout(function() {
            fadeOut(box, duration / 5)
        }, (duration - (duration / 5)) * 1000)
}

function showBigWarning(text, duration = 0, position = "top", margin = "10%", parentElement = document.body) {
    duration = (duration != null) ? duration : 0
    position = (position != null) ? position : "top"
    margin = (margin != null) ? margin : "10%"
    const box = createBasicBox(position, margin)
    box.classList.add(WARN_CLASS)
    box.innerHTML = text
    // apply styling
    box.style.padding = "1em"
    box.style.backgroundColor = "#FEBDB9"
    box.style.color ="#FFFCEE"
    box.style.borderColor = "#920700"
    box.style.fontWeight = "bolder"
    box.style.fontSize = "3em"
    // remove dupes
    const boxes = document.getElementsByClassName(WARN_CLASS)
    for (i = 0; i < boxes.length; i++)
        if (boxes[i].outerHTML == box.outerHTML)
            return
    parentElement.appendChild(box)
    if (duration > 0)
        setTimeout(function() {
            fadeOut(box, duration / 5)
        }, (duration - (duration / 5)) * 1000)
}