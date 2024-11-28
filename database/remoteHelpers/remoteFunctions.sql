USE scheduledSmiles;

DROP FUNCTION IF EXISTS SHA256;

DELIMITER $$
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
    WHERE users.email = email
    LIMIT 1;

    IF 
        ID IS NULL 
    THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Email not found';
    END IF;
    
    RETURN ID;
END$$

DELIMITER ;

DROP FUNCTION IF EXISTS pairID_of;

DELIMITER $$
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