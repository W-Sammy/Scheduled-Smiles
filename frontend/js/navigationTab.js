function newTab(){

    const para = document.createElement("p");
    const node = document.createTextNode("");
    para.appendChild(node);
    
    const element = document.getElementById("div1");
    element.appendChild(para);

}