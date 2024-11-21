# DB TEST CASES DOCUMENT
### [tableName TABLE]
>**[columnNumber] columnName**  
   Java: JavaDataType  
   MySQL: MySQLDataType
>
>**[columnNumber] columnName**  
   Java: JavaDataType  
   MySQL: MySQLDataType
>
>...

| [columnNumber] columnName| [columnNumber] columnName| ... |
|:------------------------:|:------------------------:|-----|
| value                    | value                    | ... |

---

### [roleTypes TABLE]
>**[1] roleID**  
   Java: bytes[32]  
   MySQL: BYTES(32) - **SHA256(role)**
>
>**[2] role**  
   Java: String  
   MySQL: VARCHAR(10)  

| [1] roleID | [2] role   |
|:----------:|:-----------|
| ...        | "Staff"    |
| ...        | "Patient"  |
| ...        | "Admin"    |

---

### [users TABLE] 
>**[1] userID**  
   Java: bytes[32]  
   MySQL: BYTES(32) - **SHA256(email)**
>
>**[2] email**  
   Java: String  
   MySQL: VARCHAR(100)  
>
>**[3] hashedPass**  
   Java: bytes[32]  
   MySQL: BYTES(32) - **SHA256(password)**
>
>**[4] firstName**  
   Java: String  
   MySQL: VARCHAR(35)  
>
>**[5] lastName**  
   Java: String  
   MySQL: VARCHAR(35)
>
>**[6] sex**  
   Java: char  
   MySQL: CHAR
>
>**[7] birthDate**  
   Java: int  
   MySQL: INT
>
>**[8] address**  
   Java: String  
   MySQL: VARCHAR(100)  
>
>**[9] phoneNumber**  
   Java: String  
   MySQL: VARCHAR(10)  
>
>**[10] roleID**  
   Java: bytes[32]  
   MySQL: BYTES(32) - **SHA256(role)**  
>
>**[11] detail**  
   Java: String  
   MySQL: TEXT 

| [1] userID | [2] email                            | [3] hashedPass | [4] firstName | [5] lastName | [6] sex | [7] birthDate | [8] address        | [9] phoneNumber | [10] roleID | [11] detail |
|:----------:|:-------------------------------------|:--------------:|:--------------|:-------------|:-------:|:--------------|:-------------------|:----------------|:-----------:|:------------|
| ...        | "JaySohn@email.com"                  | ...            | "Jay"         | "Sohn"       | 'M'     | 948614400     | "123 Address Ct"   | "9163597437"    | ...         | ""          |
| ...        | "StephFu@scheduledsmiles.com"        | ...            | "Steph"       | "Fu"         | 'F'     | 567648000     | "456 Address Ave"  | "9169678121"    | ...         | ""          |
| ...        | "AdamMinh@scheduledsmiles.com"       | ...            | "Adam"        | "Minh"       | 'M'     | 169171200     | "789 Address Way"  | "9166534124"    | ...         | ""          |
| ...        | "StewartFerris@scheduledsmiles.com"  | ...            | "Stewart"     | "Ferris"     | 'M'     | 905990400     | "456 Address Blvd" | "9160258429"    | ...         | ""          |
| ...        | "JohnDoe@email.com "                 | ...            | "John"        | "Doe"        | 'M'     | 759283200     | "123 Address Lane" | "1234567890"    | ...         | ""          |
| ...        | "AnnieYeager@scheduledsmiles.com"    | ...            | "Annie"       | "Yeager"     | 'F'     | 397526400     | "789 Address Dr"   | "9167954329"    | ...         | ""          |
| ...        | "EliseFlossmore@scheduledsmiles.com" | ...            | "Elise"       | "Flossmore"  | 'F'     | 774705600     | "456 Address Dr"   | "9165592063"    | ...         | ""          |
| ...        | "JaneDoe@email.com"                  | ...            | "Jane"        | "Doe"        | 'F'     | 826761600     | "123 Address Lane" | "3141592654"    | ...         | ""          |

---

### [staff TABLE] 
>**[1] staffID**  
   Java: bytes[32]  
   MySQL: BYTES(32) - **SHA256(email)**
>
>**[2] hrlyWage**  
   Java: double  
   MySQL: DECIMAL(10, 2)  

 | [1] staffID | [2] hrlyWage |
 |:-----------:|:-------------|
 | ...         | 88.75        |
 | ...         | 34.50        |  
 | ...         | 55.75        |

---

### [appointments TABLE] 
>**[1] appointmentID**  
   Java: bytes[32]  
   MySQL: BYTES(32) - **SHA256('Appointment' + uniqueID())**
