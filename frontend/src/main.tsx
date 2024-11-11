import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from "./App"
import SignInComponent from "../js/signInComponent"
import RegisterComponent from "../js/RegisterComponent"
import Landing from "../js/Landing"

createRoot(document.getElementById('root')!).render(
	<StrictMode>
		<Landing/>
	</StrictMode>,
)
