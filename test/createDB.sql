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
  staff1ID  BINARY(32),
  staff2ID  BINARY(32) NULL,
  staff3ID  BINARY(32) NULL,
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
  pairID INT,
  senderID BINARY(32),
  receiverID BINARY(32),
  PRIMARY KEY (pairID),
  CONSTRAINT FK_sender FOREIGN KEY (senderID)
  REFERENCES users(userID),
  CONSTRAINT FK_receiver FOREIGN KEY (receiverID)
  REFERENCES users(userID)
);

CREATE TABLE messages (
  pairID INT,
  createdAt INT,
  textContent TEXT NOT NULL,
  PRIMARY KEY (pairID, createdAt),
  CONSTRAINT FK_messagePair FOREIGN KEY (pairID)
  REFERENCES messagePairTypes(pairID)
);
