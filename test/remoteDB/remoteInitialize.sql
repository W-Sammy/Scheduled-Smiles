USE scheduledSmiles;

# insert roleTypes into database
INSERT INTO roleTypes
VALUES(SHA256('Patient'), 'Patient'),
      (SHA256('Staff'), 'Staff'),
      (SHA256('Admin'), 'Admin');

# insert sample patients into database
INSERT INTO users
VALUES(SHA256('JohnDoe@email.com'),
       'JohnDoe@email.com',
       SHA256('password123'),
       'John',
       'Doe',
       'M',
       759283200,
       '123 Address Lane',
       '1234567890',
       SHA256('Patient'),
       ''),

      (SHA256('JaneDoe@email.com'),
       'JaneDoe@email.com',
       SHA256('password456'),
       'Jane',
       'Doe',
       'F',
       826761600,
       '123 Address Lane',
       '3141592654',
       SHA256('Patient'),
       ''),
       
      (SHA256('JaySohn@email.com'),
       'JaySohn@email.com',
       SHA256('password732'),
       'Jay',
       'Sohn',
       'M',
       948614400,
       '123 Address Ct',
       '9163597437',
       SHA256('Patient'),
       '');

# insert sample staff into database
INSERT INTO users
VALUES(SHA256('StephFu@scheduledsmiles.com'),
       'StephFu@scheduledsmiles.com',
       SHA256('password314'),
       'Steph',
       'Fu',
       'F',
       567648000,
       '456 Address Ave',
       '9169678121',
       SHA256('Staff'),
       ''),
       
      (SHA256('StewartFerris@scheduledsmiles.com'),
       'StewartFerris@scheduledsmiles.com',
       SHA256('password917'),
       'Stewart',
       'Ferris',
       'M',
       905990400,
       '456 Address Blvd',
       '9160258429',
       SHA256('Staff'),
       ''),
       
      (SHA256('EliseFlossmore@scheduledsmiles.com'),
       'EliseFlossmore@scheduledsmiles.com',
       SHA256('password917'),
       'Elise',
       'Flossmore',
       'F',
       774705600,
       '456 Address Dr',
       '9165592063',
       SHA256('Staff'),
       '');

# insert sample admin into database
INSERT INTO users
VALUES(SHA256('AdamMinh@scheduledsmiles.com'),
       'AdamMinh@scheduledsmiles.com',
       SHA256('password159'),
       'Adam',
       'Minh',
       'M',
       169171200,
       '789 Address Way',
       '9166534124',
       SHA256('Admin'),
       ''),
       
      (SHA256('AnnieYeager@scheduledsmiles.com'),
       'AnnieYeager@scheduledsmiles.com',
       SHA256('password471'),
       'Annie',
       'Yeager',
       'F',
       397526400,
       '789 Address Dr',
       '9167954329',
       SHA256('Admin'),
       '');

INSERT INTO staff
VALUES((SELECT userID FROM users WHERE email = 'StephFu@scheduledsmiles.com'), 88.75);
INSERT INTO staff
VALUES((SELECT userID FROM users WHERE email = 'StewartFerris@scheduledsmiles.com'), 34.50);
INSERT INTO staff
VALUES((SELECT userID FROM users WHERE email = 'EliseFlossmore@scheduledsmiles.com'), 55.75);

/* 
INSERT INTO staff
VALUES(SHA256('AdamMinh@scheduledsmiles.com'), 37.82);
INSERT INTO staff
VALUES(SHA256('AnnieYeager@scheduledsmiles.com'), 36.34);
*/

INSERT INTO appointments(patientID, startTime, staff1ID, staff2ID, staff3ID, isCanceled)
VALUES((SELECT userID FROM users WHERE email = 'JohnDoe@email.com'),
       1731087000,
       (SELECT userID FROM users WHERE email = 'StewartFerris@scheduledsmiles.com'),
       NULL,
       NULL,
       0);
INSERT INTO appointments(patientID, startTime, staff1ID, staff2ID, staff3ID, isCanceled)       
VALUES((SELECT userID FROM users WHERE email = 'JaySohn@email.com'),
       1731092400,
       (SELECT userID FROM users WHERE email = 'StewartFerris@scheduledsmiles.com'),
       NULL,
       NULL,
       1);
INSERT INTO appointments(patientID, startTime, staff1ID, staff2ID, staff3ID, isCanceled)
VALUES((SELECT userID FROM users WHERE email = 'JaneDoe@email.com'),
       1731090600,
       (SELECT userID FROM users WHERE email = 'StewartFerris@scheduledsmiles.com'),
       NULL,
       NULL,
       0);
INSERT INTO appointments(patientID, startTime, staff1ID, staff2ID, staff3ID, isCanceled)
VALUES((SELECT userID FROM users WHERE email = 'JohnDoe@email.com'),
       1731690000,
       (SELECT userID FROM users WHERE email = 'EliseFlossmore@scheduledsmiles.com'),
       (SELECT userID FROM users WHERE email = 'StewartFerris@scheduledsmiles.com'),
       NULL,
       0);
INSERT INTO appointments(patientID, startTime, staff1ID, staff2ID, staff3ID, isCanceled)       
VALUES((SELECT userID FROM users WHERE email = 'JaySohn@email.com'),
       1731693600,
       (SELECT userID FROM users WHERE email = 'StewartFerris@scheduledsmiles.com'),
       NULL,
       NULL,
       0);
INSERT INTO appointments(patientID, startTime, staff1ID, staff2ID, staff3ID, isCanceled)       
VALUES((SELECT userID FROM users WHERE email = 'JaySohn@email.com'),
       1732294800,
       (SELECT userID FROM users WHERE email = 'StephFu@scheduledsmiles.com'),
       (SELECT userID FROM users WHERE email = 'EliseFlossmore@scheduledsmiles.com'),
       (SELECT userID FROM users WHERE email = 'StewartFerris@scheduledsmiles.com'),
       0);

INSERT INTO appointmentTypeKey 
VALUES(SHA256('Cleaning'),
	    'Cleaning',
	    250.00),
       
	    (SHA256('Filling'),
	    'Filling',
	    300.00),
	    
	    (SHA256('X-Ray'),
	    'X-Ray',
	    200.00),
	    
      (SHA256('Checkup'),
       'Checkup',
       200.00),
       
      (SHA256('Emergency'),
       'Emergency',
       750.00);

CALL insert_apt_type('JohnDoe@email.com', 'Checkup,,X-Ray');
CALL insert_apt_type('JohnDoe@email.com', 'Cleaning,Filling');

CALL insert_apt_type('JaneDoe@email.com', 'Checkup,,X-Ray');
CALL insert_apt_type('JaneDoe@email.com', 'Cleaning,Filling');

CALL insert_apt_type('JaySohn@email.com', 'Checkup');
CALL insert_apt_type('JaySohn@email.com', 'Checkup,X-Ray');
CALL insert_apt_type('JaySohn@email.com', 'Emergency');

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