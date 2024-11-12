
var box = document.getElementById("overview");


function newBox(){

    const para = document.createElement("div");
    const node = document.createTextNode("Test from another world");
    para.appendChild(node);
    

    box.appendChild(para);

}