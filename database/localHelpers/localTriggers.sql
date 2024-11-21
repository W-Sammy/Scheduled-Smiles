USE scheduledSmiles;

DROP TRIGGER IF EXISTS beforeInsertUsers;

CREATE TRIGGER `beforeInsertUsers`
BEFORE INSERT ON users
FOR EACH ROW
BEGIN
    SET NEW.userID = SHA256(NEW.email);
END;

DROP TRIGGER IF EXISTS beforeInsertAppointments;

CREATE TRIGGER `beforeInsertAppointments`
BEFORE INSERT ON appointments
FOR EACH ROW
BEGIN
    DECLARE patientEmail VARCHAR(255);
    
    SELECT email
    INTO patientEmail
    FROM users
    WHERE userID = NEW.patientID
    LIMIT 1;
    
    SET NEW.appointmentID = SHA256(CONCAT(patientEmail, NEW.startTime));
END;

DROP TRIGGER IF EXISTS beforeInsertMessagePairTypes;

CREATE TRIGGER `beforeInsertMessagePairTypes`
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
END;