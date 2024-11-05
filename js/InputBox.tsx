import React from 'react'
import "../css/InputBox.css"

const InputBox = () => {
  return (
    <div className="TextBox">
    <label htmlFor="email" className="label">
      Email <span className="required">*</span>
    </label>
    <input type="email" id="email" className="input" placeholder="sophie@example.com"/>
  </div>
  )
}

export default InputBox