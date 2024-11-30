package Server.utils;

import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.File;
import java.net.URLDecoder;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.Headers;
// Need to import external library for reading JSON strings and files. -Kyle
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import Users.*;
import static Users.Enum.RoleConstant.*;
import static Users.Enum.AppointmentType.*;

import static Server.utils.Populate.*;
import static Server.Enum.HttpConstants.*;
import static Server.Enum.Pages.*;
import static Server.utils.FileHandler.*;
import static Server.utils.Requests.*;
import static Server.utils.Json.*;
import Server.DatabaseConnection;
import Server.utils.DatabaseGenericParameter;

public class ServerConnectionHandler implements HttpHandler {
    // settings
    private static int responseBufferSize = 0x10000;
    // Runtime constants
    private static String serverContext;
    private static String localContext;
    // request attributes
    private static Map<String, String> requestParameters;
    private static String[] requestPath;
    private static String requestMethod;
    private static String requestFragment;
    private static URI requestUri;
    private static Headers requestHeaders;
    private static Headers responseHeaders;
    private static JsonElement requestBodyJson;
    // Values will close when HttpExchange connection object closes
    private static volatile HttpExchange con;
    private static volatile OutputStream responseBodyStream;
    private static volatile InputStream requestBodyStream;
    
    public ServerConnectionHandler(final String serverCntxt, final String localCntxt) {
        serverContext = serverCntxt;
        localContext = localCntxt;
    }
    
    private static void populate(final HttpExchange conn) throws IOException {
        con = conn;
        requestMethod = con.getRequestMethod().toUpperCase();
        requestUri = con.getRequestURI();
        requestPath = requestUri.getRawPath().split("/"); // first element is always empty since path starts with "/" by default. -Kyle
        requestParameters = getRequestQueryParameters(requestUri.getRawQuery());
        requestFragment = getRequestFragment(requestUri.getRawFragment()); // currently broken- uri does not contain fragment for some reason -Kyle
        requestBodyStream = con.getRequestBody();
        responseBodyStream = con.getResponseBody();
        requestHeaders = con.getRequestHeaders();
        responseHeaders = con.getResponseHeaders();
        requestBodyJson = convertToJsonElement(requestBodyStream);
    }
    
    private static void setReponseContentTypeHeaders(final String filename) {
        final String fileType = getFileType(filename);
        switch (fileType) {
            case ".css":
                responseHeaders.set(HEADER_KEYS.get("CONTENT_TYPE"), HEADER_TYPES.get("CSS"));
            break;
            case ".js":
                responseHeaders.set(HEADER_KEYS.get("CONTENT_TYPE"), HEADER_TYPES.get("JS"));
            break;
            case ".htm":
            case ".html":
                responseHeaders.set(HEADER_KEYS.get("CONTENT_TYPE"), HEADER_TYPES.get("HTML"));
            break;
            // Might not need these last two: txt, json
            case ".txt":
                responseHeaders.set(HEADER_KEYS.get("CONTENT_TYPE"), HEADER_TYPES.get("TEXT"));
            break;
            case ".json":
                responseHeaders.set(HEADER_KEYS.get("CONTENT_TYPE"), HEADER_TYPES.get("JSON"));
            break;
            default:
                // Sending unknown filetype
                responseHeaders.set(HEADER_KEYS.get("CONTENT_DISPOSITION"), "attachment; filename=" + filename);
        }
    }
    
    private static void sendResponse(final int statusCode, final String contentType, final int responseBodyLength, final InputStream responseBody) throws IOException {
        responseHeaders.set(HEADER_KEYS.get("CONTENT_TYPE"), contentType);
        sendResponse(statusCode, responseBodyLength, responseBody);
    }
    
    private static void sendResponse(final int statusCode, final long responseBodyLength, final InputStream responseBody) throws IOException {
        con.sendResponseHeaders(statusCode, responseBodyLength);
        final byte[] buffer = new byte[responseBufferSize];
        int count = 0;
        try {
            while ((count = responseBody.read(buffer)) >= 0) {
                responseBodyStream.write(buffer, 0, count);
            }
        } catch (IOException e) {
            // Specifically happens when requesting wrong type from the database (confirmed trigger by requesting for hashes as string type, instead of hex) -Kyle
            System.out.println("ServerHandler Error: Too many bytes to write to stream. Has responseBodyLength (" + responseBodyLength + ") been defined correctly?");
        }
    }
    
    private static void sendResponse(final int statusCode, final String responseBody) throws IOException {
        final InputStream is = new ByteArrayInputStream(responseBody.getBytes(CHARSET));
        sendResponse(statusCode, HEADER_TYPES.get("TEXT"), responseBody.length(), is);
    }
    private static void sendResponse(final int statusCode, final JsonElement responseBody) throws IOException {
        final String responseBodyJson = convertFromJson(responseBody);
        final InputStream is = new ByteArrayInputStream(responseBodyJson.getBytes(CHARSET));
        sendResponse(statusCode, HEADER_TYPES.get("JSON"), responseBodyJson.length(), is);
    }
    private static void sendResponse(final int statusCode, final Object responseBody) throws IOException {
        final String responseBodyJson = convertFromJson(responseBody);
        final InputStream is = new ByteArrayInputStream(responseBodyJson.getBytes(CHARSET));
        sendResponse(statusCode, HEADER_TYPES.get("JSON"), responseBodyJson.length(), is);
    }
    
    private static void sendFileResponse() throws IOException {
        try (final OutputStream responseBodyStream = con.getResponseBody()) {
            // Getting file
            final File file = getFile(String.join("/", requestPath), serverContext, localContext);
            if (file != null) {
                // Determine filetype
                final String filename = file.getName();
                setReponseContentTypeHeaders(filename);
                // Sending file bytestream
                try (FileInputStream fs = new FileInputStream(file)) {
                    sendResponse(STATUS_CODES.get("OK"), file.length(), fs);
                }
            } else {
                // TODO: handle file not found differently based on the type of file requested -Kyle
                sendResponse(STATUS_CODES.get("NOT_FOUND"), "File: " + String.join("/", requestPath) + " not found.");
            }
        }
    }
    
    private static boolean handleDatabaseRequest() throws IOException {
        final JsonObject requestJsonObject = requestBodyJson.getAsJsonObject();
        String results = "";
        if (!membersMatch(requestJsonObject.keySet(), "query"))
            return false;
        try (DatabaseConnection db = new DatabaseConnection()) {
            if (db.isConnected()) {
                final String queryString = requestJsonObject.get("query").getAsString();
                switch (requestPath[3]) {
                    case "get":
                        results = convertFromJson(db.query(queryString));
                    break;
                    case "set":
                        results = convertFromJson(db.update(queryString));
                    break;
                    default:
                        return false;
                }
            }
        } catch (Exception e) {
            // Errors handled in DatabaseConnection, pass
            e.printStackTrace();
        }
        sendResponse(STATUS_CODES.get("OK"), convertToJsonElement(results));
        return true;
    }
    
