import PropTypes from "prop-types";
import React from "react";
import "./generic-buttom.css";

export const GenericButtom = ({
  property1,
  className,
  genericButtonClassName,
  text = "Sign in",
}) => {
  return (
    <div className={`generic-buttom ${className}`}>
      <div className={`generic-button ${genericButtonClassName}`}>{text}</div>
    </div>
  );
};

GenericButtom.propTypes = {
  property1: PropTypes.oneOf(["default"]),
  text: PropTypes.string,
};