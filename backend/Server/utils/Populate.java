// This class holds methods for populating and uploading class attributes to and from the database
package Server.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import Server.utils.DatabaseGenericParameter;
import static Server.utils.Json.*;
import Server.DatabaseConnection;
import Users.*;
import static Users.Enum.RoleConstant.*;
import static Users.Enum.AppointmentType.*;

public class Populate {
    private Populate() {
        // restrict instantiation -Kyle
    }
    private static boolean verifyWhere(final String where, final String tableName, final DatabaseConnection db) {
        return !db.query(String.format("SELECT 1 FROM %s WHERE %s", tableName, where)).get(0).get(0).isNull();
    }
    public static Appointment populateAppt(final byte[] appointmentId, DatabaseConnection db) {
        final DatabaseGenericParameter id = new DatabaseGenericParameter(appointmentId);
        // Verify ID exists
        final boolean idExists = verifyWhere(id.equalsTo("appointmentID"), "appointments", db);
        if (!idExists) {
            return null;
        }
        // Aggregate columns
        final String[] columns = {
            "patientID", "staff1ID", "staff2ID", "staff3ID", "stationNumber", "treatment", "notes", "startTime", "isComplete", "isCanceled", "isPaid"
        };
        // Get appointment types
        final String queryString = String.format("SELECT %s FROM appointments WHERE %s", String.join(", ", columns), id.equalsTo("appointmentID"));
        final List<DatabaseGenericParameter> result = db.query(queryString).get(0);
        
        // Prepare parameters
        final byte[] patientID = result.get(0).getAsBytes();
        final List<byte[]> staffList = new ArrayList<byte[]>();
        final int station = result.get(4).getAsInteger();
        final String treatment = result.get(5).getAsString();
        final String notes = result.get(6).getAsString();
        final int timestamp = result.get(7).getAsInteger();
        final boolean completed = result.get(8).getAsBoolean();
        final boolean canceled = result.get(9).getAsBoolean();
        final boolean paid = result.get(10).getAsBoolean();
        
        staffList.add(result.get(1).getAsBytes()); // first staff ID should never be null -Kyle
        if (result.get(2).isNull()) {
            staffList.add(result.get(2).getAsBytes());
        }
        if (result.get(3).isNull()) {
            staffList.add(result.get(3).getAsBytes());
        }
        
        
        
        //  Appointment(byte[] appointmentID, byte[] patientID, List<byte[]> staffList, List<byte[]> treatmentTypes, int stationNumber, String treatment, String notes, int timestamp, boolean completionStatus, boolean cancelStatus, boolean paid)
        return new Appointment(appointmentId, patientID, staffList, station, treatment, notes, timestamp, completed, canceled, paid);
    }
    public static List<Chat> populateChat(final byte[] receiverID, final DatabaseConnection db) {
        final DatabaseGenericParameter rid = new DatabaseGenericParameter(receiverID);
        // Verify ID exists 
        final boolean idExists = verifyWhere(rid.equalsTo("senderID"), "messagePairTypes", db) || verifyWhere(rid.equalsTo("receiverID"), "messagePairTypes", db);
        if (!idExists) {
            return null;
        }
        // Get all related pair ids
        final List<List<DatabaseGenericParameter>> rawPids = db.query(String.format("SELECT pairID FROM messagePairTypes WHERE %s OR %s",  rid.equalsTo("senderID"), rid.equalsTo("receiverID")));
        final ArrayList<DatabaseGenericParameter> pids = new ArrayList<DatabaseGenericParameter>();
        for (List<DatabaseGenericParameter> row : rawPids) {
            pids.add(row.get(0));
        }
        // Aggregate columns
        final String[] columns = {
            "textContent", "createdAt"
        };
        final ArrayList<Chat> chats = new ArrayList<Chat>();
        int idx = 0;
        for (DatabaseGenericParameter pid : pids) {
            final List<DatabaseGenericParameter> idPair = db.query(String.format("SELECT senderID, receiverID FROM messagePairTypes WHERE %s", pid.equalsTo("pairID"))).get(0);
            chats.add(new Chat(idPair.get(0).getAsBytes(), idPair.get(1).getAsBytes()));
            idx = chats.size() - 1;
            final String queryString = String.format("SELECT %s FROM messages WHERE %s ORDER BY createdAt DESC", String.join(", ", columns), pid.equalsTo("pairID"));
            final List<List<DatabaseGenericParameter>> result = db.query(queryString);
            for (List<DatabaseGenericParameter> row : result) {
                chats.get(idx).addMessages(row.get(0).getAsString(), row.get(1).getAsInteger());
            }
        }
        // Chat(byte[] senderID, byte[] receiverID)
        return chats;
    }
    public static Chat populateChat(final byte[] receiverID, final byte[] senderID, final DatabaseConnection db) {
        final DatabaseGenericParameter sid = new DatabaseGenericParameter(senderID);
        final DatabaseGenericParameter rid = new DatabaseGenericParameter(receiverID);
        final Chat chat  = new Chat(senderID, receiverID);
        // Verify ID exists 
        final boolean idExists = verifyWhere(sid.equalsTo("senderID"), "messagePairTypes", db) && verifyWhere(rid.equalsTo("receiverID"), "messagePairTypes", db);
        if (!idExists) {
            return null;
        }
        // Get stupid pair id
        final DatabaseGenericParameter pid = db.query(String.format("SELECT pairID WHERE %s AND %s",  sid.equalsTo("senderID"), rid.equalsTo("receiverID"))).get(0).get(0);
        // Aggregate columns
        final String[] columns = {
            "textContent", "createdAt"
        };
        final String queryString = String.format("SELECT %s FROM messages WHERE %s ORDER BY createdAt DESC", String.join(", ", columns), pid.equalsTo("pairID"));
        final List<List<DatabaseGenericParameter>> result = db.query(queryString);
        
        for (List<DatabaseGenericParameter> row : result) {
            chat.addMessages(row.get(0).getAsString(), row.get(1).getAsInteger());
        }
        // Chat(byte[] senderID, byte[] receiverID)
        return chat;
    }
    public static User populateUser(final byte[] userId, final DatabaseConnection db) {
        final DatabaseGenericParameter id = new DatabaseGenericParameter(userId);
        // Verify ID exists 
        final boolean idExists = verifyWhere(id.equalsTo("userID"), "users", db);
        if (!idExists) {
            return null;
        }
        // Aggregate columns
        final List<String> columns = new ArrayList<String>();
        columns.add("roleID");
        columns.add("firstName");
        columns.add("lastName");
        columns.add("address");
        columns.add("sex");
        columns.add("phoneNumber");
        columns.add("email");
        columns.add("birthDate");        
        
        // Construct query
        final String queryString = String.format("SELECT %s FROM users WHERE %s", String.join(", ", columns), id.equalsTo("userID"));
        final List<DatabaseGenericParameter> result = db.query(queryString).get(0); // only get first result (there should only be one anyways)
        
        // Collect parameters
        final byte[] roleId = result.get(0).getAsBytes();
        final String first = result.get(1).getAsString();
        final String last = result.get(2).getAsString();
        final String address = result.get(3).getAsString();
        final char sex = result.get(4).getAsChar();
        final String phone = result.get(5).getAsString();
        final String email = result.get(6).getAsString();
        final int bday = result.get(7).getAsInteger();
        double wage = 0.0;
        // Get role-specific info
        if (Arrays.equals(roleId, ROLE_IDS.get("Admin")) || Arrays.equals(roleId, ROLE_IDS.get("Staff"))) {
            wage = db.query("SELECT hrlyWage WHERE " + id.equalsTo("staffID")).get(0).get(0).getAsDouble();
        }

        // Theres probably a better way to do this but im too tired -Kyle
        if (Arrays.equals(roleId, ROLE_IDS.get("Admin"))) {
            return new Admin(userId, first, last, address, sex, phone, email, bday, wage);
        } else if (Arrays.equals(roleId, ROLE_IDS.get("Staff"))) {
            return new Staff(userId, first, last, address, sex, phone, email, bday, wage);
        } else if (Arrays.equals(roleId, ROLE_IDS.get("Patient"))) {
            return new Patient(userId, first, last, address, sex, phone, email, bday);
        } else {
            return null;
        }
    }
}