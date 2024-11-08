import React from 'react'
import "../css/Button.css"


interface Props{
    display: string;
    buttonType: string;

    //onClick: () => void;
}

const Button = ({display,buttonType}: Props) => {

    if( buttonType == "submit"){
        return(
            <button type="submit" className = "genericButton">{display}</button>
        )
    }
    else{
        return(
            <button type="button" className = "genericButton">{display}</button>
        )
    }

}

export default Button