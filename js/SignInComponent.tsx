import React, { useState } from 'react';
import "../css/SignInComponent.css";
import Button from "./Button.tsx"

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
    

    return (<>
    
        <div>Scheduled Smiles Sign in box</div> 

        <form onSubmit={handleSubmit}>

            <div className="SignIn">
                <img className='Logo' src="../assets/Logo.png" height="247" width="300"/>
                <h2 className='title'>Scheduled Smiles Sign in box</h2>

                <div className='inputfield'>
                    <label className='labels'> Email : </label>
                    <input type="email" value={email} onChange={handleEmail} className="input" placeholder="sophie@example.com" />
                    <br/>
                    <label className='labels'> Password : </label>
                    <input type="password" value={password} onChange={handlePassword} className="input" placeholder="password" />
                </div>

                <div className="buttonsign"> 
                    <Button children="Sign-in2"/>
                    <Button>Sign-in3</Button>

                </div>
            </div>
            <Button>Sign-in</Button>


            <div>
            Input : {email}
            password : {password}
            </div>

        </form>
        </>
    )
}

export default SignInComponent