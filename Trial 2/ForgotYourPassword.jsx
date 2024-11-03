import PropTypes from "prop-types";
import React from "react";
import "./forgot-your-password.css";

export const ForgotYourPassword = ({ property1, className }) => {
  return (
    <div className={`forgot-your-password ${className}`}>
      <div className="text-wrapper">Forgot your password?</div>
    </div>
  );
};

ForgotYourPassword.propTypes = {
  property1: PropTypes.oneOf(["default"]),
};