import React from "react";
import Header from "./Header";
import Navbar from "./Navbar";
import betterTeeth from "../assets/betterTeeth.png";
import "../css/Landing.css"

function Landing() {
    return (
        <>        
        <Header />
        <Navbar />
        <div className="landingPage">
            <div className="betterTeethContainer">
                <img
                    src={betterTeeth}
                    className="betterTeethImg"
                    alt="better teeth, better smiles"
                />
                <a className="scheduleBtn">Scheduled Now</a>
            </div>
        </div>
        </>
    );
}

export default Landing;
