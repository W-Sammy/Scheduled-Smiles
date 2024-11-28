USE scheduledSmiles;

DROP TRIGGER IF EXISTS beforeInsertUsers;

DELIMITER $$
CREATE DEFINER=`root`@`%` TRIGGER `beforeInsertUsers`
BEFORE INSERT ON users
FOR EACH ROW
BEGIN
    IF 
        NEW.email != ''
    THEN
        SET NEW.userID = SHA256(NEW.email);
    END IF;
END$$

DELIMITER ;

DROP TRIGGER IF EXISTS afterInsertUsers;

DELIMITER $$
CREATE DEFINER=`root`@`%` TRIGGER `afterInsertUsers`
AFTER INSERT ON users
FOR EACH ROW
BEGIN
    IF 
        NEW.roleID = SHA256('Staff') AND NEW.email != ''
    THEN
        INSERT INTO staff(staffID, hrlyWage)
        VALUES(NEW.userID, 80.00);
    ELSEIF 
        NEW.roleID = SHA256('Admin') 
    THEN
        INSERT INTO staff(staffID, hrlyWage)
        VALUES(NEW.userID, 35.00);
    END IF;
END$$

DELIMITER ;

DROP TRIGGER IF EXISTS beforeInsertAppointments;

DELIMITER $$
CREATE DEFINER=`root`@`%` TRIGGER `beforeInsertAppointments`
BEFORE INSERT ON appointments
FOR EACH ROW
BEGIN
    DECLARE patientEmail VARCHAR(100);
    
    SELECT email
    INTO patientEmail
    FROM users
    WHERE userID = NEW.patientID
    LIMIT 1;
    
    SET NEW.appointmentID = SHA256(CONCAT(patientEmail, NEW.startTime));
END$$

DELIMITER ;

DROP TRIGGER IF EXISTS beforeInsertMessagePairTypes;

DELIMITER $$
CREATE DEFINER=`root`@`%` TRIGGER `beforeInsertMessagePairTypes`
BEFORE INSERT ON messagePairTypes
FOR EACH ROW
BEGIN
    DECLARE senderEmail VARCHAR(100);
    DECLARE receiverEmail VARCHAR(100);

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

    SET NEW.pairID = SHA256(CONCAT(senderEmail, receiverEmail));

    IF EXISTS (
        SELECT 1 
        FROM messagePairTypes 
        WHERE pairID = NEW.pairID
    ) THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'PairID already exists';
    END IF;
END$$

DELIMITER ;