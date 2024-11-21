# SQL FILES / DB USAGE DOCUMENT
**TABLE OF CONTENTS**
1. [Important Downloads](#ImportantDownloads)
2. [Quick Notes](#QuickNotes)
### Important Downloads 
<a name="ImportantDownloads"></a>

> Your own local server:  
>
>Windows:  
>[MySQL Community Server](https://dev.mysql.com/downloads/windows/installer/8.0.html)  
>
>Mac:  
>[MySQL Community Server](https://dev.mysql.com/downloads/mysql/)

> Provides functionalities to connect to, manage, and query a database server:  
>
>[SQLTools](https://marketplace.visualstudio.com/items?itemName=mtxr.sqltools)  

> Driver that allows you to connect to a MySQL database server:  
>  
>[SQLTools MySQL/MariaDB/TiDB](https://marketplace.visualstudio.com/items?itemName=mtxr.sqltools-driver-mysql)

---

### Quick Notes
<a name="QuickNotes"></a>
* Declared database and table names are all turned into pure lowercase characters  
  - i.e. scheduledSmiles -> scheduledsmiles  
* There are a few changes between the local/remote DB queries 
  - make sure you're running only running the local queries -- remote queries will throw an error.

### Process  
* Install [MySQL Server](https://dev.mysql.com/downloads/mysql/)
  ><u>**SELECT**</u>  
    Version: 8.0.40  
    Operating System: [...]  
  - Launch installer
  - Follow install instructions
  - Create root user (leave configurations as is for easy setup)
  - password: password  

      > (you can change this, but stay consistent)
* Create local DB  
  <u>***Windows***</u> 
  1. Open command prompt
  2. Type in:  
     > "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqld"
  <u>***Mac***</u>
  1. Open terminal 
  2. Type in:
     > /usr/local/mysql/bin/mysql -u root -p
  
  3. Enter password
  4. Run:
     > CREATE DATABASE scheduledSmiles;

* Install [SQLTools](https://marketplace.visualstudio.com/items?itemName=mtxr.sqltools) VS Code Extension 
* Install [SQLTools MySQL/MariaDB/TiDB](https://marketplace.visualstudio.com/items?itemName=mtxr.sqltools-driver-mysql) VS Code Extension  
  - Click SQLTools in the VS Code sidebar
  - Add New Connection  
    > <u>**REQUIRED FIELDS**</u>  
      Connect using: Server and Port  
      Connection name: localDB (or other, doesn't matter)
      Server Address: localhost  
      Port: 3306  
      Database: scheduledSmiles  
      Username: root



