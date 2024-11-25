/* THIS CODE WILL NOT RUN WITHOUT:
 *     BLANK <scheduledSmiles> DATABASE
 *     <local/remote> HELPER FUNCTIONS, PROCEDURES, AND TRIGGERS;
 */
USE scheduledSmiles;

# insert sample patients into database
INSERT INTO users(email, hashedPass, firstName, lastName, sex, birthDate, address, phoneNumber, roleID)
VALUES('JohnDoe@email.com',
       SHA256('password123'),
       'John',
       'Doe',
       'M',
       759283200,
       '123 Address Lane',
       '1234567890',
       SHA256('Patient')),

      ('JaneDoe@email.com',
       SHA256('password456'),
       'Jane',
       'Doe',
       'F',
       826761600,
       '123 Address Lane',
       '3141592654',
       SHA256('Patient')),

      ('JaySohn@email.com',
       SHA256('password732'),
       'Jay',
       'Sohn',
       'M',
       948614400,
       '123 Address Ct',
       '9163597437',
       SHA256('Patient'));

# insert sample staff into database 
INSERT INTO users(email, hashedPass, firstName, lastName, sex, birthDate, address, phoneNumber, roleID)
VALUES('StephFu@scheduledsmiles.com',
       SHA256('password314'),
       'Steph',
       'Fu',
       'F',
       567648000,
       '456 Address Ave',
       '9169678121',
       SHA256('Staff')),

      ('StewartFerris@scheduledsmiles.com',
       SHA256('password917'),
       'Stewart',
       'Ferris',
       'M',
       905990400,
       '456 Address Blvd',
       '9160258429',
       SHA256('Staff')),

      ('EliseFlossmore@scheduledsmiles.com',
       SHA256('password917'),
       'Elise',
       'Flossmore',
       'F',
       774705600,
       '456 Address Dr',
       '9165592063',
       SHA256('Staff'));

# insert sample admin into database
INSERT INTO users(email, hashedPass, firstName, lastName, sex, birthDate, address, phoneNumber, roleID)
VALUES('AdamMinh@scheduledsmiles.adm.com',
       SHA256('password159'),
       'Adam',
       'Minh',
       'M',
       169171200,
       '789 Address Way',
       '9166534124',
       SHA256('Admin')),

      ('AnnieYeager@scheduledsmiles.adm.com',
       SHA256('password471'),
       'Annie',
       'Yeager',
       'F',
       397526400,
       '789 Address Dr',
       '9167954329',
       SHA256('Admin'));

# insert sample appointments
INSERT INTO appointments(stationNumber, treatment, patientID, startTime, staff1ID, staff2ID, staff3ID)
VALUES(1,
       'Checkup',
       userID_of('JohnDoe@email.com'),
       1731087000,
       userID_of('StewartFerris@scheduledsmiles.com'),
       NULL,
       NULL),

      (2,
       'Checkup',
       userID_of('JaySohn@email.com'),
       1731092400,
       userID_of('StewartFerris@scheduledsmiles.com'),
       NULL,
       NULL),

      (1,
       'Checkup',
       userID_of('JaneDoe@email.com'),
       1731090600,
       userID_of('StewartFerris@scheduledsmiles.com'),
       NULL,
       NULL);
       
# change completion for email on date
UPDATE appointments
SET isComplete = 1, notes = 'Patient needs further care'
WHERE patientID = userID_of('JohnDoe@email.com')
    AND (isCanceled = 0 AND isComplete = 0)
    AND startTime >= UNIX_TIMESTAMP('2024-11-08 00:00:00')
    AND startTime < UNIX_TIMESTAMP('2024-11-09 00:00:00')
ORDER BY startTime ASC
LIMIT 1;

# change cancellation for email on date
UPDATE appointments
SET isCanceled = 1, notes = 'Patient cannot come in, rescheduled'
WHERE patientID = userID_of('JaySohn@email.com')
    AND (isCanceled = 0 AND isComplete = 0)
    AND startTime >= UNIX_TIMESTAMP('2024-11-08 00:00:00')
    AND startTime < UNIX_TIMESTAMP('2024-11-09 00:00:00')
ORDER BY startTime ASC
LIMIT 1;

# change completion for email on date
UPDATE appointments
SET isComplete = 1
WHERE patientID = userID_of('JaneDoe@email.com')
    AND (isCanceled = 0 AND isComplete = 0)
    AND startTime >= UNIX_TIMESTAMP('2024-11-08 00:00:00')
    AND startTime < UNIX_TIMESTAMP('2024-11-09 00:00:00')
ORDER BY startTime ASC
LIMIT 1;

# insert sample appointments
INSERT INTO appointments(stationNumber, treatment, patientID, startTime, staff1ID, staff2ID, staff3ID)
VALUES(1,
       'Filling',
       userID_of('JohnDoe@email.com'),
       1731690000,
       userID_of('EliseFlossmore@scheduledsmiles.com'),
       userID_of('StewartFerris@scheduledsmiles.com'),
       NULL),
       
      (1,
       'Checkup',
       userID_of('JaySohn@email.com'),
       1731693600,
       userID_of('StewartFerris@scheduledsmiles.com'),
       NULL,
       NULL);
       
