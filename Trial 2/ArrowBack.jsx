import PropTypes from "prop-types";
import React from "react";
import icon from "../appuser/icon.svg";
import "./arrow-back.css";

export const ArrowBack = ({ property1, className }) => {
  return (
    <div className={`arrow-back ${className}`}>
      <div className="overlap-group">
        <img className="icon" alt="Icon" src={icon} />

        <div className="frame" />
      </div>
    </div>
  );
};

ArrowBack.propTypes = {
  property1: PropTypes.oneOf(["default"]),
};