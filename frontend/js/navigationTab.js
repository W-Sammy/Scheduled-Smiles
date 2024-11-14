window.onload = addTab();

function addTab(){

    const para = document.createElement("span");
    const node = document.createTextNode("temp from another world and we're going");
    para.appendChild(node);
    para.style.width="20px";
    para.className ="tab";
    para.style.color="white";

    const element = document.getElementById("navigation");
    element.appendChild(para);


}