import PropTypes from "prop-types";
import React from "react";
import "./sign-in-UI.css";

export const SignInUi = ({ property1, className }) => {
  return (
    <div className={`sign-in-UI ${className}`}>
      <p className="don-t-have-an">
        <span className="text-wrapper">Don’t have an account yet?</span>

        <span className="span">&nbsp;</span>

        <span className="text-wrapper-2">Register Now</span>
      </p>

      <div className="div">Scheduled Smiles</div>

      <div className="input-field">
        <div className="input">
          <p className="p">
            <span className="text-wrapper-3">Password </span>

            <span className="text-wrapper-4">*</span>
          </p>
        </div>
      </div>

      <div className="email-wrapper">
        <p className="p">
          <span className="text-wrapper-3">Email </span>

          <span className="text-wrapper-5">*</span>
        </p>
      </div>

      <div className="frame" />

      <div className="forgot-your-password">
        <div className="text-wrapper-6">Forgot your password?</div>
      </div>

      <div className="generic-buttom">
        <div className="generic-button">Sign in</div>
      </div>

      <div className="generic-button-wrapper">
        <div className="generic-button-2">Admin</div>
      </div>
    </div>
  );
};

SignInUi.propTypes = {
  property1: PropTypes.oneOf(["sign-in"]),
};