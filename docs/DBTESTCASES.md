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

<table border=1>
<tr>
<td bgcolor=#75c5ff style='color:black'  class='medium'>userID</td>
<td bgcolor=#75c5ff style='color:black'  class='medium'>email</td>
<td bgcolor=#75c5ff style='color:black'  class='medium'>hashedPass</td>
<td bgcolor=#75c5ff style='color:black'  class='medium'>firstName</td>
<td bgcolor=#75c5ff style='color:black'  class='medium'>lastName</td>
<td bgcolor=#75c5ff style='color:black'  class='medium'>sex</td>
<td bgcolor=#75c5ff style='color:black'  class='medium'>birthDate</td>
<td bgcolor=#75c5ff style='color:black'  class='medium'>address</td>
<td bgcolor=#75c5ff style='color:black'  class='medium'>phoneNumber</td>
<td bgcolor=#75c5ff style='color:black'  class='medium'>roleID</td>
<td bgcolor=#75c5ff style='color:black'  class='medium'>detail</td>
</tr>

<tr>
<td class='normal' valign='top'>b'0000000000000000000000000000000000000000000000000000000000000000'</td>
<td class='normal' valign='top'></td>
<td class='normal' valign='top'>'00000000000000000000000000000000'</td>
<td class='normal' valign='top'></td>
<td class='normal' valign='top'></td>
<td class='normal' valign='top'></td>
<td class='normal' valign='top'>0</td>
<td class='normal' valign='top'></td>
<td class='normal' valign='top'></td>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'></td>
</tr>

<tr>
<td class='normal' valign='top'>b'0138ABFABB572A0CF7E12911AFD42B331F2ECBCD503A8D4DCCCF6DCD03B8A6C9'</td>
<td class='normal' valign='top'>"JaySohn@email.com"</td>
<td class='normal' valign='top'>'password732'</td>
<td class='normal' valign='top'>Jay</td>
<td class='normal' valign='top'>Sohn</td>
<td class='normal' valign='top'>M</td>
<td class='normal' valign='top'>948614400</td>
<td class='normal' valign='top'>123 Address Ct</td>
<td class='normal' valign='top'>9163597437</td>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'></td>
</tr>

<tr>
<td class='normal' valign='top'>b'32461ABCF37924ADF2CE88CB4E28442CE5C58162C45DA52A164E381A040A8A4B'</td>
<td class='normal' valign='top'>StephFu@scheduledsmiles.com</td>
<td class='normal' valign='top'>'password314'</td>
<td class='normal' valign='top'>Steph</td>
<td class='normal' valign='top'>Fu</td>
<td class='normal' valign='top'>F</td>
<td class='normal' valign='top'>567648000</td>
<td class='normal' valign='top'>456 Address Ave</td>
<td class='normal' valign='top'>9169678121</td>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'></td>
</tr>

<tr>
<td class='normal' valign='top'>b'358829A088D571941BE7D92ECFDDCADF920266E30E6951D7B2C00F705FBFA65C'</td>
<td class='normal' valign='top'>AnnieYeager@scheduledsmiles.adm.com</td>
<td class='normal' valign='top'>'password471'</td>
<td class='normal' valign='top'>Annie</td>
<td class='normal' valign='top'>Yeager</td>
<td class='normal' valign='top'>F</td>
<td class='normal' valign='top'>397526400</td>
<td class='normal' valign='top'>789 Address Dr</td>
<td class='normal' valign='top'>9167954329</td>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'></td>
</tr>

<tr>
<td class='normal' valign='top'>b'473DA3A7C1119A0C8756695D3AEB70654A6BAD1DF8CCD40E16FB208BFE07FD1C'</td>
<td class='normal' valign='top'>StewartFerris@scheduledsmiles.com</td>
<td class='normal' valign='top'>'password917'</td>
<td class='normal' valign='top'>Stewart</td>
<td class='normal' valign='top'>Ferris</td>
<td class='normal' valign='top'>M</td>
<td class='normal' valign='top'>905990400</td>
<td class='normal' valign='top'>456 Address Blvd</td>
<td class='normal' valign='top'>9160258429</td>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'></td>
</tr>

<tr>
<td class='normal' valign='top'>b'7EF40EA6757F4F849600FD5EBF72595A626AAF1B308B1BC40572B7E6E3580011'</td>
<td class='normal' valign='top'>JohnDoe@email.com</td>
<td class='normal' valign='top'>'password123'</td>
<td class='normal' valign='top'>John</td>
<td class='normal' valign='top'>Doe</td>
<td class='normal' valign='top'>M</td>
<td class='normal' valign='top'>759283200</td>
<td class='normal' valign='top'>123 Address Lane</td>
<td class='normal' valign='top'>1234567890</td>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'></td>
</tr>

