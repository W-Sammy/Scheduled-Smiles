import React, { useState } from 'react';
import "../css/InputBox.css"


const InputBox = () => {
    
    const [inputValue, setInputValue] = useState('');

    const handleChange = (event: any) => {
        setInputValue(event.target.value);
    };

    return (
        <div className="TextBox">
        <label htmlFor="email" className="label">
            Email <span className="required">*</span>
        </label>
        <input type="email" id="email" value={inputValue} onChange={handleChange} className="input" placeholder="sophie@example.com"/>

        Input : {inputValue}

        </div>
    )
}

export default InputBox