    private static boolean registerUser() throws IOException {
        final JsonObject requestJsonObject = requestBodyJson.getAsJsonObject();
        // Collect fields and verify
        if (!membersMatch(requestJsonObject.keySet(), "firstName", "lastName", "address", "sex", "phone", "email", "birthDate", "password"))
            return false;
        final String sexStr = requestJsonObject.get("sex").getAsString().toUpperCase();
        final String phoneNumberStr = requestJsonObject.get("phone").getAsString();
        final String[] columns = {
            "email", "hashedPass", "firstName", "lastName", "sex", "birthDate", "address", "phoneNumber", "roleID" // we don't insert detail section(added later by staff) or userID(generated by DB) on account creation -Kyle 
        };
        if (
            !(sexStr.length() == 1 && (sexStr.equals("M") || sexStr.equals("F")))
            || phoneNumberStr.length() != 10
        )
            return false;
        
        int result = 0;
        // Collect the rest of the values as strings
        final String emailStr = requestJsonObject.get("email").getAsString();
        final String passwordStr = requestJsonObject.get("password").getAsString();
        final String firstNameStr = requestJsonObject.get("firstName").getAsString();
        final String lastNameStr = requestJsonObject.get("lastName").getAsString();
        final int birthDateInt = requestJsonObject.get("birthDate").getAsInt();
        final String addressStr = requestJsonObject.get("address").getAsString();
        
        // Determine roleID by email
        final byte[] roleIdDigest = getRoleId(emailStr);
                    
        // Generate enocde password before sending to DB
        final byte[] passwordDigest = hash256(passwordStr.getBytes(CHARSET));
        
        // Prepare values for query
        final DatabaseGenericParameter email = new DatabaseGenericParameter(emailStr);
        final DatabaseGenericParameter password = new DatabaseGenericParameter(passwordDigest);
        final DatabaseGenericParameter firstName = new DatabaseGenericParameter(firstNameStr);
        final DatabaseGenericParameter lastName = new DatabaseGenericParameter(lastNameStr);
        final DatabaseGenericParameter sex = new DatabaseGenericParameter(sexStr);
        final DatabaseGenericParameter birthDate = new DatabaseGenericParameter(birthDateInt); // need to switch to long type instead of int if we want to support past 2038 -Kyle
        final DatabaseGenericParameter address = new DatabaseGenericParameter(addressStr);
        final DatabaseGenericParameter phoneNumber = new DatabaseGenericParameter(phoneNumberStr);
        final DatabaseGenericParameter roleId = new DatabaseGenericParameter(roleIdDigest);
        
        // Aggregate values
        final String[] values = {
            email.getAsParameter(),
            password.getAsParameter(),
            firstName.getAsParameter(),
            lastName.getAsParameter(),
            sex.getAsParameter(),
            birthDate.getAsParameter(),
            address.getAsParameter(),
            phoneNumber.getAsParameter(),
            roleId.getAsParameter()
        };
        
        final String queryString = String.format("INSERT INTO users (%s) VALUES (%s)", String.join(", ", columns), String.join(", ", values));
        // testing
        try (DatabaseConnection db = new DatabaseConnection()) {
            if (db.isConnected()) {
                result = db.update(queryString);
            }
        } catch (Exception e) {
            // Errors handled in DatabaseConnection, pass
            e.printStackTrace();
        }
        final String resultStr = (result > 0) ? "true" : "false";
        sendResponse(STATUS_CODES.get("OK"), convertToJsonElement(resultStr));
        return true;
    }
    
    private static boolean verifyRowExists() throws IOException {
        System.out.println("1");
        final JsonObject requestJsonObject = requestBodyJson.getAsJsonObject();
        System.out.println("2");
        if (!membersMatch(requestJsonObject.keySet(), "query"))
            System.out.println("2.5");
            return false;
        String results = "";
        final String queryString = requestJsonObject.get("query").getAsString();
        System.out.println("3");
        try (DatabaseConnection db = new DatabaseConnection()) {
            System.out.println("4");
            if (db.isConnected()) {
                System.out.println(db.query(queryString));
                results = (convertFromJson(db.query(queryString)).equals("[]")) ? "false" : "true";
            }
        } catch (Exception e) {
            System.out.println("5");
            // Errors handled in DatabaseConnection, pass
            e.printStackTrace();
        } finally {
            sendResponse(STATUS_CODES.get("OK"), convertToJsonElement(results));
            return true;
        }
    }
    
    private static boolean getUser() throws IOException {
        System.out.println("Called getUser()");
        final JsonObject requestJsonObject = requestBodyJson.getAsJsonObject();
        if (membersMatch(requestJsonObject.keySet(), "userID")) {
            try (DatabaseConnection db = new DatabaseConnection()) {
                if (db.isConnected()) {
                    final User user = populateUser(unhex(requestJsonObject.get("userID").getAsString()), db);
                    sendResponse(STATUS_CODES.get("OK"), user);
                    return true;
                }
            } catch (Exception e) {
                // Errors handled in DatabaseConnection, pass
                e.printStackTrace();
            }
        } else if (membersMatch(requestJsonObject.keySet(), "email", "password")) {
            final byte[] passwordDigest = hash256(requestJsonObject.get("password").getAsString());
            final DatabaseGenericParameter email = new DatabaseGenericParameter(requestJsonObject.get("email").getAsString());
            final DatabaseGenericParameter password = new DatabaseGenericParameter(passwordDigest);
            final String queryString = String.format("SELECT userID FROM users WHERE %s AND %s", email.equalsTo("email"), password.equalsTo("hashedPass"));
            System.out.println(queryString);
            try (DatabaseConnection db = new DatabaseConnection()) {
                if (db.isConnected()) {
                    final List<List<DatabaseGenericParameter>> results = db.query(queryString);
                    if (results != null) {
                        if (!results.get(0).get(0).isNull()) { // second check might be redundant
                            final User user = populateUser(results.get(0).get(0).getAsBytes(), db);
                            sendResponse(STATUS_CODES.get("OK"), user);
                            return true;
                        }
                    } else {
                        sendResponse(STATUS_CODES.get("OK"), convertToJsonElement("false"));
                        return true;
                    }
                }
            } catch (Exception e) {
                // Errors handled in DatabaseConnection, pass
                e.printStackTrace();
            }
        } else {
            return false;
        }
        sendResponse(STATUS_CODES.get("OK"), convertToJsonElement("false"));
        return true;
    }
    // The only reason this functions exists, does what it does, and is only a single function, is because of lack of time and skill (on my part). Most of it's behavior will likely be undocumented, so im sorry. -Kyle
    private static boolean lookupRoleValue() throws IOException {
        final JsonObject requestJsonObject = requestBodyJson.getAsJsonObject();
        final Set<String> requestKeys = requestJsonObject.keySet();
        String returnValue;
        if (requestKeys.size() != 1)
            return false;
        final String requestKey = requestKeys.iterator().next();
        final String requestValue = requestJsonObject.get(requestKey).getAsString();
        switch (requestKey) {
            case "roleId":
                returnValue = getRoleName(unhex(requestValue));
            break;
            case "roleName":
                returnValue = (ROLE_IDS.containsKey(requestValue)) ? hex(ROLE_IDS.get(requestValue)) : null;
            break;
            default:
                return false;
        }
        sendResponse(STATUS_CODES.get("OK"), convertToJsonElement(returnValue));
        return true;
    }
    // this does it's best to follow the privacy protection promises we made -Kyle
    private static boolean lookupStaffAvailability() throws IOException {
        final JsonObject requestJsonObject = requestBodyJson.getAsJsonObject();
        final Set<String> requestKeys = requestJsonObject.keySet();
        if (!membersMatch(requestKeys, "startTime"))
            return false;
        final int oneHour = 60 * 60; // in seconds
        final int timestamp = requestJsonObject.get("startTime").getAsInt();
        final int before = timestamp - oneHour;
        final int after = timestamp + oneHour;
        final String queryString = "SELECT staffID FROM staff WHERE staffID NOT IN ( " + String.join(" UNION ",
            String.format("SELECT staff1ID FROM appointments WHERE startTime < %s AND startTime > %s", after, before),
            String.format("SELECT staff2ID FROM appointments WHERE startTime < %s AND startTime > %s", after, before),
            String.format("SELECT staff3ID FROM appointments WHERE startTime < %s AND startTime > %s", after, before)
        ) + " )";
        JsonElement result = convertToJsonElement("[]");
        try (DatabaseConnection db = new DatabaseConnection()) {
            if (db.isConnected()) {
                final ArrayList<String> resultArray = new ArrayList<String>();
                final List<List<DatabaseGenericParameter>> rawResult = db.query(queryString);
                for (List<DatabaseGenericParameter> l : rawResult) {
                    resultArray.add(
                        l.get(0).toString()
                    );
                }
                result = convertToJsonElement(resultArray);
            }
        } catch (Exception e) {
            // Errors handled in DatabaseConnection, pass
            e.printStackTrace();
        }
        sendResponse(STATUS_CODES.get("OK"), result);
        return true;
    }
    
