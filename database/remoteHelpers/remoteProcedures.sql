USE scheduledSmiles;

DROP PROCEDURE IF EXISTS insertMessage;

DELIMITER $$
CREATE DEFINER=`root`@`%` PROCEDURE `insertMessage`(
    senderID BINARY(32),
    receiverID BINARY(32),
    textContent VARCHAR(160)
) 
BEGIN
    DECLARE senderEmail VARCHAR(100);
    DECLARE receiverEmail VARCHAR(100);
    DECLARE pairID BINARY(32);
    DECLARE inverseID BINARY(32); 

    SELECT email
    INTO senderEmail
    FROM users
    WHERE userID = senderID
    LIMIT 1;

    SELECT email
    INTO receiverEmail
    FROM users
    WHERE userID = receiverID
    LIMIT 1;

    SET pairID = SHA256(CONCAT(senderEmail, receiverEmail));

    IF NOT EXISTS (
        SELECT 1 
        FROM messagePairTypes
        WHERE messagePairTypes.pairID = pairID
    )
    THEN
        SET inverseID = SHA256(CONCAT(receiverEmail, senderEmail));
        INSERT INTO messagePairTypes (senderID, receiverID)
        VALUES(senderID,
                receiverID),
               (receiverID,
                senderID);
        INSERT INTO messages
        VALUES(inverseID, 0, '');
    END IF;

    INSERT INTO messages
    VALUES(pairID, UNIX_TIMESTAMP(), textContent);
END$$

DELIMITER ;

DROP PROCEDURE IF EXISTS getAvailableStaff;

DELIMITER $$

CREATE DEFINER=`root`@`%` PROCEDURE `getAvailableStaff`(
    IN startTime INT
) 
BEGIN
    SELECT staffID 
    FROM staff
    WHERE staffID NOT IN (
        SELECT staff1ID 
        FROM appointments 
        WHERE appointments.isCanceled = 0 AND appointments.startTime = startTime AND staff1ID IS NOT NULL
        UNION
        SELECT staff2ID 
        FROM appointments 
        WHERE appointments.isCanceled = 0 AND appointments.startTime = startTime AND staff2ID IS NOT NULL
        UNION
        SELECT staff3ID 
        FROM appointments 
        WHERE appointments.isCanceled = 0 AND appointments.startTime = startTime AND staff3ID IS NOT NULL
    );
END$$

DELIMITER ;