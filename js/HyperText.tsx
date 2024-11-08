import React, { useState } from 'react';
import "../css/HyperText.css"

interface Props{
    display: string;
    link: string;
    //onClick: () => void;
}

const HyperText = ({display, link}: Props) => {
    return(
        <a className='link' href={link}>{display}</a>
    )
}

export default HyperText
