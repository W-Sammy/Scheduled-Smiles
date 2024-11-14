# NOTE: database and table names are turned into all lowercase
# i.e. scheduledSmiles -> scheduledsmiles

# create and use scheduledSmiles database
CREATE DATABASE scheduledSmiles;
USE scheduledSmiles;

CREATE TABLE roleTypes (
    roleID BINARY(32),
    role VARCHAR(10),
    PRIMARY KEY (roleID)
);

CREATE TABLE users (
    userID BINARY(32),
    email VARCHAR(100),
    hashedPass BINARY(32),
    firstName VARCHAR(35),
    lastName VARCHAR(35),
    sex CHAR,
    birthDate INT,
    address VARCHAR(100),
    phoneNumber VARCHAR(10),
    roleID BINARY(32),
    detail TEXT NULL,
    PRIMARY KEY (userID),
    CONSTRAINT FK_userRole FOREIGN KEY (roleID)
    REFERENCES roleTypes(roleID)
);

CREATE TABLE staff (
    staffID BINARY(32),
    hrlyWage decimal(10, 2),
    PRIMARY KEY (staffID),
    CONSTRAINT FK_staffID FOREIGN KEY (staffID)
    REFERENCES users(userID)
);

CREATE TABLE appointments (
    appointmentID BINARY(32),
    patientID BINARY(32),
    startTime INT,
    staff1ID BINARY(32),
    staff2ID BINARY(32) NULL,
    staff3ID BINARY(32) NULL,
    isCanceled TINYINT DEFAULT 0,
    PRIMARY KEY (appointmentID),
    CONSTRAINT FK_patient FOREIGN KEY (patientID)
    REFERENCES users(userID),
    CONSTRAINT FK_staff1 FOREIGN KEY (staff1ID)
    REFERENCES users(userID),
    CONSTRAINT FK_staff2 FOREIGN KEY (staff2ID)
    REFERENCES users(userID),
    CONSTRAINT FK_staff3 FOREIGN KEY (staff3ID)
    REFERENCES users(userID)
);

CREATE TABLE appointmentTypeKey (
    typeID BINARY(32),
    appointmentType VARCHAR(50),
    cost DECIMAL(10, 2),
    PRIMARY KEY (typeID)
);

CREATE TABLE appointmentTypes (
    appointmentID BINARY(32),
    typeID BINARY(32),
    PRIMARY KEY (appointmentID, typeID),
    CONSTRAINT FK_appointment FOREIGN KEY (appointmentID)
    REFERENCES appointments(appointmentID),
    CONSTRAINT FK_type FOREIGN KEY (typeID)
    REFERENCES appointmentTypeKey(typeID)
);

CREATE TABLE messagePairTypes (
    pairID BINARY(32),
    senderID BINARY(32),
    receiverID BINARY(32),
    PRIMARY KEY (pairID),
    CONSTRAINT FK_sender FOREIGN KEY (senderID)
    REFERENCES users(userID),
    CONSTRAINT FK_receiver FOREIGN KEY (receiverID)
    REFERENCES users(userID)
);

CREATE TABLE messages (
    pairID BINARY(32),
    createdAt INT,
    textContent TEXT NOT NULL,
    PRIMARY KEY (pairID, createdAt),
    CONSTRAINT FK_messagePair FOREIGN KEY (pairID)
    REFERENCES messagePairTypes(pairID)
);

DROP FUNCTION IF EXISTS SHA256;

DELIMITER $$
USE scheduledSmiles$$
CREATE DEFINER=`root`@`%` FUNCTION `SHA256`(
    input VARCHAR(255)
) 
RETURNS BINARY(32)
DETERMINISTIC
BEGIN
    RETURN UNHEX(SHA2(input, 256));
END$$

DELIMITER ;

DROP FUNCTION IF EXISTS userID_of;

DELIMITER $$
USE scheduledSmiles$$
CREATE DEFINER=`root`@`%` FUNCTION `userID_of`(
    email VARCHAR(255)
) 
RETURNS BINARY(32)
DETERMINISTIC
BEGIN
    DECLARE ID BINARY(32);
    
    SELECT userID 
    INTO ID
    FROM users 
    WHERE senderID = SHA256(email)
    LIMIT 1;
    
    RETURN ID;
END$$

DELIMITER ;

DROP FUNCTION IF EXISTS pairID_of;

DELIMITER $$
USE scheduledSmiles$$
CREATE DEFINER=`root`@`%` FUNCTION `pairID_of`(
    sender VARCHAR(255),
    receiver VARCHAR(255)
) 
RETURNS BINARY(32)
DETERMINISTIC
BEGIN
    DECLARE ID BINARY(32);
    
    SELECT pairID 
    INTO ID
    FROM messagePairTypes 
    WHERE senderID = SHA256(sender) AND receiverID = SHA256(receiver)
    LIMIT 1;
    
    RETURN ID;
END$$

DELIMITER ;

DROP PROCEDURE IF EXISTS insert_apt_type;

DELIMITER $$

CREATE DEFINER=`root`@`%` PROCEDURE `insert_apt_type`(
    IN inEmail VARCHAR(255),
    IN aptTypes VARCHAR(255)
)
BEGIN
    DECLARE selectedAppointmentID BINARY(32);

    SELECT appointmentID 
    INTO selectedAppointmentID
    FROM appointments a
    WHERE a.patientID = (SELECT userID FROM users WHERE email = inEmail)
    AND NOT EXISTS (
        SELECT 1 
        FROM appointmentTypes
        WHERE appointmentID = selectedAppointmentID
    )
    ORDER BY a.startTime ASC
    LIMIT 1;

    INSERT INTO appointmentTypes (appointmentID, typeID)
    SELECT 
        selectedAppointmentID, 
        t.typeID
    FROM 
        appointmentTypeKey t
    WHERE 
        FIND_IN_SET(t.appointmentType, aptTypes) > 0
    AND NOT EXISTS (
        SELECT 1 
        FROM appointmentTypes
        WHERE appointmentID = selectedAppointmentID AND typeID = t.typeID
    );

END$$

DELIMITER ;

DROP TRIGGER IF EXISTS beforeInsertAppointments;

DELIMITER $$
CREATE DEFINER=`root`@`%` TRIGGER `beforeInsertAppointments`
BEFORE INSERT ON appointments
FOR EACH ROW
BEGIN
    SET NEW.appointmentID = SHA256(CONCAT('Appointment', UUID()));
END$$

DELIMITER ;

DROP TRIGGER IF EXISTS beforeInsertMessagePairTypes;

DELIMITER $$
CREATE DEFINER=`root`@`%` TRIGGER `beforeInsertMessagePairTypes`
BEFORE INSERT ON messagePairTypes
FOR EACH ROW
BEGIN
 DECLARE senderEmail VARCHAR(255);
    DECLARE receiverEmail VARCHAR(255);
    
    SELECT email 
    INTO senderEmail
    FROM users
    WHERE userID = NEW.senderID
    LIMIT 1;
    
    SELECT email 
    INTO receiverEmail
    FROM users
    WHERE userID = NEW.receiverID
    LIMIT 1;
    
    SET NEW.pairID = SHA256(CONCAT(senderEmail,receiverEmail));
END$$
DELIMITER ;