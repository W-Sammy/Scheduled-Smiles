import { useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'
import SignInComponent from "../js/signInComponent"
import Landing from "../js/Landing"

function App() {
  const [count, setCount] = useState(0)

  return (
    <>

      <div className="card"/>
        <button onClick={() => setCount((count) => count + 1)}>
          count is {count}
        </button>
        <SignInComponent/>
        <Landing/>

    </>
  )
}

export default App
