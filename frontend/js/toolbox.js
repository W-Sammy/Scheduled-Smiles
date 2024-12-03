// These are features that were either never properly implemeted as css classes or something I was too lazy to write in html on every page. -Kyle
window.addEventListener("load", function(event) {
    addStylingToPage("./css/toolbox.css")
    loadLoadingIcons()
}, true)

// THis function was made out of laziness, imports stylesheet on all pages the script is on so I don't have to do it manually
function addStylingToPage(stylesheetUrl) {
    var link = document.createElement( "link" )
    link.href = stylesheetUrl
    link.type = "text/css"
    link.rel = "stylesheet"
    document.getElementsByTagName( "head" )[0].appendChild( link );
}

/* too lazy to write docstring sorry
    accepts:
        HTMLElement the parent element to append the loading icon INTO
    returns:
        [
            element,   HTMLElement the loading icon element
            close()    calling this handler function will remove the element from DOM
        ]
    BASICALLY, to use this-
    1. call function, take element and close handler
    2. append element to parent
    3. do stuff syncornously / things that take time
    4. call close handler
    5. profit
*/
function createLoadingIcon(parentEl = null) {
    /* HTML must match:
        <parent-element>
            <div></div>
            <div></div>
            <div></div>
            <div></div>
            <div></div>
            <div></div>
            <div></div>
            <div></div>
        </parent-element>
    */
    const el = document.createElement("div")
    const close = function() { el.remove() }
    el.classList.add("loading-icon")
    // insert 8 div children, css handles the rest
    for (let i  = 0; i < 8; i++) {
        el.appendChild(document.createElement("div"))
    }
    if (parentEl)
        parentEl.appendChild(el)
    return [el, close]
}

function loadLoadingIcons() {
    /* HTML must match:
        <parent-element>
            <div></div>
            <div></div>
            <div></div>
            <div></div>
            <div></div>
            <div></div>
            <div></div>
            <div></div>
        </parent-element>
    */
    for (let e of document.getElementsByClassName("loading-icon")) {
        e.innerHTML = "<div></div><div></div><div></div><div></div><div></div><div></div><div></div><div></div>" // insert 8 div children
    }
}

function fadeOut(element, seconds = 0.75) {
    element.style.transition = "opacity " + seconds + "s ease"
    element.style.opacity = 0
    setTimeout(function() {
        element.remove()
    }, seconds * 1000)
}

function createBasicBox(position = "top", margin = "10%") {
    const box = document.createElement("div")
    box.classList.add("notification-box")
    if (position)
        box.classList.add(...position.split(" "))
    if (margin)
        box.style.setProperty("--margin", margin)
    box.onclick = (e) => {
        fadeOut(box)
    }
    return box
}


// Display a NON-BLOCKING message to the user
// set duration to 0 seconds to make it last indefinitely
function showNotification(text, duration = 0, position = null, margin = null, parentElement = document.body) {
    duration = (duration != null) ? duration : 0
    position = (position != null) ? position : "top"
    margin = (margin != null) ? margin : "10%"
    const box = createBasicBox(position, margin)
    box.innerHTML = text
    // don't create dupes
    const boxes = document.getElementsByClassName("notification-box")
    for (i = 0; i < boxes.length; i++)
        if (boxes[i].outerHTML == box.outerHTML)
            return
    parentElement.appendChild(box)
    if (duration > 0)
        setTimeout(function() {
            fadeOut(box, duration / 5)
        }, (duration - (duration / 5)) * 1000)
    return box
}

function showBigNotification(text, duration = 0, position = null, margin = null, parentElement = document.body) {
    showNotification(text, duration, position, margin, parentElement).classList.add("big")
}

function showWarning(text, duration = 0, position = null, margin = null, parentElement = document.body) {
    showNotification(text, duration, position, margin, parentElement).classList.add("warn")
}

function showBigWarning(text, duration = 0, position = null, margin = null, parentElement = document.body) {
    showWarning(text, duration, position, margin, parentElement).classList.add("big")
}