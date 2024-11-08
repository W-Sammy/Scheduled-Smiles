import React, { useState } from 'react';
import "../css/RegisterComponent.css";
import Button from "./Button.tsx"
import HyperText from "./HyperText.tsx"

const RegisterComponent = () => {

    const [firstName, setFirstName] = useState("")
    const [lastName, setLastName] = useState("")
    const [email, setEmail] = useState("")
    const [password, setPassword] = useState("")
    const [phoneNumber, setPhoneNumber] = useState(0)
    const [address, setAddress] = useState("")
    const [birthday, setbirthday] = useState(0)

    
    
    return (
        <div>RegisterComponent</div>
    )
}

export default RegisterComponent