    private static boolean handleLookupRequest() throws IOException {
        if (requestPath.length >= 4) {
            switch (requestPath[3]) {
                case "role":
                    return lookupRoleValue();
                case "availability":
                    return lookupStaffAvailability();
            }
        }
        return false;
    }
    private static boolean bookAppointment() throws IOException {
        final JsonObject requestJsonObject = requestBodyJson.getAsJsonObject();
        final Set<String> requestKeys = requestJsonObject.keySet();
        final Set<String> columns = Set.of("patientID", "staff1ID", "staff2ID", "staff3ID", "startTime");
        if (!membersMatch(requestKeys, columns))
            return false;
        final DatabaseGenericParameter patientID = new DatabaseGenericParameter(requestJsonObject.get("patientID").getAsString(), "bytes");
        final DatabaseGenericParameter staff1ID = new DatabaseGenericParameter(requestJsonObject.get("staff1ID").getAsString(), "bytes");
        final DatabaseGenericParameter staff2ID = (requestJsonObject.get("staff2ID").getAsString().equals("null")) ? new DatabaseGenericParameter() : new DatabaseGenericParameter(requestJsonObject.get("staff2ID").getAsString(), "bytes");
        final DatabaseGenericParameter staff3ID = (requestJsonObject.get("staff3ID").getAsString().equals("null")) ? new DatabaseGenericParameter() : new DatabaseGenericParameter(requestJsonObject.get("staff3ID").getAsString(), "bytes");
        final DatabaseGenericParameter startTime = new DatabaseGenericParameter(requestJsonObject.get("startTime").getAsInt());
        
        // ORDER MUST MATCH columns VARIABLE!!
        final String[] values = {
            patientID.getAsParameter(),
            staff1ID.getAsParameter(),
            staff2ID.getAsParameter(),
            staff3ID.getAsParameter(),
            startTime.getAsParameter()
        };
        int result = 0;
        final String queryString = String.format("INSERT INTO appointments (%s, isPaid, isCanceled, isComplete) VALUES (%s, 0, 0, 0)", String.join(", ", columns), String.join(", ", values));
        try (DatabaseConnection db = new DatabaseConnection()) {
            if (db.isConnected()) {
                result = db.update(queryString);
            }
        } catch (Exception e) {
            // Errors handled in DatabaseConnection, pass
            e.printStackTrace();
        }
        final String resultStr = (result > 0) ? "true" : "false";
        sendResponse(STATUS_CODES.get("OK"), convertToJsonElement(resultStr));
        return true;
    }
    private static boolean updateAppointment() throws IOException {
        final JsonObject requestJsonObject = requestBodyJson.getAsJsonObject();
        final Set<String> requestKeys = requestJsonObject.keySet();
        int result = 0;
        String queryString = "";
        if (membersMatch(requestKeys, "appointmentID", "stationNumber", "treatment", "notes")) {
            final DatabaseGenericParameter appointmentID = new DatabaseGenericParameter(requestJsonObject.get("appointmentID").getAsString(), "bytes");
            final DatabaseGenericParameter stationNumber = new DatabaseGenericParameter(requestJsonObject.get("stationNumber").getAsInt());
            final DatabaseGenericParameter treatment = new DatabaseGenericParameter(requestJsonObject.get("treatment").getAsString());
            final DatabaseGenericParameter notes = new DatabaseGenericParameter(requestJsonObject.get("notes").getAsString());
            queryString = String.format("UPDATE appointments SET %s, %s, %s WHERE %s", stationNumber.equalsTo("stationNumber"), treatment.equalsTo("treatment"), notes.equalsTo("notes"), appointmentID.equalsTo("appointmentID"));
        } else if (membersMatch(requestKeys, "appointmentID", "isCanceled")) {
            final DatabaseGenericParameter appointmentID = new DatabaseGenericParameter(requestJsonObject.get("appointmentID").getAsString(), "bytes");
            final DatabaseGenericParameter value = new DatabaseGenericParameter(requestJsonObject.get("isCanceled").getAsBoolean());
            queryString = String.format("UPDATE appointments SET %s WHERE %s", value.equalsTo("isCanceled"), appointmentID.equalsTo("appointmentID"));
        } else if (membersMatch(requestKeys, "appointmentID", "isComplete")) {
            final DatabaseGenericParameter appointmentID = new DatabaseGenericParameter(requestJsonObject.get("appointmentID").getAsString(), "bytes");
            final DatabaseGenericParameter value = new DatabaseGenericParameter(requestJsonObject.get("isComplete").getAsBoolean());
            queryString = String.format("UPDATE appointments SET %s WHERE %s", value.equalsTo("isComplete"), appointmentID.equalsTo("appointmentID"));
        } else if (membersMatch(requestKeys, "appointmentID", "isPaid")) {
            final DatabaseGenericParameter appointmentID = new DatabaseGenericParameter(requestJsonObject.get("appointmentID").getAsString(), "bytes");
            final DatabaseGenericParameter value = new DatabaseGenericParameter(requestJsonObject.get("isPaid").getAsBoolean());
            queryString = String.format("UPDATE appointments SET %s WHERE %s", value.equalsTo("isPaid"), appointmentID.equalsTo("appointmentID"));
        } else {
            return false;
        }
        try (DatabaseConnection db = new DatabaseConnection()) {
            if (db.isConnected()) {
                result = db.update(queryString);
            }
        } catch (Exception e) {
            // Errors handled in DatabaseConnection, pass
            e.printStackTrace();
        }
        final String resultStr = (result > 0) ? "true" : "false";
        sendResponse(STATUS_CODES.get("OK"), convertToJsonElement(resultStr));
        return true;
    }
    private static boolean getAppointment() throws IOException {
        final JsonObject requestJsonObject = requestBodyJson.getAsJsonObject();
        if (membersMatch(requestJsonObject.keySet(), "appointmentID")) {
            try (DatabaseConnection db = new DatabaseConnection()) {
                if (db.isConnected()) {
                    final Appointment appt = populateAppt(unhex(requestJsonObject.get("appointmentID").getAsString()), db);
                    sendResponse(STATUS_CODES.get("OK"), appt);
                    return true;
                }
            } catch (Exception e) {
                // Errors handled in DatabaseConnection, pass
                e.printStackTrace();
            }
        } else {
            return false;
        }
        sendResponse(STATUS_CODES.get("OK"), convertToJsonElement("false"));
        return true;
    }
    
    private static boolean getAppointments() throws IOException {
        final JsonObject requestJsonObject = requestBodyJson.getAsJsonObject();
        if (membersMatch(requestJsonObject.keySet(), "userID")) {
            try (DatabaseConnection db = new DatabaseConnection()) {
                if (db.isConnected()) {
                    final List<Appointment> appts = populateAppts(unhex(requestJsonObject.get("userID").getAsString()), db);
                    sendResponse(STATUS_CODES.get("OK"), convertToJsonElement(appts));
                    return true;
                }
            } catch (Exception e) {
                // Errors handled in DatabaseConnection, pass
                e.printStackTrace();
            }
        } else {
            return false;
        }
        sendResponse(STATUS_CODES.get("OK"), convertToJsonElement("false"));
        return true;
    }
    
    // unused
    private static boolean bookFullAppointment() throws IOException {
        final JsonObject requestJsonObject = requestBodyJson.getAsJsonObject();
        final Set<String> requestKeys = requestJsonObject.keySet();
        final Set<String> columns = Set.of("patientID", "staff1ID", "staff2ID", "staff3ID", "stationNumber", "treatment", "notes", "startTime", "isComplete", "isCanceled", "isPaid");
        if (!membersMatch(requestKeys, columns))
            return false;
        final DatabaseGenericParameter patientID = new DatabaseGenericParameter(requestJsonObject.get("patientID").getAsString(), "bytes");
        final DatabaseGenericParameter staff1ID = new DatabaseGenericParameter(requestJsonObject.get("staff1ID").getAsString(), "bytes");
        final DatabaseGenericParameter staff2ID = new DatabaseGenericParameter(requestJsonObject.get("staff2ID").getAsString(), "bytes");
        final DatabaseGenericParameter staff3ID = new DatabaseGenericParameter(requestJsonObject.get("staff3ID").getAsString(), "bytes");
        final DatabaseGenericParameter stationNumber = new DatabaseGenericParameter(requestJsonObject.get("stationNumber").getAsInt());
        final DatabaseGenericParameter treatment = new DatabaseGenericParameter(requestJsonObject.get("treatment").getAsString());
        final DatabaseGenericParameter notes = new DatabaseGenericParameter(requestJsonObject.get("notes").getAsString());
        final DatabaseGenericParameter startTime = new DatabaseGenericParameter(requestJsonObject.get("startTime").getAsInt());
        final DatabaseGenericParameter isComplete = new DatabaseGenericParameter(requestJsonObject.get("isComplete").getAsBoolean());
        final DatabaseGenericParameter isCanceled = new DatabaseGenericParameter(requestJsonObject.get("isCanceled").getAsBoolean());
        final DatabaseGenericParameter isPaid = new DatabaseGenericParameter(requestJsonObject.get("isPaid").getAsBoolean());
        
        // ORDER MUST MATCH columns VARIABLE!!
        final String[] values = {
            patientID.getAsParameter(),
            staff1ID.getAsParameter(),
            staff2ID.getAsParameter(),
            staff3ID.getAsParameter(),
            stationNumber.getAsParameter(),
            treatment.getAsParameter(),
            notes.getAsParameter(),
            startTime.getAsParameter(),
            isComplete.getAsParameter(),
            isCanceled.getAsParameter(),
            isPaid.getAsParameter()
        };
        int result = 0;
        final String queryString = String.format("INSERT INTO appointments (%s) VALUES (%s)", String.join(", ", columns), String.join(", ", values));
        try (DatabaseConnection db = new DatabaseConnection()) {
            if (db.isConnected()) {
                result = db.update(queryString);
            }
        } catch (Exception e) {
            // Errors handled in DatabaseConnection, pass
            e.printStackTrace();
        }
        final String resultStr = (result > 0) ? "true" : "false";
        sendResponse(STATUS_CODES.get("OK"), convertToJsonElement(resultStr));
        return true;
    }

