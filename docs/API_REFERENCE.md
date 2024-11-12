# API Endpoint Documentation
This document details the structure of this project's Server API Endpoint, and some examples of what request structures are expected.\
\
&nbsp;&nbsp;&nbsp;&nbsp;*note from author: we use POST requests for most of these because we do __not__ want users __bookmarking__ our api endpoints. yes, GET request would work just as well in most of these situations.*

## General Operations with the Database
| Operation | Method | Endpoint | Body | Returns |
| :-- | :-: | :-: | :-- | :-: |
| Getting data<br>from the database | POST | /api/database/get | <pre>\{<br>  "query": ...,<br>  "type": \["string"\|"hex"\|"integer"\|"boolean"\] <br>\}</pre> | A JSON-formatted list of string lists, in order corrosponding the queries that were requested. Strings will be of the requested `type` specified for each query.<br>(string, hex-byte representation, integer, boolean)
| Inserting data into<br>the database | POST | /api/database/send | <pre>\{ "query": ... \}</pre> | A JSON-formatted body containing `true` if the row was succesfully populated in the database, and `false` otherwise. |

## Specialized Operations with the Database
Some operations are sensitive, or repetitve enough to warrant their specific, streamlined implementation in our API. As such, these endpoints may overlap in function with one another. They are as follows:
<br>
| Operation | Method | Endpoint | Body | Returns |
| :-- | :-: | :-: | :-- | :-: |
| Checking if something exists | GET | /api/verify | <pre>\{ "query": ... \}</pre> | A JSON-formatted body containing `true` if any results are found matching the given `query`, and `false` otherwise. |
| Registering a new user | POST | /api/register | <pre>\{<br>  "firstName": ...,<br>  "lastName": ...,<br>  "address": ...,<br>  "sex": ...,<br>  "phone": ...,<br>  "email": ...,<br>  "birthDate": ...,<br>  "password": ...<br>\} | A JSON-formatted body containing `true` if the row was succesfully populated in the database, and `false` otherwise.
| Checking if a user's password matches | GET | /api/login | <pre>\{<br>  "email": ...,<br>  "password": ...<br>\} | A JSON-formatted body containing the user's account details if the password matches, and empty if the password does not match or the `email` entry does not exist in the database. |
| Uploading an image (xray FR, i think) | PUT | /api/upload | Idk exactly how the body of a multipart submission is supposed to look like, I think it's a bytestream? | either HTTP response code 201 with the `Content-Location` header set to the newly uploaded file's location, or HTTP 204 if the file already exists, with `Content-Location` set to the existing file's location. IDK what to return if the upload fails due to insufficent disk space or something else, as the offical MDN docs (they define http semantics) don't specify that use case or what to do for it. |

## Additional Notes
- All endpoints will return a 400 (bad request) HTTP response code if the formatting specified in the body sections of each table does not match.
- Bytes cannot be requested from the database directly, we use hex instead to return a byte representation.
- Some methods might be changed from POST to GET and back depending on syntax choices that come up during implementation.
- It would be a good idea (but not a totally necessary use of time) to implement some sort of verification / handshake flow that needs to be made before any api requests can be sent, but that's something that would need a decent amount of time (a full day or two) to implement server-side. (functionality for recognizing and storing clients or cookies)
- Uploading an image isn't one of our FRs, so it will likely not be implemented.
