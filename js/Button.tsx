import React from 'react'
import "../css/Button.css"


interface Props{
    children: string;

    //onClick: () => void;
}

const Button = ({children}: Props) => {
  return (
    <button className = "button1">{children}</button>
  )
}

export default Button