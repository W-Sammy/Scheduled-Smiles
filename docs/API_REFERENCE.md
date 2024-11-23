# API Endpoint Documentation
This document details the structure of this project's Server API Endpoint, and some examples of what request structures are expected.\
\
&nbsp;&nbsp;&nbsp;&nbsp;*note from author: we use POST requests for most of these because we do __not__ want users __bookmarking__ our api endpoints. yes, GET request would work just as well in most of these situations.*

## General Operations with the Database
| Operation | Method | Endpoint | Body | Returns |
| :-- | :-: | :-: | :-- | :-- |
| Getting data<br>from the database | POST | /api/database/get | <pre>\{ "query": ... \}</pre> | <pre>\[<br>  \[row1col1, row1col2, ...\],<br>  \[row2col1, row2col2, ...\],<br>  ...<br>\]</pre>or<br><pre>\{ null \}</pre>
| Inserting data into<br>the database | POST | /api/database/send | <pre>\{ "query": ... \}</pre> | <pre>\{<br>  "updated": \[int\]<br>\}</pre> |

## Specialized Operations with the Database
Some operations are sensitive, or repetitve enough to warrant their specific, streamlined implementation in our API. As such, these endpoints may overlap in function with one another. They are as follows:\

| Operation | Method | Endpoint | Body | Returns |
| :-- | :-: | :-: | :-- | :-- |
| Checking if something exists | POST | /api/verify | <pre>\{ "query": ... \}</pre> | <pre>\{ \[true\|false\] \}</pre> |
| Registering a new user | POST | /api/register | <pre>\{<br>  "firstName": ...,<br>  "lastName": ...,<br>  "address": ...,<br>  "sex": ...,<br>  "phone": ...,<br>  "email": ...,<br>  "birthDate": ...,<br>  "password": ...<br>\}</pre> | <pre>\{ \[true\|false\] \}</pre> |
| Checking if a user's password matches | POST | /api/login | <pre>\{<br>  "email": ...,<br>  "password": ...<br>\} | <pre>\{<br>  "roleId": ...,<br>  "userId": ...,<br>  "firstName": ...,<br>  "lastName": ...,<br>  "address": ...,<br>  "sex": ...,<br>  "phone": ...,<br>  "email": ...,<br>  "birthDate": ...<br>\}</pre>or<br><pre>\{ false \}</pre> |
| Uploading an image (not an FR, likely won't implement) | PUT | /api/upload | Idk exactly how the body of a multipart submission is supposed to look like, I think it's a bytestream? | either HTTP response code 201 with the `Content-Location` header set to the newly uploaded file's location, or HTTP 204 if the file already exists, with `Content-Location` set to the existing file's location. IDK what to return if the upload fails due to insufficent disk space or something else, as the offical MDN docs (they define http semantics) don't specify that use case or what to do for it. |

## Additional Notes
- None of the endpoints other than getting raw data from the database should never return null- if a null value is returned outside of the specified circumstance, something went wrong server-side. Always return false to signify that the process was handled gracefully by the server. 
- All endpoints will return a 400 (bad request) HTTP response code if the formatting specified in the body sections of each table does not match.
- Bytes cannot be requested from the database directly, we use hex instead to return a byte representation.
- Some methods might be changed from POST to GET and back depending on syntax choices that come up during implementation.
- It would be a good idea (but not a totally necessary use of time) to implement some sort of verification / handshake flow that needs to be made before any api requests can be sent, but that's something that would need a decent amount of time (a full day or two) to implement server-side. (functionality for recognizing and storing clients or cookies)
- Uploading an image isn't one of our FRs, so it will likely not be implemented.