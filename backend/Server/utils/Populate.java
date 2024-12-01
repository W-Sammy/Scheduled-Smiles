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
        final List<List<DatabaseGenericParameter>> r = db.query(String.format("SELECT 1 FROM %s WHERE %s", tableName, where));
        return r != null && r.size() > 0 && !r.get(0).get(0).isNull();
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
        final byte[] staff1ID = result.get(1).getAsBytes();
        final byte[] staff2ID = result.get(2).getAsBytes();
        final byte[] staff3ID = result.get(3).getAsBytes();
        final int station = result.get(4).getAsInteger();
        final String treatment = result.get(5).getAsString();
        final String notes = result.get(6).getAsString();
        final int timestamp = result.get(7).getAsInteger();
        final boolean completed = result.get(8).getAsBoolean();
        final boolean canceled = result.get(9).getAsBoolean();
        final boolean paid = result.get(10).getAsBoolean();
        
        //  Appointment(byte[] appointmentID, byte[] patientID, List<byte[]> staffList, List<byte[]> treatmentTypes, int stationNumber, String treatment, String notes, int timestamp, boolean completionStatus, boolean cancelStatus, boolean paid)
        return new Appointment(appointmentId, patientID, staff1ID, staff2ID, staff3ID, station, treatment, notes, timestamp, completed, canceled, paid);
    }
    
    public static List<Appointment> populateAppts(final byte[] userID, DatabaseConnection db) {
        final DatabaseGenericParameter id = new DatabaseGenericParameter(userID);
        // Verify ID exists
        final boolean idExists = verifyWhere(id.equalsTo("staff1ID"), "appointments", db);
        if (!idExists) {
            return null;
        }
        // Aggregate columns
        final String[] columns = {
            "appointmentID", "patientID", "staff2ID", "staff3ID", "stationNumber", "treatment", "notes", "startTime", "isComplete", "isCanceled", "isPaid"
        };
        // Get appointment types
        final String queryString = String.format("SELECT %s FROM appointments WHERE %s", String.join(", ", columns), id.equalsTo("staff1ID"));
        final List<List<DatabaseGenericParameter>> results = db.query(queryString);
        final List<Appointment> appts = new ArrayList<Appointment>();
        for (List<DatabaseGenericParameter> result : results) {
            // Prepare parameters
            final byte[] appointmentID = result.get(0).getAsBytes();
            final byte[] patientID = result.get(1).getAsBytes();
            final byte[] staff2ID = result.get(2).getAsBytes();
            final byte[] staff3ID = result.get(3).getAsBytes();
            final int station = result.get(4).getAsInteger();
            final String treatment = result.get(5).getAsString();
            final String notes = result.get(6).getAsString();
            final int timestamp = result.get(7).getAsInteger();
            final boolean completed = result.get(8).getAsBoolean();
            final boolean canceled = result.get(9).getAsBoolean();
            final boolean paid = result.get(10).getAsBoolean();
            
            //  Appointment(byte[] appointmentID, byte[] patientID, List<byte[]> staffList, List<byte[]> treatmentTypes, int stationNumber, String treatment, String notes, int timestamp, boolean completionStatus, boolean cancelStatus, boolean paid)
            appts.add(new Appointment(appointmentID, patientID, userID, staff2ID, staff3ID, station, treatment, notes, timestamp, completed, canceled, paid));
        }
        return appts;
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
            chats.add(new Chat(idPair.get(0).getAsBytes(), idPair.get(1).getAsBytes(), pid.getAsBytes()));
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
        // Verify ID exists 
        final boolean idExists = verifyWhere(sid.equalsTo("senderID"), "messagePairTypes", db) && verifyWhere(rid.equalsTo("receiverID"), "messagePairTypes", db);
        if (!idExists) {
            return null;
        }
        // Get stupid pair id
        final DatabaseGenericParameter pid = db.query(String.format("SELECT pairID WHERE %s AND %s",  sid.equalsTo("senderID"), rid.equalsTo("receiverID"))).get(0).get(0);
        final Chat chat  = new Chat(senderID, receiverID, pid.getAsBytes());
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
            // lazy, sorry- "I don't care if it's pretty, atp make sure it just works" - Sammy "The Scrum Master" W. -Kyle
            try {
                wage = db.query("SELECT hrlyWage FROM staff WHERE " + id.equalsTo("staffID")).get(0).get(0).getAsDouble();
            } catch (Exception e) /* this should be index out of bounds exception or something -Kyle */ {
                wage = -1; // negtive values should throw errors in backend, so never pass this value back. only pass to frontend to show to US that something went wrong. -Kyle
            }            
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