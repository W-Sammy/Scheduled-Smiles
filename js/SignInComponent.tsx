import React, { useState } from 'react';
import "../css/SignInComponent.css";
import Button from "./Button.tsx"
import HyperText from "./HyperText.tsx"

const SignInComponent = () => {

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

    const handleEmail = (event:any) => {
        event.preventDefault();
        setEmail(event.target.value);
    };

    const handlePassword = (event:any) =>{ 
        event.preventDefault();
        setPassword(event.target.value);

    };

    const handleSubmit = (event:any) => {
        event.preventDefault();
        console.log('Username:', email);
        console.log('Password:', password);
    };
    
    return (
    <>
        <form onSubmit={handleSubmit}>

            <div className="signIn">
                <img className='Logo' src="../assets/Logo.png" height="192" width="230"/>
                <h2 className='title'>Scheduled Smiles</h2>

                <div className='inputField'>
                    <label className='labels2'> Email :</label>
                    <input type="email" value={email} onChange={handleEmail} className="input" placeholder="Sophie@example.com" />
                    <br/>
                    <label className='labels'> Password : </label>
                    <input type="password" value={password} onChange={handlePassword} className="input" placeholder="Password" />
                </div>

                <div className='hyperlinkPassword'>
                    <HyperText link="forgotPasswordLink" display="Forgot your password?"/>
                </div>

                <div className="buttonSignIn">
                    <Button buttonType="submit" display="Sign-in2"/>
                </div>

                <div className="hyperlinkRegister">
                    Don't have an account yet? <HyperText display='Register Now' link='registerLink'/>
                </div>
            </div>

        </form>
    </>
    )
}

export default SignInComponent