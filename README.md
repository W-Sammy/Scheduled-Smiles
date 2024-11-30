
# <a name="overview"></a>**Scheduled Smiles**

**Scheduled Smiles** is a **Dental Clinic Management System (DCMS**) created by a group of students from California State University-Sacramento. The purpose of this application is to provide dental clinics a centralized system to manage patient records, appointment scheduling, and treatment history. This project aims to develop a system that efficiently manages clinic operations and enhance patient care. 

The focus of the system is to streamline the management of  patient appointments, dental treatments, and staff work hours, providing the clinic with a comprehensive platform for managing clinic activities while ensuring accurate payroll for dental professionals. 

## Table of Contents

 * <a href="#overview">Overview</a>
 * <a href="#structure">Structure</a>
 * <a href="#doc">Documentation & Diagrams</a>
 * <a href="#instruct">Instructions For Use</a>
 * <a href="#contributors">Contributors</a>

## <a id="structure"></a>**Structure**

> This application is built with the intention that the User is utilizing it through a desktop screen. <br>
> Content for the application is structured around that of a web application and accessed through a search engine. <br>
> *Note: It is recommended to access this application through WindowsOS and Chromium*

![Landing Page](/frontend/assets/LandingPageScreen.png)

## <a id="doc"></a>**Documentation & Diagrams**

> All documentation relating to the project can be found through the file path: </br>
> `./Root/docs`</br>
> Primary documentation include: 
> * <a href="/docs/Software Requirements Specifications.pdf">Software Requirement Specification</a>
> * <a href="/docs/Software Design Document.pdf">Software Design Document</a> </br>
> 
> Guidelines and manuals for certain aspect of the source code can also be found here. 

### Database Schema

![Database Schema](/frontend/assets/Database%20Schema.png)

### UML Diagram 

![UML Diagram](/frontend/assets/UML%20Diagram.drawio.png)

## <a id="instruct"></a>**Instructions For Use**

To operate the application, the following procedures will need to be performed: </br>

  1. **Clone or download the repository from GitHub onto local machine** </br>
>  ![Clone](/frontend/assets/CloneRepo.png)
>
  2. **Add dependencies to your compiler**

> In VSCode this can be done by navigating to the bottom of the "Explorer" tab  
> Under `Java Project > Referenced Libraries > Add Jar libraries to Project Classpath`  
> From File Explorer: `backend > Server > lib`  
> Select both `gson` & `mysql-connector` and verified they have been added

  3. **Run the Main method**
>
> Navigate to `backend > Main.java` </br>
> Run `Main.java`

  3. **Cmd Prompt (Alternative)**
> Open `cmd` prompt and verify you are running from the root classpath of Scheduled Smiles
> Paste & enter 
```
java -cp ./backend/;./backend/Server/lib/gson-2.11.0.jar;./backend/Server/lib/mysql-connector-j-9.1.0.jar ./backend/Main.java
```
  4. **Connect to Server**
>
> On successful run of `Main.java`, connect to the server via link from terminal 
>
> ![Successful Terminal Connection](/frontend/assets/TerminalConnection.png)

## <a id="contributors"></a>**Contributors**

Below is a list of all original contributors to the project with their respective roles and contacts

| Contributor | Role | GitHub |
| :-----------: | :---: | :--: |
| **Sammy Wong** | *Scrum Master/Product Owner* | [W-Sammy](https://github.com/W-Sammy)
| **Kyle Tran** | *Full-Stack Developer* | [scritty1249](https://github.com/scritty1249)
| **Keav'n Lor** | *Full-Stack Developer* | [Keavn-CSU](https://github.com/Keavn-CSU)
| **Dann Manganti** | *Lead Frontend Developer* | [dmanganti](https://github.com/dmanganti)
| **Kaylina Kwong** | *Lead Backend Developer* | [mooneity](https://github.com/mooneity)
| **John Vue** | *Backend Developer* | [cscjohn](https://github.com/cscjohn)
| **Brandon Casey** | *Backend Developer* | [Zon-Vorelle](https://github.com/Zon-Vorelle)
| **Erds Ferdi Mabilog** | *Database Developer* | [erdsmabilog](https://github.com/erdsmabilog)
