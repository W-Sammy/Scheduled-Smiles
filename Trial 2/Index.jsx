import React from "react";
import { ArrowBack } from "../ArrowBack/ArrowBack";
import { ForgotYourPassword } from "../ForgotYourPassword/ForgotYourPassword";
import { GenericButtom } from "../GenericButtom/GenericButtom";
import bar from "./bar.svg";
import "./style.css";

export const ViewAllSignIn = () => {
  return (
    <div className="view-all-sign-in">
      <div className="container">
        <div className="sign-in-ui">
          <img className="logo" alt="Logo" src={require("./frame-4.png")} />
          <h1 className="title">Scheduled Smiles</h1>

          <div className="input-field">
            <label htmlFor="email" className="label">
              Email <span className="required">*</span>
            </label>
            <input type="email" id="email" className="input" />
          </div>

          <div className="input-field">
            <label htmlFor="password" className="label">
              Password <span className="required">*</span>
            </label>
            <input type="password" id="password" className="input" />
          </div>

          <ForgotYourPassword className="forgot-password" property1="default" />

          <div className="button-group">
            <GenericButtom className="sign-in-button" property1="default" />
            <GenericButtom
              className="admin-button"
              property1="default"
              text="Admin"
            />
          </div>

          <p className="register-text">
            Don’t have an account yet? <a href="/register">Register Now</a>
          </p>
        </div>

        <div className="title-bar">
          <div className="overlap">
            <div className="rectangle" />
            <img className="bar" alt="Bar" src={bar} />
            <div className="contact-us">Contact Us</div>
            <ArrowBack className="arrow-back" property1="default" />
          </div>
        </div>
      </div>
    </div>
  );
};