    // This method will Volkswagen you!
    // This returns ALL messages to ALL contacts in a single response body. This function will likely NOT WORK or take EXTREMELY LONG TO LOAD if the server device and client devices are seperate! (i.e. outside of demo) -Kyle
    private static boolean getChats() throws IOException {
        final JsonObject requestJsonObject = requestBodyJson.getAsJsonObject();
        final Set<String> requestKeys = requestJsonObject.keySet();
        if (!membersMatch(requestKeys, "userID"))
            return false;
        final DatabaseGenericParameter receiverID = new DatabaseGenericParameter(requestJsonObject.get("userID").getAsString(), "bytes");
        JsonElement result = convertToJsonElement("[]");
        try (DatabaseConnection db = new DatabaseConnection()) {
            if (db.isConnected()) {
                final List<Chat> all = populateChat(receiverID.getAsBytes(), db);
                result = convertToJsonElement(all);
            }
        } catch (Exception e) {
            // Errors handled in DatabaseConnection, pass
            e.printStackTrace();
        }
        sendResponse(STATUS_CODES.get("OK"), result);
        return true;
    }

    private static boolean sendMessage() throws IOException {
        final JsonObject requestJsonObject = requestBodyJson.getAsJsonObject();
        final Set<String> requestKeys = requestJsonObject.keySet();
        int result = 0;
        if (!membersMatch(requestKeys, "senderID", "receiverID", "content"))
            return false;
        final DatabaseGenericParameter senderID = new DatabaseGenericParameter(requestJsonObject.get("senderID").getAsString(), "bytes");
        final DatabaseGenericParameter receiverID = new DatabaseGenericParameter(requestJsonObject.get("receiverID").getAsString(), "bytes");
        final DatabaseGenericParameter content = new DatabaseGenericParameter(requestJsonObject.get("content").getAsString());
        final String queryString = String.format("CALL insertMessage(%s, %s, %s)", senderID.getAsParameter(), receiverID.getAsParameter(), content.getAsParameter());
        try (DatabaseConnection db = new DatabaseConnection()) {
            if (db.isConnected()) {
                result = db.update(queryString);
            }
        } catch (Exception e) {
            // Errors handled in DatabaseConnection, pass
            e.printStackTrace();
        }
        final String resultStr = (result > 0) ? "true" : "false";
        sendResponse(STATUS_CODES.get("OK"), convertToJsonElement(resultStr));
        return true;
    }
    
    private static boolean getAppointmentTypes() throws IOException {
        sendResponse(STATUS_CODES.get("OK"), convertToJsonElement(APPOINTMENT_TYPE_FEE));
        return true;
    }
    
    private static void handleApiRequest() throws IOException {
        boolean isValidRequest = (requestBodyJson != null && requestPath.length >= 3); // Used to determine if an error reponse needs to be sent after checking switch cases
        if (isValidRequest) {
            switch (requestMethod) {
                case "GET":

                break;
                case "POST":
                    isValidRequest = switch (requestPath[2]) {
                        case "verify" -> verifyRowExists();
                        case "login" -> getUser();
                        case "database" -> handleDatabaseRequest();
                        case "register" -> registerUser();
                        case "lookup" -> handleLookupRequest();
                        case "book-appointment" -> bookAppointment();
                        case "messages" -> getChats();
                        case "message" -> sendMessage();
                        case "update-appointment" -> updateAppointment();
                        case "get-appointment" -> getAppointment();
                        case "get-appointments" -> getAppointments();
                        case "get-appointment-types" -> getAppointmentTypes();
                        default -> false;
                    };
                break;
                // This is where we'd have a PUT case for uploading images... if we were going to implement that. -Kyle
                // we are not. -Kyle from 2 weeks later
                default:
                    isValidRequest = false;
            }
        }
        if (isValidRequest) {
            System.out.println("API request finished with response");
        } else {
            System.out.println("API request was found to be invalid");
            // Malformed/Invalid request body
            sendResponse(STATUS_CODES.get("BAD_REQUEST"), "Malformed request body or invalid endpoint");
        }
    }
    
    private static void handleRequest() throws IOException {
        switch (requestMethod) {
            case "GET":
                // Get request parameters
                System.out.println("Got a GET request: " + requestUri.toString());
                sendFileResponse();
            break;
            case "POST": // TODO: Non-api POST calls should return invalid. -Kyle
                System.out.println("Got a POST request: " + requestUri.toString());
            
            break;
            case "OPTIONS":
                System.out.println("Got an OPTIONS request: " + requestUri.toString());
                responseHeaders.set(HEADER_KEYS.get("ALLOW"), ALLOWED_METHODS);
                con.sendResponseHeaders(STATUS_CODES.get("OK"), NO_RESPONSE_LENGTH);
            break;
            default:
                System.out.println("Got an invalid request: " + requestUri.toString());
                con.sendResponseHeaders(STATUS_CODES.get("NOT_ALLOWED"), NO_RESPONSE_LENGTH);
        }
    }
    
