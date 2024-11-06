import React from "react";
import Header from "./components/Header";
import Navbar from "./components/Navbar";
import betterTeeth from "./assets/betterTeeth.png";
import "./App.css";

function App() {
    return (
        <div className="landingPage">
            <Header />
            <Navbar />
            <div className="betterTeethContainer">
                <img
                    src={betterTeeth}
                    className="betterTeethImg"
                    alt="better teeth, better smiles"
                />
                <a className="scheduleBtn">Scheduled Now</a>
            </div>
        </div>
    );
}

export default App;