# change completion for email on date
UPDATE appointments
SET isComplete = 1, notes = 'Operation Successful, Patient must...'
WHERE patientID = userID_of('JohnDoe@email.com')
    AND (isCanceled = 0 AND isComplete = 0)
    AND startTime >= UNIX_TIMESTAMP('2024-11-15 00:00:00')
    AND startTime < UNIX_TIMESTAMP('2024-11-16 00:00:00')
ORDER BY startTime ASC
LIMIT 1;

# change completion for email on date
UPDATE appointments
SET isComplete = 1, notes = 'Patient needs further care'
WHERE patientID = userID_of('JaySohn@email.com')
    AND (isCanceled = 0 AND isComplete = 0)
    AND startTime >= UNIX_TIMESTAMP('2024-11-15 00:00:00')
    AND startTime < UNIX_TIMESTAMP('2024-11-16 00:00:00')
ORDER BY startTime ASC
LIMIT 1;

# insert sample appointment
INSERT INTO appointments(stationNumber, treatment, patientID, startTime, staff1ID, staff2ID, staff3ID)
VALUES(1,
       'Emergency',
       userID_of('JaySohn@email.com'),
       1732294800,
       userID_of('StephFu@scheduledsmiles.com'),
       userID_of('EliseFlossmore@scheduledsmiles.com'),
       userID_of('StewartFerris@scheduledsmiles.com'));
       
# change completion for email on date
UPDATE appointments
SET isComplete = 1, notes = 'Operation Successful, Patient must...'
WHERE patientID = userID_of('JaySohn@email.com')
    AND (isCanceled = 0 AND isComplete = 0)
    AND startTime >= UNIX_TIMESTAMP('2024-11-22 00:00:00')
    AND startTime < UNIX_TIMESTAMP('2024-11-23 00:00:00')
ORDER BY startTime ASC
LIMIT 1;

INSERT INTO messagePairTypes(senderID, receiverID)
VALUES((SELECT userID FROM users WHERE email = 'JohnDoe@email.com'),
       (SELECT userID FROM users WHERE email = 'StephFu@scheduledsmiles.com')),
      ((SELECT userID FROM users WHERE email = 'StephFu@scheduledsmiles.com'),
       (SELECT userID FROM users WHERE email = 'JohnDoe@email.com'));
INSERT INTO messagePairTypes(senderID, receiverID)
VALUES((SELECT userID FROM users WHERE email = 'JaneDoe@email.com'),
       (SELECT userID FROM users WHERE email = 'EliseFlossmore@scheduledsmiles.com')),
      ((SELECT userID FROM users WHERE email = 'EliseFlossmore@scheduledsmiles.com'),
       (SELECT userID FROM users WHERE email = 'JaneDoe@email.com'));
INSERT INTO messagePairTypes(senderID, receiverID)
VALUES((SELECT userID FROM users WHERE email = 'JaySohn@email.com'),
       (SELECT userID FROM users WHERE email = 'StewartFerris@scheduledsmiles.com')),
      ((SELECT userID FROM users WHERE email = 'StewartFerris@scheduledsmiles.com'),
       (SELECT userID FROM users WHERE email = 'JaySohn@email.com'));

INSERT INTO messages
VALUES(pairID_of('JohnDoe@email.com', 'StephFu@scheduledsmiles.com'),
       UNIX_TIMESTAMP()-481767,
       'MESSAGE1-1'),
       (pairID_of('StephFu@scheduledsmiles.com', 'JohnDoe@email.com'),
       UNIX_TIMESTAMP()-400631,
       'RESPONSE1-1'),
       (pairID_of('JohnDoe@email.com', 'StephFu@scheduledsmiles.com'),
       UNIX_TIMESTAMP()-390168,
       'MESSAGE1-2'),
       (pairID_of('StephFu@scheduledsmiles.com', 'JohnDoe@email.com'),
       UNIX_TIMESTAMP()-54632,
       'RESPONSE1-2'),
       (pairID_of('JaneDoe@email.com', 'EliseFlossmore@scheduledsmiles.com'),
       UNIX_TIMESTAMP()-83286,
       'MESSAGE2-1'),
       (pairID_of('EliseFlossmore@scheduledsmiles.com', 'JaneDoe@email.com'),
       UNIX_TIMESTAMP()-23872,
       'RESPONSE2-1'),
       (pairID_of('JaySohn@email.com', 'StewartFerris@scheduledsmiles.com'),
       UNIX_TIMESTAMP()-468352,
       'MESSAGE3-1'),
       (pairID_of('StewartFerris@scheduledsmiles.com', 'JaySohn@email.com'),
       UNIX_TIMESTAMP()-1777,
       'RESPONSE3-1');
       