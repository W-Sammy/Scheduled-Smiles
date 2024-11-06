import React from 'react'


interface props{
    children: string;

    //onClick: () => void;
}

const SubmitButton = ({children}: props) => {
    return(
        <button className = "button1">{children}</button>
    )
}

export default SubmitButton