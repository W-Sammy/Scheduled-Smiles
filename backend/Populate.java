// This class holds methods for populating and uploading class attributes to and from the database

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import Server.utils.DatabaseGenericParameter;
import static Server.utils.Json.*;
import Server.DatabaseConnection;
import Appointment;
import static Chat.*;
import static User.*; // why are all of these classes in one file? -Kyle
import static Users.Enum.RoleConstant.*;
import static Users.Enum.AppointmentType.*;

public class Populate {
    private Populate() {
        // restrict instantiation -Kyle
    }
    private static boolean verifyWhere(final String where, final String tableName, final DatabaseConnection db) {
        return db.update(String.format("SELECT 1 FROM %s WHERE %s", tableName, where)) > 0;
    }
    public static Appointment populate(byte[] appointmentId, DatabaseConnection db) {
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
        final String queryString = String.format("SELECT %s FROM appointments WHERE %s", String.join(", ", columns), id.equalsTo("appointmentID");
        // clarify with sammy and erds if we are only doing one treatment type per appointment, also change Appointment.typeID to to appointmentID or something, make it more clear. -Kyle
        final List<DatabaseGenericParameter> result = db.query(queryString).get(0);
        // Prepare parameters
        final ArrayList<byte[]> staffList = new ArrayList<byte[]>();
        staffList.add(result.get(1).getAsBytes()); // first staff ID should never be null -Kyle
        if (result.get(2).isNull()) {
            staffList.add(result.get(2).getAsBytes());
        }
        if (result.get(3).isNull()) {
            staffList.add(result.get(3).getAsBytes());
        }
        // Appointment(byte[] typeID, byte[] patientID, ArrayList<byte[]> staffList, int stationNumber, String treatment, String notes, int timestamp, boolean completionStatus, boolean cancelStatus, boolean paid)
        return new Appointment(appointmentId, result.get(0).getAsBytes(), staffList, result.get(4).getAsInteger(), result.get(5).getAsString(), result.get(6).getAsString(), result.get(7).getAsInteger(), result.get(8).getAsBoolean(), result.get(9).getAsBoolean(), result.get(10).getAsBoolean());
    }
    public static Chat populate(final byte[] receiverID, final byte[] senderID, final DatabaseConnection db) {
        final DatabaseGenericParameter sid = new DatabaseGenericParameter(senderID);
        final DatabaseGenericParameter rid = new DatabaseGenericParameter(receiverID);
        final Chat chat  = new Chat(senderID, receiverID);
        // Verify ID exists 
        final boolean idExists = verifyWhere(sid.equalsTo("senderID"), "messages", db) && verifyWhere(rid.equalsTo("receiverID"), "messages", db); // these values dont exist in DB?? -Kyle
        if (!idExists) {
            return null;
        }
        // Aggregate columns
        final String[] columns = {
            "textContent", "createdAt"
        };
        // Get appointment types
        final String queryString = String.format("SELECT %s FROM messages WHERE %s AND %s ORDER BY createAt DESC", String.join(", ", columns), sid.equalsTo("senderID"), rid.equalsTo("receiverID");
        final List<List<DatabaseGenericParameter>> result = db.query(queryString);
        
        for (L row : result) {
            chat.addMessages(row.get(0).getAsString(), row.get(1).getAsInteger());
        }
        // Chat(byte[] senderID, byte[] receiverID)
        return chat;
    }
    public static User populate(final byte[] userID, final DatabaseConnection db) {
        final DatabaseGenericParameter id = new DatabaseGenericParameter(userID);
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
        // Get roleID
        final byte[] roleID = db.query("SELECT roleID FROM users WHERE " + id.equalsTo("userID")).get(0).get(0).getAsBytes();
        double hrlyWage = 0.0;
        // Get role-specific info
        if (Arrays.equals(roleID, ROLE_IDS.get("Admin")) || Arrays.equals(roleID, ROLE_IDS.get("Staff"))) {
            hrlyWage = db.query("SELECT hrlyWage WHERE " + id.equalsTo("staffID")).get(0).get(0).getAsDouble();
        }
        // Construct query
        final String queryString = String.format("SELECT %s FROM users WHERE %s", String.join(", ", columns), id.equalsTo("userID"));
        final List<DatabaseGenericParameter> result = db.query(queryString).get(0); // only get first result (there should only be one anyways)
        // Patient(byte[] userID, byte[] roleID, String firstName, String lastName, String address, char sex, String phoneNumber, String email, int birthDate)
        // Staff(byte[] userID, byte[] roleID, String firstName, String lastName, String address, char sex, String phoneNumber, String email, int birthDate, double hourlyRate)
        // Admin(byte[] userID, byte[] roleID, String firstName, String lastName, String address, char sex, String phoneNumber, String email, int birthDate, double hourlyRate)
        
        // Theres probably a better way to do this but im too tired -Kyle
        if (Arrays.equals(roleID, ROLE_IDS.get("Admin"))) {
            return new Admin(userId, result.get(0).getAsBytes(), result.get(1).getAsString(), result.get(2).getAsString(), result.get(3).getAsChar(), result.get(4).getAsString(), result.get(5).getAsString(), result.get(6).getAsInteger(), hrlyWage);
        } else if (Arrays.equals(roleID, ROLE_IDS.get("Staff"))) {
            return new Staff(userId, result.get(0).getAsBytes(), result.get(1).getAsString(), result.get(2).getAsString(), result.get(3).getAsChar(), result.get(4).getAsString(), result.get(5).getAsString(), result.get(6).getAsInteger(), hrlyWage);
        } else if (!Arrays.equals(roleID, ROLE_IDS.get("Patient"))) {
            return new Patient(userId, result.get(0).getAsBytes(), result.get(1).getAsString(), result.get(2).getAsString(), result.get(3).getAsChar(), result.get(4).getAsString(), result.get(5).getAsString(), result.get(6).getAsInteger());
        } else {
            return null;
        }
    }
}