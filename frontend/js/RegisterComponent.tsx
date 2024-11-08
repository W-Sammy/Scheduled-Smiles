import React, { useState } from 'react';
import "../css/RegisterComponent.css";
import Button from "./Button.tsx"
import HyperText from "./HyperText.tsx"


const handleChange = (initial:any) => {
        const [value, setValue] = React.useState(initial);
        const handleChange = React.useCallback(
            (event:any) => setValue(event.target.value), // This is the meaty part.
            []
        );
    return [value, handleChange];
}

const RegisterComponent = () => {

    const [firstName, setFirstName] = handleChange("")
    const [lastName, setLastName] = handleChange("")
    const [email, setEmail] = handleChange("")
    const [password, setPassword] = handleChange("")
    const [phoneNumber, setPhoneNumber] = handleChange(0)
    const [address, setAddress] = handleChange("")
    const [birthday, setbirthday] = handleChange(0)

    const handleSubmit = (event:any) => {
        event.preventDefault();
        console.log('First Name:', firstName);
        console.log('Last Name:', lastName);
        console.log('Username:', email);
        console.log('Password:', password);
        console.log('Phone Number:', phoneNumber);
        console.log('Address:', address);
        console.log('Birthday:', birthday);
    };


    
    return (
        <form onSubmit={handleSubmit}>

        <div className="Register">
            <div className="hyperlinkBack">
                <HyperText display='Back' link='backLink'/>
            </div>
            <h2 className='title'>Scheduled Smiles </h2>
            <h2 className='title'>Create your new Account!</h2>

            <div className='inputField'>
                <input type="text" value={firstName} onChange={setFirstName} className="input" placeholder="First Name" />
                <input type="text" value={lastName} onChange={setLastName} className="input" placeholder="Last Name" />
                <input type="email" value={email} onChange={setEmail} className="input" placeholder="Email" />

                <div>Minimal 8 characters: (a-z)(0-9)<br/></div>
                <input type="password" value={password} onChange={setPassword} className="input2" placeholder="Password" />
                <input type="text" value={phoneNumber} onChange={setPhoneNumber} className="input2" placeholder="Phone Number: (123-4567)" />
                <input type="text" value={address} onChange={setAddress} className="input2" placeholder="Address" />
                <input type="date" value={birthday} onChange={setbirthday} className="input2" placeholder="Date of Birth" />
            </div>

            <div className="buttonRegister">
                <Button buttonType="submit" display="Register"/>
            </div>
            
        </div>

        </form>
    )
}

export default RegisterComponent