> 
>**[2] patientID**  
   Java: bytes[32]  
   MySQL: BYTES(32) - **SHA256(email)**
>
>**[3] startTime**  
   Java: int
   MySQL: INT
>
>**[4] staff1ID**  
   Java: bytes[32]  
   MySQL: BYTES(32) - **SHA256(email)**
>
>**[5] staff2ID**  
   Java: bytes[32]  
   MySQL: BYTES(32) - **SHA256(email)**
>
>**[6] staff3ID**  
   Java: bytes[32]  
   MySQL: BYTES(32) - **SHA256(email)**
>
>**[7] isCanceled**  
   Java: boolean  
   MySQL: TINYINT

| [1] appointmentID | [2] patientID | [3] startTime | [4] staff1ID | [5] staff2ID | [6] staff3ID | [7] isCanceled |
|:-----------------:|:-------------:|:--------------|:------------:|:------------:|:------------:|:--------------:|
| ...               | ...           | 1731092400    | ...          | NULL         | NULL         | 1              |
| ...               | ...           | 1731087000    | ...          | NULL         | NULL         | 0              |
| ...               | ...           | 1731090600    | ...          | NULL         | NULL         | 0              |
| ...               | ...           | 1732294800    | ...          | ...          | ...          | 0              |
| ...               | ...           | 1731690000    | ...          | ...          | NULL         | 0              |
| ...               | ...           | 1731693600    | ...          | NULL         | NULL         | 0              |

---

### [appointmentTypeKey TABLE] 
>**[1] typeID**  
   Java: bytes[32]  
   MySQL: BYTES(32) - **SHA256(appointmentType)**
> 
>**[2] appointmentType**  
   Java: String
   MySQL: VARCHAR(50)
>
>**[3] cost**  
   Java: double
   MySQL: DECIMAL(10, 2)

| [1] typeID | [2] appointmentType | [3] cost |
|:----------:|:--------------------|:---------|
| ...        | "Cleaning"          | 250.00   |
| ...        | "Checkup"           | 200.00   |
| ...        | "Emergency"         | 750.00   |
| ...        | "X-Ray"             | 200.00   |
| ...        | "Filling"           | 300.00   |

### [appointmentTypes TABLE] 
>**[1] appointmentID**  
   Java: bytes[32]  
   MySQL: BYTES(32) - **SHA256('Appointment' + uniqueID())**
>
>**[2] typeID**  
   Java: bytes[32]  
   MySQL: BYTES(32) - **SHA256(appointmentType)**

| [1] appointmentID | [2] typeID |
|:-----------------:|:----------:|
| ...               | ...        |
| ...               | ...        |
| ...               | ...        |
| ...               | ...        |
| ...               | ...        |
| ...               | ...        |
| ...               | ...        |
| ...               | ...        |
| ...               | ...        |
| ...               | ...        |
| ...               | ...        |

---

### [messagePairTypes TABLE] 
>**[1] pairID**  
   Java: bytes[32]  
   MySQL: BYTES(32) - **SHA256(email1 + email2)**
>
>**[2] senderID**  
   Java: bytes[32]  
   MySQL: BYTES(32) - **SHA256(email)**
>
>**[3] receiverID**  
   Java: bytes[32]  
   MySQL: BYTES(32) - **SHA256(email)**

| [1] pairID | [2] senderID | [3] receiverID |
|:----------:|:------------:|:--------------:|
| ...        | ...          | ...            |
| ...        | ...          | ...            |
| ...        | ...          | ...            |
| ...        | ...          | ...            |
| ...        | ...          | ...            |
| ...        | ...          | ...            |

---

### [messagePairTypes TABLE] 
>**[1] pairID**  
   Java: bytes[32]  
   MySQL: BYTES(32) - **SHA256(email1 + email2)**
>
>**[2] createdAt**  
   Java: int
   MySQL: INT
>
>**[3] detail**  
   Java: String  
   MySQL: TEXT 

| [1] pairID | [2] createdAt | [3] textContent |
|:----------:|:-------------:|:---------------:|
| ...        | 1730997822    | "MESSAGE1-1"    |
| ...        | 1731089421    | "MESSAGE1-2"    |
| ...        | 1731011237    | "MESSAGE3-1"    |
| ...        | 1731078958    | "RESPONSE1-1"   |
| ...        | 1731424957    | "RESPONSE1-2"   |
| ...        | 1731396303    | "MESSAGE2-1"    |
| ...        | 1731455717    | "RESPONSE2-1"   |
| ...        | 1731477812    | "RESPONSE3-1"   |
