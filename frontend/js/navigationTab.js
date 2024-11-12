function newTab(){

    const para = document.createElement("div");
    const node = document.createTextNode("open tabby");
    para.appendChild(node);
    para.className="tab";
    
    const element = document.getElementById("navigation");
    element.appendChild(para);

}