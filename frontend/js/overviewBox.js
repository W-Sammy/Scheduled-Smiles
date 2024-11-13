document.body.onload = addBox;


var box = document.getElementById("overview");


function addBox(){

    const para = document.createElement("div");
    const node = document.createTextNode("Test from another world");
    para.appendChild(node);
    

    box.appendChild(para);

}