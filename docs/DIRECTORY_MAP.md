# Directory Map

This document provides a visual explaination of the organization of files and folders in the Scheduled Smiles repository.

## Scheduled Smiles Directory Structure

```plaintext
📦 Root Directory
├─ .gitignore								        	     	    #
├─ .vscode									        	     	    #
│  └─ settings.json					    		       	 	  #
├─ LICENSE									       	       	    # The license under which the project is shared
├─ README.md								               	  	# Repository description	
├─ backend								                			# Main backend source code for the application
│  ├─ Main.java								             	  	# Entry point for the backend java program
│  ├─ Server									            	    # Package directory to run the server
│  │  ├─ .test-pages						          	  	# Package for server testing
│  │  │  ├─ index.html						        	  	# HTML Testing
│  │  │  ├─ main.js								          	  # JavaScript Testing
│  │  │  └─ styles.css						          		# CSS Testing
│  │  ├─ DatabaseConnection.java		    	  		# API to connect database to backend
│  │  ├─ Enum									             	    # Server package related enumerations
│  │  │  ├─ HttpConstants.java			    	    	# Mappings of STATUS_CODES, METHODS, HEADER_KEYS, HEADER_TYPES
│  │  │  └─ Pages.java						            	# Mappings of file names -> file type
│  │  ├─ Server.java					          	    	# Facilitator of frontend to backend connection 
│  │  ├─ lib									                  # Package directory for jar files
│  │  │  ├─ gson-2.11.0.jar						          # 
│  │  │  └─ mysql-connector-j-9.1.0.jar		    	# 
│  │  └─ utils								                	# Package directory for project utilies
│  │     ├─ DatabaseGenericParameter.java	    	# 
│  │     ├─ FileHandler.java					          # 
│  │     ├─ Json.java							              #	
│  │     ├─ Populate.java						            #
│  │     ├─ Requests.java						            #
│  │     └─ ServerConnectionHandler.java	    	#
│  └─ Users										                  # Package directory for user content
│     ├─ Admin.java								              # Admin-role specific attributes
│     ├─ Appointment.java						            # 
│     ├─ Chat.java								              #
│     ├─ Enum								                   	# User package related enumerations
│     │  ├─ AppointmentType.java				        # Mapping of treatment -> cost 
│     │  └─ RoleConstant.java					          # Mapping of ROLE_IDS and ROLE_DOMAINS
│     ├─ Message.java							              #
│     ├─ Patient.java							              #
│     ├─ Staff.java								              #
│     └─ User.java								              #
├─ docs											                    # Package directory for project documentation 
│  ├─ API_REFERENCE.md							            #
│  ├─ DatabaseConnection.md					      	    #
│  ├─ Scheduled Smiles Presentation.pdf			    #
│  ├─ Server.md									                #
│  ├─ Software Design Document.pdf			      	#	
│  └─ Software Requirements Specifications.pdf	#
└─ frontend										                  # Main backend source code for the application
   ├─ adminpayroll.html							            # Admin-exclusive view of payroll page
   ├─ assets									                  # Package directory for project assets
   │  ├─ betterTeeth.png					             	#
   │  ├─ bg_photo.png							              #
   │  ├─ contact.png							              #
   │  ├─ icons									                # Package directory for favicon
   │  │  ├─ android-chrome-192x192.png		  	  #
   │  │  ├─ android-chrome-512x512.png		    	#
   │  │  ├─ apple-touch-icon.png				        #
   │  │  ├─ favicon-16x16.png					          #
   │  │  ├─ favicon-32x32.png					          #
   │  │  └─ favicon.ico							            #
   │  ├─ landingPage_bg.png						#
   │  └─ logo.png								#
   ├─ billing.html								# Patient-exclusive billing page
   ├─ calendar.html								# User calendar page
   ├─ chat.html									# User chat page
   ├─ css										# Package directory for site CSS  
   │  ├─ Header.css								#		
   │  ├─ Landing.css							#	
   │  ├─ Navbar.css								#
   │  ├─ billing.css							#
   │  ├─ button.css								#
   │  ├─ calendar.css							#		
   │  ├─ chat.css								#
   │  ├─ dashboard.css							#	
   │  ├─ patientBilling.css						#
   │  ├─ payment-history.css					#	
   │  ├─ payment.css							#
   │  ├─ payroll.css							#
   │  ├─ session.css							#
   │  └─ signIn.css								#	
   ├─ dashboard.html							# User dashboard page
   ├─ history.html								# User appointment history page
   ├─ index.html								# Site landing page
   ├─ js										# Package directory for JavaScript
   │  ├─ Header.js								#				
   │  ├─ Navbar.js								#
   │  ├─ api.js									#
   │  ├─ calendar.js							#
   │  ├─ chat.js								#
   │  ├─ cookie.js								#
   │  ├─ navigationTab.js						#
   │  ├─ overviewBox.js							#
   │  ├─ session.js								#
   │  ├─ signIn.js								#
   │  ├─ toolbox.js								#
   │  └─ utils.js								#
   ├─ payment.html								# Patient-exclusive payment page
   ├─ session.html								# User appointments list page
   └─ staffpayroll.html							# Staff-exclusive payroll page
```


`Last updated: 11/29/2024`