<tr>
<td class='normal' valign='top'>b'A46BEF284705DA9528C48D2ACFEB293A8BDE6F94E3EFA0D1AFCED1FBEEC2FD6B'</td>
<td class='normal' valign='top'>AdamMinh@scheduledsmiles.adm.com</td>
<td class='normal' valign='top'>'password159'</td>
<td class='normal' valign='top'>Adam</td>
<td class='normal' valign='top'>Minh</td>
<td class='normal' valign='top'>M</td>
<td class='normal' valign='top'>169171200</td>
<td class='normal' valign='top'>789 Address Way</td>
<td class='normal' valign='top'>9166534124</td>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'></td>
</tr>

<tr>
<td class='normal' valign='top'>b'DB3516D8204BE8D4BB785DB155ACBC03D235B92904A35866C06BBC707177490E'</td>
<td class='normal' valign='top'>EliseFlossmore@scheduledsmiles.com</td>
<td class='normal' valign='top'>'password917'</td>
<td class='normal' valign='top'>Elise</td>
<td class='normal' valign='top'>Flossmore</td>
<td class='normal' valign='top'>F</td>
<td class='normal' valign='top'>774705600</td>
<td class='normal' valign='top'>456 Address Dr</td>
<td class='normal' valign='top'>9165592063</td>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'></td>
</tr>

<tr>
<td class='normal' valign='top'>b'F5B052CACF435704D410102783823EAC2A1ED39943BE0F94C0BD3AE0525511D3'</td>
<td class='normal' valign='top'>JaneDoe@email.com</td>
<td class='normal' valign='top'>'password456'</td>
<td class='normal' valign='top'>Jane</td>
<td class='normal' valign='top'>Doe</td>
<td class='normal' valign='top'>F</td>
<td class='normal' valign='top'>826761600</td>
<td class='normal' valign='top'>123 Address Lane</td>
<td class='normal' valign='top'>3141592654</td>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'></td>
</tr>
</table>

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

<table border=1>
<tr>
<td bgcolor=#75c5ff style='color:black'  class='medium'>staffID</td>
<td bgcolor=#75c5ff style='color:black'  class='medium'>hrlyWage</td>
</tr>

<tr>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'>80.00</td>
</tr>

<tr>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'>35.00</td>
</tr>

<tr>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'>80.00</td>
</tr>

<tr>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'>35.00</td>
</tr>

<tr>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'>80.00</td>
</tr>
</table>

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

<table border=1>
<tr>
<td bgcolor=#75c5ff style='color:black'  class='medium'>pairID</td>
<td bgcolor=#75c5ff style='color:black'  class='medium'>senderID</td>
<td bgcolor=#75c5ff style='color:black'  class='medium'>receiverID</td>
</tr>

<tr>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'>...</td>
</tr>

<tr>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'>...</td>
</tr>

<tr>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'>...</td>
</tr>

<tr>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'>...</td>
</tr>

<tr>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'>...</td>
</tr>

<tr>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'>...</td>
</tr>
</table>

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
>**[3] textContent**  
   Java: String  
   MySQL: TEXT 

<table border=1>
<tr>
<td bgcolor=#75c5ff style='color:black'  class='medium'>pairID</td>
<td bgcolor=#75c5ff style='color:black'  class='medium'>createdAt</td>
<td bgcolor=#75c5ff style='color:black'  class='medium'>textContent</td>
</tr>

<tr>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'>1732857672</td>
<td class='normal' valign='top'>MESSAGE2-1</td>
</tr>

<tr>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'>1732857672</td>
<td class='normal' valign='top'></td>
</tr>

<tr>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'>1732857707</td>
<td class='normal' valign='top'>RESPONSE2-1</td>
</tr>

<tr>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'>1732857672</td>
<td class='normal' valign='top'>MESSAGE1-1</td>
</tr>

<tr>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'>1732857693</td>
<td class='normal' valign='top'>MESSAGE1-2</td>
</tr>

<tr>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'>1732857672</td>
<td class='normal' valign='top'>MESSAGE3-1</td>
</tr>

<tr>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'>1732857672</td>
<td class='normal' valign='top'></td>
</tr>

<tr>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'>1732857689</td>
<td class='normal' valign='top'>RESPONSE1-1</td>
</tr>

<tr>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'>1732857707</td>
<td class='normal' valign='top'>RESPONSE1-2</td>
</tr>

<tr>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'>1732857672</td>
<td class='normal' valign='top'></td>
</tr>

<tr>
<td class='normal' valign='top'>...</td>
<td class='normal' valign='top'>1732857707</td>
<td class='normal' valign='top'>RESPONSE3-1</td>
</tr>
</table>
