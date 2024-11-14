# SQL FILES / DB USAGE DOCUMENT
**TABLE OF CONTENTS**
1. [Important Downloads](#ImportantDownloads)
2. [Quick Notes](#QuickNotes)
### Important Downloads 
<a name="ImportantDownloads"></a>

> Your own local server:  
>  
>[MySQL Server](https://dev.mysql.com/downloads/windows/installer/8.0.html)

> Provides functionalities to connect to, manage, and query a database server:  
>
>[SQLTools](https://marketplace.visualstudio.com/items?itemName=mtxr.sqltools)  

> Driver that allows you to connect to a MySQL database server:  
>  
>[SQLTools MySQL/MariaDB/TiDB](https://marketplace.visualstudio.com/items?itemName=mtxr.sqltools-driver-mysql)

---

### Quick Notes
<a name="QuickNotes"></a>
Declared database and table names are all turned into pure lowercase characters  
i.e. scheduledSmiles -> scheduledsmiles

### Process  
- Install [MySQL Server](https://dev.mysql.com/downloads/windows/installer/8.0.html)
  - Launch Installer
  - Follow instructions
  - Create root user (leave other configurations as is for easy setup)

- Install [SQLTools](https://marketplace.visualstudio.com/items?itemName=mtxr.sqltools) VS Code Extension 
- Install [SQLTools MySQL/MariaDB/TiDB](https://marketplace.visualstudio.com/items?itemName=mtxr.sqltools-driver-mysql) VS Code Extension  
  - Click SQLTools in the VS Code sidebar
  - Add New Connection
  > REQUIRED FIELDS
    Server Address: localhost  
    Port: 3306  
    Database: scheduledSmiles  
    Username: root



