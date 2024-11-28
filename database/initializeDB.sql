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

# insert special 'N/A' staff into database
INSERT INTO users(userID, email, hashedPass, firstName, lastName, sex, birthDate, address, phoneNumber, roleID)
VALUES(b'00000000000000000000000000000000',
       '',
       SHA256('00000000000000000000000000000000'),
       '',
       '',
       '',
       0,
       '',
       '',
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
       1731085200,
       userID_of('StewartFerris@scheduledsmiles.com'),
       userID_of(''),
       userID_of('')),

      (1,
       'Checkup',
       userID_of('JaneDoe@email.com'),
       1731088800,
       userID_of('StewartFerris@scheduledsmiles.com'),
       userID_of(''),
       userID_of('')),
       
      (2,
       'Checkup',
       userID_of('JaySohn@email.com'),
       1731092400,
       userID_of('StewartFerris@scheduledsmiles.com'),
       userID_of(''),
       userID_of(''));
       
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
       userID_of('')),
       
      (1,
       'Checkup',
       userID_of('JaySohn@email.com'),
       1731693600,
       userID_of('StewartFerris@scheduledsmiles.com'),
       userID_of(''),
       userID_of(''));
       
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

# insert sample messages (and messagePairTypes / pairID's)
call insertMessage(userID_of('JohnDoe@email.com'), userID_of('StephFu@scheduledsmiles.com'), 'MESSAGE1-1');
call insertMessage(userID_of('JaneDoe@email.com'), userID_of('StephFu@scheduledsmiles.com'), 'MESSAGE2-1');
call insertMessage(userID_of('JaySohn@email.com'), userID_of('StephFu@scheduledsmiles.com'), 'MESSAGE3-1');
call insertMessage(userID_of('StephFu@scheduledsmiles.com'), userID_of('JohnDoe@email.com'), 'RESPONSE1-1');
call insertMessage(userID_of('JohnDoe@email.com'), userID_of('StephFu@scheduledsmiles.com'), 'MESSAGE1-2');
call insertMessage(userID_of('StephFu@scheduledsmiles.com'), userID_of('JohnDoe@email.com'), 'RESPONSE1-2');
call insertMessage(userID_of('StephFu@scheduledsmiles.com'), userID_of('JaneDoe@email.com'), 'RESPONSE2-1');
call insertMessage(userID_of('StephFu@scheduledsmiles.com'), userID_of('JaySohn@email.com'), 'RESPONSE3-1');
