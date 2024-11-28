USE scheduledSmiles;

DROP PROCEDURE IF EXISTS insertMessage;

CREATE PROCEDURE `insertMessage`(
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
    SET inverseID = SHA256(CONCAT(receiverEmail, senderEmail));
    
    IF NOT EXISTS (
        SELECT 1 
        FROM messagePairTypes
        WHERE messagePairTypes.pairID = pairID
    )
    THEN
        INSERT IGNORE INTO messagePairTypes
        VALUES(pairID, senderID, receiverID);
    END IF;

    INSERT IGNORE INTO messages
    VALUES(pairID, UNIX_TIMESTAMP(), textContent);

    IF NOT EXISTS (
        SELECT 1 
        FROM messagePairTypes
        WHERE messagePairTypes.pairID = inverseID
    )
    THEN
        INSERT IGNORE INTO messagePairTypes
        VALUES(inverseID, receiverID, senderID);
        INSERT INTO messages
        VALUES(inverseID, UNIX_TIMESTAMP(), '');
    END IF;
END;