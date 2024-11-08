import { useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'
import SignInComponent from "../js/signInComponent"
import RegisterComponent from "../js/RegisterComponent"
import Landing from "../js/Landing"

function App() {
  const [count, setCount] = useState(0)

  return (
    <>
        <RegisterComponent/>
    </>
  )
}

export default App