    @Override
    public void handle(final HttpExchange conn) throws IOException {
        try {
            // Get response details
            populate(conn);
            // this first IF case is for testing only, remove before deployment -Kyle
            if (requestParameters.containsKey("debug") && requestParameters.get("debug").equals("testingtool")) {
                final String testingHtmlB64 = new String(java.util.Base64.getDecoder().decode(
                "PCFET0NUWVBFIGh0bWw+CjxodG1sPgogIDxoZWFkPgogICAgPHRpdGxlPlRoaXMgaXMgdGhlIHRpdGxlIG9mIHRoZSB3ZWJwYWdlITwvdGl0bGU+CiAgICA8IS0tIFJlbmRlcmluZyBtYXJrZG93biAtLT4KICAgIDxzY3JpcHQgc3JjPSJodHRwczovL2Nkbi5qc2RlbGl2ci5uZXQvbnBtL21hcmtkb3duLWl0QDE0LjEuMC9kaXN0L21hcmtkb3duLWl0Lm1pbi5qcyI+PC9zY3JpcHQ+CiAgICA8c2NyaXB0PgogICAgICAgIGZ1bmN0aW9uIG1haW4oKSB7CiAgICAgICAgICAgIHZhciBbZW5kcG9pbnQsIG1ldGhvZCwgYm9keV0gPSBnZXREYXRhKCkKICAgICAgICAgICAgc2VuZFJlcXVlc3QoZW5kcG9pbnQsIG1ldGhvZCwgYm9keSkKICAgICAgICAgICAgCiAgICAgICAgfQoKICAgICAgICBmdW5jdGlvbiBnZXREYXRhKCkgewogICAgICAgICAgICBjb25zdCByZXF1ZXN0Qm9keUFyZWEgPSBkb2N1bWVudC5nZXRFbGVtZW50QnlJZCgicmVxdWVzdEJvZHkiKQogICAgICAgICAgICBjb25zdCBlbmRwb2ludEFyZWEgPSBkb2N1bWVudC5nZXRFbGVtZW50QnlJZCgiZW5kcG9pbnQiKQogICAgICAgICAgICBjb25zdCBtZXRob2RzU2VsZWN0ID0gZG9jdW1lbnQuZ2V0RWxlbWVudEJ5SWQoIm1ldGhvZHMiKQogICAgICAgICAgICB2YXIgcmVxdWVzdEJvZHkgPSByZXF1ZXN0Qm9keUFyZWEudmFsdWUKICAgICAgICAgICAgdmFyIG1ldGhvZCA9IG1ldGhvZHNTZWxlY3QudmFsdWUKICAgICAgICAgICAgdmFyIGVuZHBvaW50ID0gZW5kcG9pbnRBcmVhLnZhbHVlCiAgICAgICAgICAgIHJldHVybiBbZW5kcG9pbnQsIG1ldGhvZCwgcmVxdWVzdEJvZHldCiAgICAgICAgfQoKICAgICAgICBmdW5jdGlvbiBzZW5kUmVxdWVzdChlbmRwb2ludCwgbWV0aG9kLCByZXF1ZXN0Qm9keSkgewogICAgICAgICAgICBjb25zb2xlLmxvZygiU2VuZGluZyAiICsgbWV0aG9kICsgIiB0byAiICsgZW5kcG9pbnQgKyAiOlxuIiArIHJlcXVlc3RCb2R5KTsKICAgICAgICAgICAgZmV0Y2god2luZG93LmxvY2F0aW9uLm9yaWdpbiArIGVuZHBvaW50LCB7CiAgICAgICAgICAgICAgICBtZXRob2Q6IG1ldGhvZCwKICAgICAgICAgICAgICAgIG1vZGU6ICJuby1jb3JzIiwKICAgICAgICAgICAgICAgIGJvZHk6IEpTT04uc3RyaW5naWZ5KEpTT04ucGFyc2UocmVxdWVzdEJvZHkpKSwKICAgICAgICAgICAgICAgIGhlYWRlcnM6IHsKICAgICAgICAgICAgICAgICAgICAnQWNjZXB0JzogJ2FwcGxpY2F0aW9uL2pzb24nLAogICAgICAgICAgICAgICAgICAgICJDb250ZW50LXR5cGUiOiAiYXBwbGljYXRpb24vanNvbjsgY2hhcnNldD1VVEYtOCIKICAgICAgICAgICAgICAgIH0KICAgICAgICAgICAgfSkudGhlbihyZXNwb25zZSA9PiB7CiAgICAgICAgICAgICAgICByZXR1cm4gUHJvbWlzZS5hbGwoW3Jlc3BvbnNlLnN0YXR1cywgcmVzcG9uc2UudGV4dCgpXSkKICAgICAgICAgICAgfSwgbmV0d29ya0Vycm9yID0+IHsKICAgICAgICAgICAgICBjb25zb2xlLmxvZyhuZXR3b3JrRXJyb3IubWVzc2FnZSkKICAgICAgICAgICAgICBsb2FkUmVzcG9uc2UoIiIsIG5ldHdvcmtFcnJvci5tZXNzYWdlKQogICAgICAgICAgICB9KS50aGVuKCh2YWx1ZXMpID0+IHsKICAgICAgICAgICAgICB2YXIgcmVzcG9uc2VUZXh0ID0gICh2YWx1ZXNbMF0gPj0gNDAwKSA/IHZhbHVlc1sxXSA6IEpTT04uc3RyaW5naWZ5KEpTT04ucGFyc2UodmFsdWVzWzFdKSwgbnVsbCwgMikKICAgICAgICAgICAgICBjb25zb2xlLmxvZyh2YWx1ZXMpCiAgICAgICAgICAgICAgbG9hZFJlc3BvbnNlKHZhbHVlc1swXSwgcmVzcG9uc2VUZXh0KQogICAgICAgICAgICB9KQogICAgICAgIH0KCiAgICAgICAgLy8gRGV2IG9ubHkuIHJlbW92ZSBiZWZvcmUgZGVwbG95bWVudC4gUmVxdWlyZXMgaW1wb3J0IG9mIHRoZSBtYXJrZWQgSlMgbGlicmFyeSAtS3lsZQogICAgICAgIGZ1bmN0aW9uIHJlbmRlckFwaURvYygpIHsKICAgICAgICAgICAgY29uc3QgbWQgPSBtYXJrZG93bml0KHsKICAgICAgICAgICAgICBodG1sOiB0cnVlLAogICAgICAgICAgICAgIGxpbmtpZnk6IHRydWUsCiAgICAgICAgICAgICAgdHlwb2dyYXBoZXI6IHRydWUKICAgICAgICAgICAgfSkKICAgICAgICAgIGZldGNoKCJodHRwczovL3Jhdy5naXRodWJ1c2VyY29udGVudC5jb20vVy1TYW1teS9TY2hlZHVsZWQtU21pbGVzL3JlZnMvaGVhZHMvYmFja2VuZC9kb2NzL0FQSV9SRUZFUkVOQ0UubWQiKSAgICAgIC8vIFRoZSBwYXRoIHRvIHRoZSByYXcgTWFya2Rvd24gZmlsZQogICAgICAgICAgICAudGhlbihyZXNwb25zZSA9PiByZXNwb25zZS50ZXh0KCkpCiAgICAgICAgICAgIC50aGVuKG1hcmtkb3duID0+IHsKICAgICAgICAgICAgICBkb2N1bWVudC5nZXRFbGVtZW50QnlJZCgiYXBpRG9jIikuaW5uZXJIVE1MID0gbWQucmVuZGVyKG1hcmtkb3duKS8vbWFya2VkLnBhcnNlKG1hcmtkb3duKQogICAgICAgICAgICB9KQogICAgICAgIH0KCiAgICAgICAgd2luZG93Lm9ubG9hZCA9ICgpID0+IHsKICAgICAgICAgICAgcmVuZGVyQXBpRG9jKCkKICAgICAgICB9CiAgICAgICAgICAgIAogICAgICAgIGZ1bmN0aW9uIGxvYWRSZXNwb25zZShyZXNwb25zZVN0YXR1cywgcmVzcG9uc2VUZXh0KSB7CiAgICAgICAgICAgIGNvbnN0IHJlc3BvbnNlQm9keUFyZWEgPSBkb2N1bWVudC5nZXRFbGVtZW50QnlJZCgicmVzcG9uc2VCb2R5IikKICAgICAgICAgICAgY29uc3QgcmVzcG9uc2VTdGF0dXNBcmVhID0gZG9jdW1lbnQuZ2V0RWxlbWVudEJ5SWQoInJlc3BvbnNlU3RhdHVzIikKICAgICAgICAgICAgcmVzcG9uc2VCb2R5QXJlYS52YWx1ZSA9IHJlc3BvbnNlVGV4dAogICAgICAgICAgICByZXNwb25zZVN0YXR1c0FyZWEuaW5uZXJIVE1MID0gcmVzcG9uc2VTdGF0dXMKICAgICAgICB9CiAgICA8L3NjcmlwdD4KICAgIDxzdHlsZT4KICAgICAgICBodG1sLCBib2R5IHsKICAgICAgICAgICAgYmFja2dyb3VuZC1jb2xvcjogIzIzMjcyQTsgLyogTVkgRVlFUyEgKi8KICAgICAgICAgICAgY29sb3I6IHdoaXRlOwogICAgICAgICAgICBmb250LWZhbWlseTogQXJpYWwsIEhlbHZldGljYSwgc2Fucy1zZXJpZjsgLyogbGVzcyBoYXJzaCB0byBsb29rIGF0IGR1cmluZyBkZXYsIHN1YmplY3QgdG8gY2hhbmdlIGluIGFjdHVhbCBhcHAgKi8KICAgICAgICB9CgogICAgICAgIGJvZHkgPiBkaXYgPiAqIHsKICAgICAgICAgICAgZmxvYXQ6IGxlZnQ7CiAgICAgICAgICAgIHBhZGRpbmctcmlnaHQ6IDM1cHg7CiAgICAgICAgfQoKICAgICAgICAjcmVzcG9uc2VTdGF0dXMgewogICAgICAgICAgICBiYWNrZ3JvdW5kLWNvbG9yOiAjNDI0NTQ5OwogICAgICAgICAgICBoZWlnaHQ6IDEuNWVtOwogICAgICAgICAgICB3aWR0aDogMTBlbTsKICAgICAgICAgICAgY29sb3I6IHdoaXRlOwogICAgICAgIH0KCiAgICAgICAgI2VuZHBvaW50IHsKICAgICAgICAgICAgYmFja2dyb3VuZC1jb2xvcjogIzQyNDU0OTsKICAgICAgICAgICAgaGVpZ2h0OiAxLjVlbTsKICAgICAgICAgICAgd2lkdGg6IDQ1ZW07CiAgICAgICAgICAgIGNvbG9yOiB3aGl0ZTsKICAgICAgICB9CgogICAgICAgICNyZXF1ZXN0Qm9keSwgI3Jlc3BvbnNlQm9keSB7CiAgICAgICAgICAgIGJvcmRlci1yYWRpdXM6IDE1cHg7CiAgICAgICAgICAgIGJhY2tncm91bmQtY29sb3I6ICM0MjQ1NDk7CiAgICAgICAgICAgIHBhZGRpbmc6IDEwcHg7CiAgICAgICAgICAgIGhlaWdodDogMjAwcHg7CiAgICAgICAgICAgIHdpZHRoOiAxNTBweDsKICAgICAgICAgICAgY29sb3I6IHdoaXRlOwogICAgICAgIH0KCiAgICAgICAgI3Jlc3BvbnNlQm9keSB7CiAgICAgICAgICAgIHdpZHRoOiA1MDBweDsKICAgICAgICB9CgogICAgICAgIHNlbGVjdCB7CiAgICAgICAgICBhcHBlYXJhbmNlOiBub25lOwogICAgICAgICAgcGFkZGluZzogNXB4OwogICAgICAgIH0KICAgICAgICBvcHRpb24gewogICAgICAgICAgICBiYWNrZ3JvdW5kLWNvbG9yOiAjNDI0NTQ5ICFpbXBvcnRhbnQ7CiAgICAgICAgfQogICAgICAgICNidXR0b25zICogewogICAgICAgICAgICBiYWNrZ3JvdW5kLWNvbG9yOiBpbmhlcml0OwogICAgICAgICAgICBjb2xvcjogaW5oZXJpdDsKICAgICAgICB9CgogICAgICAgICNhcGlEb2MsICNkYkRvYyB7CiAgICAgICAgICAgIG1hcmdpbjogNXB4OyAgICAKICAgICAgICAgICAgZGlzcGxheTogYmxvY2s7CiAgICAgICAgICAgIGJvcmRlci1yYWRpdXM6IDE1cHg7CiAgICAgICAgICAgIGJhY2tncm91bmQtY29sb3I6ICM0MjQ1NDkgIWltcG9ydGFudDsKICAgICAgICAgICAgcGFkZGluZzogMTBweDsKICAgICAgICB9CgogICAgICAgICNhcGlEb2MgKiB7CiAgICAgICAgICB3aGl0ZS1zcGFjZSA6IHByZS13cmFwICFpbXBvcnRhbnQ7CiAgICAgICAgfQogICAgICAgIAogICAgICAgIHRhYmxlIHsKICAgICAgICAgICAgYm9yZGVyLWNvbGxhcHNlOiBjb2xsYXBzZTsKICAgICAgICAgICAgbWFyZ2luOiAyNXB4IDA7CiAgICAgICAgICAgIGZvbnQtc2l6ZTogMC45ZW07CiAgICAgICAgICAgIGZvbnQtZmFtaWx5OiBzYW5zLXNlcmlmOwogICAgICAgICAgICBtaW4td2lkdGg6IDQwMHB4OwogICAgICAgICAgICBib3gtc2hhZG93OiAwIDAgMjBweCByZ2JhKDAsIDAsIDAsIDAuMTUpOwogICAgICAgIH0KICAgICAgICB0YWJsZSB0aGVhZCB0ciB7CiAgICAgICAgICAgIGJhY2tncm91bmQtY29sb3I6ICM3Mjg5REE7CiAgICAgICAgICAgIGNvbG9yOiAjZmZmZmZmOwogICAgICAgICAgICB0ZXh0LWFsaWduOiBsZWZ0OwogICAgICAgIH0KICAgICAgICB0YWJsZSB0aCwKICAgICAgICB0YWJsZSB0ZCB7CiAgICAgICAgICAgIHBhZGRpbmc6IDEycHggMTVweDsKICAgICAgICB9CiAgICAgICAgdGFibGUgPiB0Ym9keTpmaXJzdC1jaGlsZCA+IHRyOmZpcnN0LWNoaWxkID4gKiB7CiAgICAgICAgICAgIGJhY2tncm91bmQtY29sb3I6ICM3Mjg5REEgIWltcG9ydGFudDsKICAgICAgICB9CiAgICAgICAgdGFibGUgdGJvZHkgdHIgewogICAgICAgICAgICBib3JkZXItYm90dG9tOiAxcHggc29saWQgI2RkZGRkZDsKICAgICAgICB9CgogICAgICAgIHRhYmxlIHRib2R5IHRyOm50aC1vZi10eXBlKGV2ZW4pIHsKICAgICAgICAgICAgYmFja2dyb3VuZC1jb2xvcjogIzI4MmIzMCAgIDsKICAgICAgICB9CgogICAgICAgIHRhYmxlIHRib2R5IHRyOmxhc3Qtb2YtdHlwZSB7CiAgICAgICAgICAgIGJvcmRlci1ib3R0b206IDJweCBzb2xpZCAjNzI4OURBOwogICAgICAgIH0KICAgICAgICB0YWJsZSB0Ym9keSB0ci5hY3RpdmUtcm93IHsKICAgICAgICAgICAgZm9udC13ZWlnaHQ6IGJvbGQ7CiAgICAgICAgICAgIGNvbG9yOiAjNzI4OURBOwogICAgICAgIH0KICAgICAgICAjYXBpRG9jIHRhYmxlLCAjYXBpRG9jIHRhYmxlIHRkIHsgLyogZm9ybWF0dGluZyBpcyBmdWNrZWQgdXAgZm9yIHNvbWUgcmVhc29uIC1LeWxlICovCiAgICAgICAgICAgIGJvcmRlcjogMXB4IHNvbGlkICNBQUE7CiAgICAgICAgfQogICAgPC9zdHlsZT4KICA8L2hlYWQ+CiAgPGJvZHk+CiAgICA8cD5UaGlzIHBhZ2UgaXMgYSB0b29sIGZvciBkZWJ1Z2dpbmcgb3VyIHByb2plY3QncyBBUEkuIENoZWNrIGNvbnNvbGUgb3V0cHV0IGluIERldlRvb2xzIHdpbmRvd3MgKGN0cmwrc2hpZnQraSkuPC9wPgogICAgPHRleHRhcmVhIGlkPSJlbmRwb2ludCIgc3BlbGxjaGVjaz0iZmFsc2UiIHBsYWNlaG9sZGVyPSJFbmRwb2ludCI+L2FwaS9kYXRhYmFzZS9nZXQ8L3RleHRhcmVhPgogICAgPGRpdj4KICAgICAgICA8ZGl2PgogICAgICAgICAgICA8dGV4dGFyZWEgaWQ9InJlcXVlc3RCb2R5IiBzcGVsbGNoZWNrPSJmYWxzZSIgcGxhY2Vob2xkZXI9IlJlcXVlc3QgQm9keSAoSlNPTiBmb3JtYXR0ZWQpIj57ICJxdWVyeSI6ICJTRUxFQ1Qgcm9sZUlkIEZST00gcm9sZVR5cGVzIiwgInR5cGUiOiAiaGV4IiB9PC90ZXh0YXJlYT4KICAgICAgICAgICAgPGRpdiBpZD0iYnV0dG9ucyI+CiAgICAgICAgICAgICAgICA8c2VsZWN0IG5hbWU9Im1ldGhvZHMiIGlkPSJtZXRob2RzIj4KICAgICAgICAgICAgICAgICAgICA8b3B0aW9uIHZhbHVlPSJHRVQiPkdFVDwvb3B0aW9uPgogICAgICAgICAgICAgICAgICAgIDxvcHRpb24gdmFsdWU9IlBPU1QiIHNlbGVjdGVkPlBPU1Q8L29wdGlvbj4KICAgICAgICAgICAgICAgICAgICA8b3B0aW9uIHZhbHVlPSJPUFRJT05TIj5PUFRJT05TPC9vcHRpb24+CiAgICAgICAgICAgICAgICAgICAgPG9wdGlvbiB2YWx1ZT0iUFVUIj5QVVQ8L29wdGlvbj4KICAgICAgICAgICAgICAgIDwvc2VsZWN0PgogICAgICAgICAgICAgICAgPGJ1dHRvbiBvbmNsaWNrPSJtYWluKCkiPlNlbmQgUmVxdWVzdDwvYnV0dG9uPgogICAgICAgICAgICA8L2Rpdj4KICAgICAgICA8L2Rpdj4KICAgICAgICA8ZGl2PgogICAgICAgICAgICA8dGV4dGFyZWEgcmVhZG9ubHkgc3BlbGxjaGVjaz0iZmFsc2UiIHBsYWNlaG9sZGVyPSJSZXNwb25zZSBEYXRhIiBpZD0icmVzcG9uc2VCb2R5Ij48L3RleHRhcmVhPgogICAgICAgICAgICA8ZGl2PgogICAgICAgICAgICAgICAgPHRleHRhcmVhIHJlYWRvbmx5IHNwZWxsY2hlY2s9ImZhbHNlIiBwbGFjZWhvbGRlcj0iUmVwb25zZSBDb2RlIiBpZD0icmVzcG9uc2VTdGF0dXMiPjwvdGV4dGFyZWE+CiAgICAgICAgICAgIDwvZGl2PgogICAgICAgIDwvZGl2PgogICAgICAgIDxzcGFuIGlkPSJhcGlEb2MiPjwvc3Bhbj4KICAgICAgICA8c3BhbiBpZD0iZGJEb2MiPgogICAgICAgICAgICA8aDE+RGF0YWJhc2UgUmVmZXJlbmNlPC9oMT4KICAgICAgICAgICAgPHA+VGhpcyBpcyBhIHJvdWdoIGRyYWZ0IHRoYXQgd2FzIHByb3ZpZGVkIGJ5IEVyZHMsIGFuZCBpcyBub3QgdXBkYXRlZC48L3A+CiAgICAgICAgICAgIDxoMj48Y29kZT5yb2xlVHlwZXM8L2NvZGU+PC9oMj4KICAgICAgICAgICAgPHRhYmxlIGJvcmRlcj0xPgogICAgICAgICAgICA8dHI+CiAgICAgICAgICAgIDx0ZCBiZ2NvbG9yPXNpbHZlciBjbGFzcz0nbWVkaXVtJz5yb2xlSUQgfCBieXRlWzMyXSA8YnI+ICBCWVRFUygzMikgLSBTSEEyNTYocm9sZSk8L3RkPgogICAgICAgICAgICA8dGQgYmdjb2xvcj1zaWx2ZXIgY2xhc3M9J21lZGl1bSc+cm9sZSB8IFN0cmluZyA8YnI+IFZBUkNIQVIoMTApPC90ZD4KICAgICAgICAgICAgPC90cj4KCiAgICAgICAgICAgIDx0cj4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPlN0YWZmPC90ZD4KICAgICAgICAgICAgPC90cj4KCiAgICAgICAgICAgIDx0cj4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPlBhdGllbnQ8L3RkPgogICAgICAgICAgICA8L3RyPgoKICAgICAgICAgICAgPHRyPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+QWRtaW48L3RkPgogICAgICAgICAgICA8L3RyPgogICAgICAgICAgICA8L3RhYmxlPgoKICAgICAgICAgICAgPGJyPiAKICAgICAgICAgICAgPGgyPjxjb2RlPnVzZXJzPC9jb2RlPjwvaDI+CiAgICAgICAgICAgIDx0YWJsZSBib3JkZXI9MT4KICAgICAgICAgICAgPHRyPgogICAgICAgICAgICA8dGQgYmdjb2xvcj1zaWx2ZXIgY2xhc3M9J21lZGl1bSc+dXNlcklEIHwgYnl0ZVszMl0gPGJyPiBCWVRFUygzMikgLSBTSEEyNTYoZW1haWwpPC90ZD4KICAgICAgICAgICAgPHRkIGJnY29sb3I9c2lsdmVyIGNsYXNzPSdtZWRpdW0nPmVtYWlsIHwgU3RyaW5nIDxicj4gVkFSQ0hBUigxMDApPC90ZD4KICAgICAgICAgICAgPHRkIGJnY29sb3I9c2lsdmVyIGNsYXNzPSdtZWRpdW0nPmhhc2hlZFBhc3MgfCBieXRlWzMyXSA8YnI+IEJZVEVTKDMyKSAtIFNIQTI1NihwYXNzd29yZCk8L3RkPgogICAgICAgICAgICA8dGQgYmdjb2xvcj1zaWx2ZXIgY2xhc3M9J21lZGl1bSc+Zmlyc3ROYW1lIHwgU3RyaW5nIDxicj4gVkFSQ0hBUigzNSk8L3RkPgogICAgICAgICAgICA8dGQgYmdjb2xvcj1zaWx2ZXIgY2xhc3M9J21lZGl1bSc+bGFzdE5hbWUgfCBTdHJpbmcgPGJyPiBWQVJDSEFSKDM1KTwvdGQ+CiAgICAgICAgICAgIDx0ZCBiZ2NvbG9yPXNpbHZlciBjbGFzcz0nbWVkaXVtJz5zZXggfCBjaGFyIDxicj4gQ0hBUjwvdGQ+CiAgICAgICAgICAgIDx0ZCBiZ2NvbG9yPXNpbHZlciBjbGFzcz0nbWVkaXVtJz5iaXJ0aERhdGUgfCBpbnQgPGJyPiBJTlRFR0VSIC0tIFVUQ19TRUNPTkRTPC90ZD4KICAgICAgICAgICAgPHRkIGJnY29sb3I9c2lsdmVyIGNsYXNzPSdtZWRpdW0nPmFkZHJlc3MgfCBTdHJpbmcgPGJyPiBWQVJDSEFSKDEwMCk8L3RkPgogICAgICAgICAgICA8dGQgYmdjb2xvcj1zaWx2ZXIgY2xhc3M9J21lZGl1bSc+cGhvbmVOdW1iZXIgfCBTdHJpbmcgPGJyPiBWQVJDSEFSKDEwKTwvdGQ+CiAgICAgICAgICAgIDx0ZCBiZ2NvbG9yPXNpbHZlciBjbGFzcz0nbWVkaXVtJz5yb2xlSUQgfCBieXRlWzMyXSA8YnI+IEJZVEVTKDMyKSAtIFNIQTI1Nihyb2xlKTwvdGQ+CiAgICAgICAgICAgIDx0ZCBiZ2NvbG9yPXNpbHZlciBjbGFzcz0nbWVkaXVtJz5kZXRhaWwgfCBTdHJpbmcgPGJyPiBURVhUPC90ZD4KICAgICAgICAgICAgPC90cj4KCiAgICAgICAgICAgIDx0cj4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPkpheVNvaG5AZW1haWwuY29tPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPkpheTwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+U29objwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+TTwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+OTQ4NjE0NDAwPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4xMjMgQWRkcmVzcyBDdDwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+OTE2MzU5NzQzNzwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Li4uPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz48L3RkPgogICAgICAgICAgICA8L3RyPgoKICAgICAgICAgICAgPHRyPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+U3RlcGhGdUBzY2hlZHVsZWRzbWlsZXMuY29tPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPlN0ZXBoPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz5GdTwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+RjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+NTY3NjQ4MDAwPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz40NTYgQWRkcmVzcyBBdmU8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjkxNjk2NzgxMjE8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+PC90ZD4KICAgICAgICAgICAgPC90cj4KCiAgICAgICAgICAgIDx0cj4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPkFkYW1NaW5oQHNjaGVkdWxlZHNtaWxlcy5jb208L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+QWRhbTwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+TWluaDwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+TTwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+MTY5MTcxMjAwPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz43ODkgQWRkcmVzcyBXYXk8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjkxNjY1MzQxMjQ8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+PC90ZD4KICAgICAgICAgICAgPC90cj4KCiAgICAgICAgICAgIDx0cj4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPlN0ZXdhcnRGZXJyaXNAc2NoZWR1bGVkc21pbGVzLmNvbTwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Li4uPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz5TdGV3YXJ0PC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz5GZXJyaXM8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPk08L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjkwNTk5MDQwMDwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+NDU2IEFkZHJlc3MgQmx2ZDwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+OTE2MDI1ODQyOTwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Li4uPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz48L3RkPgogICAgICAgICAgICA8L3RyPgoKICAgICAgICAgICAgPHRyPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Sm9obkRvZUBlbWFpbC5jb208L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Sm9objwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+RG9lPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz5NPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz43NTkyODMyMDA8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjEyMyBBZGRyZXNzIExhbmU8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjEyMzQ1Njc4OTA8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+PC90ZD4KICAgICAgICAgICAgPC90cj4KCiAgICAgICAgICAgIDx0cj4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPkFubmllWWVhZ2VyQHNjaGVkdWxlZHNtaWxlcy5jb208L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+QW5uaWU8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPlllYWdlcjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+RjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Mzk3NTI2NDAwPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz43ODkgQWRkcmVzcyBEcjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+OTE2Nzk1NDMyOTwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Li4uPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz48L3RkPgogICAgICAgICAgICA8L3RyPgoKICAgICAgICAgICAgPHRyPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+RWxpc2VGbG9zc21vcmVAc2NoZWR1bGVkc21pbGVzLmNvbTwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Li4uPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz5FbGlzZTwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Rmxvc3Ntb3JlPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz5GPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz43NzQ3MDU2MDA8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjQ1NiBBZGRyZXNzIERyPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz45MTY1NTkyMDYzPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjwvdGQ+CiAgICAgICAgICAgIDwvdHI+CgogICAgICAgICAgICA8dHI+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Li4uPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz5KYW5lRG9lQGVtYWlsLmNvbTwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Li4uPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz5KYW5lPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz5Eb2U8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPkY8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjgyNjc2MTYwMDwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+MTIzIEFkZHJlc3MgTGFuZTwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+MzE0MTU5MjY1NDwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Li4uPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz48L3RkPgogICAgICAgICAgICA8L3RyPgogICAgICAgICAgICA8L3RhYmxlPgoKICAgICAgICAgICAgPGJyPgogICAgICAgICAgICA8aDI+PGNvZGU+c3RhZmY8L2NvZGU+PC9oMj4KICAgICAgICAgICAgPHRhYmxlIGJvcmRlcj0xPgogICAgICAgICAgICA8dHI+CiAgICAgICAgICAgICAgICA8dGQgYmdjb2xvcj1zaWx2ZXIgY2xhc3M9J21lZGl1bSc+c3RhZmZJRCB8IGJ5dGVbMzJdIDxicj4gQllURVMoMzIpIC0gU0hBMjU2KGVtYWlsKTwvdGQ+CiAgICAgICAgICAgICAgICA8dGQgYmdjb2xvcj1zaWx2ZXIgY2xhc3M9J21lZGl1bSc+aHJseVdhZ2UgfCBkb3VibGUgPGJyPiBERUNJTUFMKDEwLCAyKSA8L3RkPgogICAgICAgICAgICA8L3RyPgoKICAgICAgICAgICAgPHRyPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+ODguNzU8L3RkPgogICAgICAgICAgICA8L3RyPgoKICAgICAgICAgICAgPHRyPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+MzQuNTA8L3RkPgogICAgICAgICAgICA8L3RyPgoKICAgICAgICAgICAgPHRyPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+NTUuNzU8L3RkPgogICAgICAgICAgICA8L3RyPgogICAgICAgICAgICA8L3RhYmxlPgoKICAgICAgICAgICAgPGJyPgogICAgICAgICAgICA8aDI+PGNvZGU+YXBwb2ludG1lbnRzPC9jb2RlPjwvaDI+CiAgICAgICAgICAgIDx0YWJsZSBib3JkZXI9MT4KICAgICAgICAgICAgPHRyPgogICAgICAgICAgICA8dGQgYmdjb2xvcj1zaWx2ZXIgY2xhc3M9J21lZGl1bSc+YXBwb2ludG1lbnRJRCB8IGJ5dGVbMzJdIDxicj4gQllURVMoMzIpIC0gU0hBMjU2KCdBcHBvaW50bWVudCcgKyB1bmlxdWVJRCkpPC90ZD4KICAgICAgICAgICAgPHRkIGJnY29sb3I9c2lsdmVyIGNsYXNzPSdtZWRpdW0nPnBhdGllbnRJRCB8IGJ5dGVbMzJdIDxicj4gQllURVMoMzIpIC0gU0hBMjU2KGVtYWlsKTwvdGQ+CiAgICAgICAgICAgIDx0ZCBiZ2NvbG9yPXNpbHZlciBjbGFzcz0nbWVkaXVtJz5zdGFydFRpbWUgfCBpbnQgPGJyPiBJTlRFR0VSPC90ZD4KICAgICAgICAgICAgPHRkIGJnY29sb3I9c2lsdmVyIGNsYXNzPSdtZWRpdW0nPnN0YWZmMUlEIHwgYnl0ZVszMl0gPGJyPiBCWVRFUygzMikgLSBTSEEyNTYoZW1haWwpPC90ZD4KICAgICAgICAgICAgPHRkIGJnY29sb3I9c2lsdmVyIGNsYXNzPSdtZWRpdW0nPnN0YWZmMklEIHwgYnl0ZVszMl0gPGJyPiBCWVRFUygzMikgLSBTSEEyNTYoZW1haWwpPC90ZD4KICAgICAgICAgICAgPHRkIGJnY29sb3I9c2lsdmVyIGNsYXNzPSdtZWRpdW0nPnN0YWZmM0lEIHwgYnl0ZVszMl0gPGJyPiBCWVRFUygzMikgLSBTSEEyNTYoZW1haWwpPC90ZD4KICAgICAgICAgICAgPHRkIGJnY29sb3I9c2lsdmVyIGNsYXNzPSdtZWRpdW0nPmlzQ2FuY2VsZWQgfCBib29sZWFuIDxicj4gVElOWUlOVCgxKTwvdGQ+CiAgICAgICAgICAgIDwvdHI+CgogICAgICAgICAgICA8dHI+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Li4uPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjE3MzEwOTI0MDA8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+TlVMTDwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+TlVMTDwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+MTwvdGQ+CiAgICAgICAgICAgIDwvdHI+CgogICAgICAgICAgICA8dHI+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Li4uPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjE3MzEwODcwMDA8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+TlVMTDwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+TlVMTDwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+MDwvdGQ+CiAgICAgICAgICAgIDwvdHI+CgogICAgICAgICAgICA8dHI+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Li4uPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjE3MzEwOTA2MDA8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+TlVMTDwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+TlVMTDwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+MDwvdGQ+CiAgICAgICAgICAgIDwvdHI+CgogICAgICAgICAgICA8dHI+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Li4uPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjE3MzIyOTQ4MDA8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Li4uPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjA8L3RkPgogICAgICAgICAgICA8L3RyPgoKICAgICAgICAgICAgPHRyPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Li4uPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4xNzMxNjkwMDAwPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+TlVMTDwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+MDwvdGQ+CiAgICAgICAgICAgIDwvdHI+CgogICAgICAgICAgICA8dHI+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Li4uPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjE3MzE2OTM2MDA8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+TlVMTDwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+TlVMTDwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+MDwvdGQ+CiAgICAgICAgICAgIDwvdHI+CiAgICAgICAgICAgIDwvdGFibGU+CgogICAgICAgICAgICA8YnI+CiAgICAgICAgICAgIDxoMj48Y29kZT5hcHBvaW50bWVudFR5cGVLZXk8L2NvZGU+PC9oMj4KICAgICAgICAgICAgPHRhYmxlIGJvcmRlcj0xPgogICAgICAgICAgICA8dHI+CiAgICAgICAgICAgIDx0ZCBiZ2NvbG9yPXNpbHZlciBjbGFzcz0nbWVkaXVtJz50eXBlSUQgfCBieXRlWzMyXSA8YnI+IEJZVEVTKDMyKSAtIFNIQTI1NihhcHBvaW50bWVuVHlwZSkpPC90ZD4KICAgICAgICAgICAgPHRkIGJnY29sb3I9c2lsdmVyIGNsYXNzPSdtZWRpdW0nPmFwcG9pbnRtZW50VHlwZSB8IFN0cmluZyA8YnI+IFZBUkNIQVIoNTApPC90ZD4KICAgICAgICAgICAgPHRkIGJnY29sb3I9c2lsdmVyIGNsYXNzPSdtZWRpdW0nPmNvc3QgfCBkb3VibGUgPGJyPiBERUNJTUFMKDEwLCAyKTwvdGQ+CiAgICAgICAgICAgIDwvdHI+CgogICAgICAgICAgICA8dHI+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Li4uPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz5DbGVhbmluZzwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+MjUwLjAwPC90ZD4KICAgICAgICAgICAgPC90cj4KCiAgICAgICAgICAgIDx0cj4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPkNoZWNrdXA8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjIwMC4wMDwvdGQ+CiAgICAgICAgICAgIDwvdHI+CgogICAgICAgICAgICA8dHI+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Li4uPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz5FbWVyZ2VuY3k8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjc1MC4wMDwvdGQ+CiAgICAgICAgICAgIDwvdHI+CgogICAgICAgICAgICA8dHI+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Li4uPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz5YLVJheTwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+MjAwLjAwPC90ZD4KICAgICAgICAgICAgPC90cj4KCiAgICAgICAgICAgIDx0cj4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPkZpbGxpbmc8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjMwMC4wMDwvdGQ+CiAgICAgICAgICAgIDwvdHI+CiAgICAgICAgICAgIDwvdGFibGU+CiAgICAgICAgPC9zcGFuPgogICAgPC9kaXY+CiAgPC9ib2R5Pgo8L2h0bWw+"
                ), CHARSET);
                sendResponse(STATUS_CODES.get("OK"), HEADER_TYPES.get("HTML"), testingHtmlB64.length(), new ByteArrayInputStream(testingHtmlB64.getBytes(CHARSET)));
            } else if (requestPath.length == 0) {
                // idk, redirect to home page i guess
                handleRequest();
            } else if(requestPath[1].equals("api")) {
                System.out.println("API endpoint called");
                try {
                    handleApiRequest();
                } catch (Exception e) {
                    System.out.println("Something went horribly wrong!");
                    e.printStackTrace();
                }
            } else {
                handleRequest();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            con.close();
        }
    }
}