import React from "react";
import { SignInUi } from "../SignInUi/SignInUi";
import bar from "./bar.svg";
import icon from "./icon.svg";
import "./style.css";

export const ViewAllSignIn = () => {
  return (
    <div className="view-all-sign-in">
      <header className="title-bar">
        <div className="overlap">
          <div className="rectangle" />
          <img className="bar" alt="Bar" src={bar} />
          <h1 className="text-wrapper-7">Contact Us</h1>
          <button className="arrow-back" aria-label="Go back">
            <div className="overlap-group">
              <img className="icon" alt="Back Icon" src={icon} />
              <div className="frame-2" />
            </div>
          </button>
        </div>
      </header>
      <main className="div-2">
        <SignInUi className="sign-in-UI-instance" property1="sign-in" />
      </main>
    </div>
  );
};
