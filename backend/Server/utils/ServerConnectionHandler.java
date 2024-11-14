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
import java.util.List;
import java.util.ArrayList;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.Headers;
// Need to import external library for reading JSON strings and files. -Kyle
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

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
            System.out.println("ServerHandler Error: Too many bytes to write to stream. Has responseBodyLength been defined correctly?");
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
    
    private static void handleDatabaseRequest() throws IOException {
        boolean isValidRequest;
        final JsonObject requestJsonObject = requestBodyJson.getAsJsonObject();
        String results = "";
        switch (requestPath[3]) {
            case "get":
                isValidRequest = membersMatch(requestJsonObject.keySet(), "query");
                if (isValidRequest) {
                    final String queryString = requestJsonObject.get("query").getAsString();
                    // Databse connection
                    try (DatabaseConnection db = new DatabaseConnection()) {
                        if (db.isConnected()) {
                            results = convertFromJson(db.query(queryString));
                        }
                    } catch (Exception e) {
                        // Errors handled in DatabaseConnection, pass
                    }
                    sendResponse(STATUS_CODES.get("OK"), convertToJsonElement(results));
                }
            break;
            case "send":
                isValidRequest = membersMatch(requestJsonObject.keySet(), "query");
                if (isValidRequest) {
                    final String queryString = requestJsonObject.get("query").getAsString();
                    // Databse connection
                    try (DatabaseConnection db = new DatabaseConnection()) {
                        if (db.isConnected()) {
                            results = convertFromJson(db.update(queryString));
                        }
                    } catch (Exception e) {
                        // Errors handled in DatabaseConnection, pass
                    }
                    sendResponse(STATUS_CODES.get("OK"), convertToJsonElement(results));
                }
            break;
            default:
                isValidRequest = false;
        }
        // Testing
        if (isValidRequest) {
            System.out.println("Operation successful!");
        }
    }
    
    
    private static void registerUser() throws IOException {
        final boolean isValidRequest = membersMatch(getKeys(requestBodyJson),
            "userId", "roleId", "firstName", "lastName", "address", "sex", "phone", "email", "birthDate", "password"
        );
        if (isValidRequest) {
            // Testing
            sendResponse(STATUS_CODES.get("OK"), "Valid request body");
        } else {
            // Malformed/Invalid request body
            sendResponse(STATUS_CODES.get("BAD_REQUEST"), "Malformed request body");
        }
    }
    
    private static void getUser() throws IOException {
        final boolean isValidRequest = membersMatch(getKeys(requestBodyJson),
            "email", "password"
        );
        if (isValidRequest) {
            // Testing
            sendResponse(STATUS_CODES.get("OK"), "Valid request body");
        } else {
            // Malformed/Invalid request body
            sendResponse(STATUS_CODES.get("BAD_REQUEST"), "Malformed request body");
        }
    }
    
    // THIS IS NOT SPECFICALLY AN FR, LEAVE UNIMPLEMENTED OR IMPLEMENT LAST -Kyle
    private static void uploadFile() {
        
    }
    
    private static void handleApiRequest() throws IOException {        
        boolean isValidRequest = (requestBodyJson != null && requestPath.length >= 3); // Used to determine if an error reponse needs to be sent after checking switch cases
        if (isValidRequest) {
            switch (requestMethod) {
                case "GET":
                    switch (requestPath[2]) {
                        case "verify":
                            
                        break;
                        case "login":
                            
                        break;
                        default:
                            isValidRequest = false;
                    }
                break;
                case "POST":
                    switch (requestPath[2]) {
                        case "database":
                            handleDatabaseRequest();
                        break;
                        case "register":
                            registerUser();
                        break;
                        default:
                            isValidRequest = false;
                    }
                break;
                // This is where we'd have a PUT case for uploading images... if we were going to implement that. -Kyle
                default:
                    isValidRequest = false;
            }
        }
        if (!isValidRequest) {
            System.out.println("API request was found to be invalid");
            // Malformed/Invalid request body
            sendResponse(STATUS_CODES.get("BAD_REQUEST"), "Malformed or invalid request");
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
                "PCFET0NUWVBFIGh0bWw+CjxodG1sPgogIDxoZWFkPgogICAgPHRpdGxlPlRoaXMgaXMgdGhlIHRpdGxlIG9mIHRoZSB3ZWJwYWdlITwvdGl0bGU+CiAgICA8IS0tIFJlbmRlcmluZyBtYXJrZG93biAtLT4KICAgIDxzY3JpcHQgc3JjPSJodHRwczovL2Nkbi5qc2RlbGl2ci5uZXQvbnBtL21hcmtkb3duLWl0QDE0LjEuMC9kaXN0L21hcmtkb3duLWl0Lm1pbi5qcyI+PC9zY3JpcHQ+CiAgICA8c2NyaXB0PgogICAgICAgIGZ1bmN0aW9uIG1haW4oKSB7CiAgICAgICAgICAgIHZhciBbZW5kcG9pbnQsIG1ldGhvZCwgYm9keV0gPSBnZXREYXRhKCkKICAgICAgICAgICAgc2VuZFJlcXVlc3QoZW5kcG9pbnQsIG1ldGhvZCwgYm9keSkKICAgICAgICAgICAgCiAgICAgICAgfQoKICAgICAgICBmdW5jdGlvbiBnZXREYXRhKCkgewogICAgICAgICAgICBjb25zdCByZXF1ZXN0Qm9keUFyZWEgPSBkb2N1bWVudC5nZXRFbGVtZW50QnlJZCgicmVxdWVzdEJvZHkiKQogICAgICAgICAgICBjb25zdCBlbmRwb2ludEFyZWEgPSBkb2N1bWVudC5nZXRFbGVtZW50QnlJZCgiZW5kcG9pbnQiKQogICAgICAgICAgICBjb25zdCBtZXRob2RzU2VsZWN0ID0gZG9jdW1lbnQuZ2V0RWxlbWVudEJ5SWQoIm1ldGhvZHMiKQogICAgICAgICAgICB2YXIgcmVxdWVzdEJvZHkgPSByZXF1ZXN0Qm9keUFyZWEudmFsdWUKICAgICAgICAgICAgdmFyIG1ldGhvZCA9IG1ldGhvZHNTZWxlY3QudmFsdWUKICAgICAgICAgICAgdmFyIGVuZHBvaW50ID0gZW5kcG9pbnRBcmVhLnZhbHVlCiAgICAgICAgICAgIHJldHVybiBbZW5kcG9pbnQsIG1ldGhvZCwgcmVxdWVzdEJvZHldCiAgICAgICAgfQoKICAgICAgICBmdW5jdGlvbiBzZW5kUmVxdWVzdChlbmRwb2ludCwgbWV0aG9kLCByZXF1ZXN0Qm9keSkgewogICAgICAgICAgICBjb25zb2xlLmxvZygiU2VuZGluZyAiICsgbWV0aG9kICsgIiB0byAiICsgZW5kcG9pbnQgKyAiOlxuIiArIHJlcXVlc3RCb2R5KTsKICAgICAgICAgICAgZmV0Y2god2luZG93LmxvY2F0aW9uLm9yaWdpbiArIGVuZHBvaW50LCB7CiAgICAgICAgICAgICAgICBtZXRob2Q6IG1ldGhvZCwKICAgICAgICAgICAgICAgIGJvZHk6IEpTT04uc3RyaW5naWZ5KEpTT04ucGFyc2UocmVxdWVzdEJvZHkpKSwKICAgICAgICAgICAgICAgIGhlYWRlcnM6IHsKICAgICAgICAgICAgICAgICAgICAnQWNjZXB0JzogJ2FwcGxpY2F0aW9uL2pzb24nLAogICAgICAgICAgICAgICAgICAgICJDb250ZW50LXR5cGUiOiAiYXBwbGljYXRpb24vanNvbjsgY2hhcnNldD1VVEYtOCIKICAgICAgICAgICAgICAgIH0KICAgICAgICAgICAgfSkudGhlbihyZXNwb25zZSA9PiB7CiAgICAgICAgICAgICAgICByZXR1cm4gUHJvbWlzZS5hbGwoW3Jlc3BvbnNlLnN0YXR1cywgcmVzcG9uc2UudGV4dCgpXSkKICAgICAgICAgICAgfSwgbmV0d29ya0Vycm9yID0+IHsKICAgICAgICAgICAgICBjb25zb2xlLmxvZyhuZXR3b3JrRXJyb3IubWVzc2FnZSkKICAgICAgICAgICAgICBsb2FkUmVzcG9uc2UoIiIsIG5ldHdvcmtFcnJvci5tZXNzYWdlKQogICAgICAgICAgICB9KS50aGVuKCh2YWx1ZXMpID0+IHsKICAgICAgICAgICAgICB2YXIgcmVzcG9uc2VUZXh0ID0gICh2YWx1ZXNbMF0gPj0gNDAwKSA/IHZhbHVlc1sxXSA6IEpTT04uc3RyaW5naWZ5KEpTT04ucGFyc2UodmFsdWVzWzFdKSwgbnVsbCwgMikKICAgICAgICAgICAgICBjb25zb2xlLmxvZyh2YWx1ZXMpCiAgICAgICAgICAgICAgbG9hZFJlc3BvbnNlKHZhbHVlc1swXSwgcmVzcG9uc2VUZXh0KQogICAgICAgICAgICB9KQogICAgICAgIH0KCiAgICAgICAgLy8gRGV2IG9ubHkuIHJlbW92ZSBiZWZvcmUgZGVwbG95bWVudC4gUmVxdWlyZXMgaW1wb3J0IG9mIHRoZSBtYXJrZWQgSlMgbGlicmFyeSAtS3lsZQogICAgICAgIGZ1bmN0aW9uIHJlbmRlckFwaURvYygpIHsKICAgICAgICAgICAgY29uc3QgbWQgPSBtYXJrZG93bml0KHsKICAgICAgICAgICAgICBodG1sOiB0cnVlLAogICAgICAgICAgICAgIGxpbmtpZnk6IHRydWUsCiAgICAgICAgICAgICAgdHlwb2dyYXBoZXI6IHRydWUKICAgICAgICAgICAgfSkKICAgICAgICAgIGZldGNoKCJodHRwczovL3Jhdy5naXRodWJ1c2VyY29udGVudC5jb20vVy1TYW1teS9TY2hlZHVsZWQtU21pbGVzL3JlZnMvaGVhZHMvYmFja2VuZC9kb2NzL0FQSV9SRUZFUkVOQ0UubWQiKSAgICAgIC8vIFRoZSBwYXRoIHRvIHRoZSByYXcgTWFya2Rvd24gZmlsZQogICAgICAgICAgICAudGhlbihyZXNwb25zZSA9PiByZXNwb25zZS50ZXh0KCkpCiAgICAgICAgICAgIC50aGVuKG1hcmtkb3duID0+IHsKICAgICAgICAgICAgICBkb2N1bWVudC5nZXRFbGVtZW50QnlJZCgiYXBpRG9jIikuaW5uZXJIVE1MID0gbWQucmVuZGVyKG1hcmtkb3duKS8vbWFya2VkLnBhcnNlKG1hcmtkb3duKQogICAgICAgICAgICB9KQogICAgICAgIH0KCiAgICAgICAgd2luZG93Lm9ubG9hZCA9ICgpID0+IHsKICAgICAgICAgICAgcmVuZGVyQXBpRG9jKCkKICAgICAgICB9CiAgICAgICAgICAgIAogICAgICAgIGZ1bmN0aW9uIGxvYWRSZXNwb25zZShyZXNwb25zZVN0YXR1cywgcmVzcG9uc2VUZXh0KSB7CiAgICAgICAgICAgIGNvbnN0IHJlc3BvbnNlQm9keUFyZWEgPSBkb2N1bWVudC5nZXRFbGVtZW50QnlJZCgicmVzcG9uc2VCb2R5IikKICAgICAgICAgICAgY29uc3QgcmVzcG9uc2VTdGF0dXNBcmVhID0gZG9jdW1lbnQuZ2V0RWxlbWVudEJ5SWQoInJlc3BvbnNlU3RhdHVzIikKICAgICAgICAgICAgcmVzcG9uc2VCb2R5QXJlYS52YWx1ZSA9IHJlc3BvbnNlVGV4dAogICAgICAgICAgICByZXNwb25zZVN0YXR1c0FyZWEuaW5uZXJIVE1MID0gcmVzcG9uc2VTdGF0dXMKICAgICAgICB9CiAgICA8L3NjcmlwdD4KICAgIDxzdHlsZT4KICAgICAgICBodG1sLCBib2R5IHsKICAgICAgICAgICAgYmFja2dyb3VuZC1jb2xvcjogIzIzMjcyQTsgLyogTVkgRVlFUyEgKi8KICAgICAgICAgICAgY29sb3I6IHdoaXRlOwogICAgICAgICAgICBmb250LWZhbWlseTogQXJpYWwsIEhlbHZldGljYSwgc2Fucy1zZXJpZjsgLyogbGVzcyBoYXJzaCB0byBsb29rIGF0IGR1cmluZyBkZXYsIHN1YmplY3QgdG8gY2hhbmdlIGluIGFjdHVhbCBhcHAgKi8KICAgICAgICB9CgogICAgICAgIGJvZHkgPiBkaXYgPiAqIHsKICAgICAgICAgICAgZmxvYXQ6IGxlZnQ7CiAgICAgICAgICAgIHBhZGRpbmctcmlnaHQ6IDM1cHg7CiAgICAgICAgfQoKICAgICAgICAjcmVzcG9uc2VTdGF0dXMgewogICAgICAgICAgICBiYWNrZ3JvdW5kLWNvbG9yOiAjNDI0NTQ5OwogICAgICAgICAgICBoZWlnaHQ6IDEuNWVtOwogICAgICAgICAgICB3aWR0aDogMTBlbTsKICAgICAgICAgICAgY29sb3I6IHdoaXRlOwogICAgICAgIH0KCiAgICAgICAgI2VuZHBvaW50IHsKICAgICAgICAgICAgYmFja2dyb3VuZC1jb2xvcjogIzQyNDU0OTsKICAgICAgICAgICAgaGVpZ2h0OiAxLjVlbTsKICAgICAgICAgICAgd2lkdGg6IDQ1ZW07CiAgICAgICAgICAgIGNvbG9yOiB3aGl0ZTsKICAgICAgICB9CgogICAgICAgICNyZXF1ZXN0Qm9keSwgI3Jlc3BvbnNlQm9keSB7CiAgICAgICAgICAgIGJvcmRlci1yYWRpdXM6IDE1cHg7CiAgICAgICAgICAgIGJhY2tncm91bmQtY29sb3I6ICM0MjQ1NDk7CiAgICAgICAgICAgIHBhZGRpbmc6IDEwcHg7CiAgICAgICAgICAgIGhlaWdodDogMjAwcHg7CiAgICAgICAgICAgIHdpZHRoOiAxNTBweDsKICAgICAgICAgICAgY29sb3I6IHdoaXRlOwogICAgICAgIH0KCiAgICAgICAgI3Jlc3BvbnNlQm9keSB7CiAgICAgICAgICAgIHdpZHRoOiA1MDBweDsKICAgICAgICB9CgogICAgICAgIHNlbGVjdCB7CiAgICAgICAgICBhcHBlYXJhbmNlOiBub25lOwogICAgICAgICAgcGFkZGluZzogNXB4OwogICAgICAgIH0KICAgICAgICBvcHRpb24gewogICAgICAgICAgICBiYWNrZ3JvdW5kLWNvbG9yOiAjNDI0NTQ5ICFpbXBvcnRhbnQ7CiAgICAgICAgfQogICAgICAgICNidXR0b25zICogewogICAgICAgICAgICBiYWNrZ3JvdW5kLWNvbG9yOiBpbmhlcml0OwogICAgICAgICAgICBjb2xvcjogaW5oZXJpdDsKICAgICAgICB9CgogICAgICAgICNhcGlEb2MsICNkYkRvYyB7CiAgICAgICAgICAgIG1hcmdpbjogNXB4OyAgICAKICAgICAgICAgICAgZGlzcGxheTogYmxvY2s7CiAgICAgICAgICAgIGJvcmRlci1yYWRpdXM6IDE1cHg7CiAgICAgICAgICAgIGJhY2tncm91bmQtY29sb3I6ICM0MjQ1NDkgIWltcG9ydGFudDsKICAgICAgICAgICAgcGFkZGluZzogMTBweDsKICAgICAgICB9CgogICAgICAgICNhcGlEb2MgKiB7CiAgICAgICAgICB3aGl0ZS1zcGFjZSA6IHByZS13cmFwICFpbXBvcnRhbnQ7CiAgICAgICAgfQogICAgICAgIAogICAgICAgIHRhYmxlIHsKICAgICAgICAgICAgYm9yZGVyLWNvbGxhcHNlOiBjb2xsYXBzZTsKICAgICAgICAgICAgbWFyZ2luOiAyNXB4IDA7CiAgICAgICAgICAgIGZvbnQtc2l6ZTogMC45ZW07CiAgICAgICAgICAgIGZvbnQtZmFtaWx5OiBzYW5zLXNlcmlmOwogICAgICAgICAgICBtaW4td2lkdGg6IDQwMHB4OwogICAgICAgICAgICBib3gtc2hhZG93OiAwIDAgMjBweCByZ2JhKDAsIDAsIDAsIDAuMTUpOwogICAgICAgIH0KICAgICAgICB0YWJsZSB0aGVhZCB0ciB7CiAgICAgICAgICAgIGJhY2tncm91bmQtY29sb3I6ICM3Mjg5REE7CiAgICAgICAgICAgIGNvbG9yOiAjZmZmZmZmOwogICAgICAgICAgICB0ZXh0LWFsaWduOiBsZWZ0OwogICAgICAgIH0KICAgICAgICB0YWJsZSB0aCwKICAgICAgICB0YWJsZSB0ZCB7CiAgICAgICAgICAgIHBhZGRpbmc6IDEycHggMTVweDsKICAgICAgICB9CiAgICAgICAgdGFibGUgPiB0Ym9keTpmaXJzdC1jaGlsZCA+IHRyOmZpcnN0LWNoaWxkID4gKiB7CiAgICAgICAgICAgIGJhY2tncm91bmQtY29sb3I6ICM3Mjg5REEgIWltcG9ydGFudDsKICAgICAgICB9CiAgICAgICAgdGFibGUgdGJvZHkgdHIgewogICAgICAgICAgICBib3JkZXItYm90dG9tOiAxcHggc29saWQgI2RkZGRkZDsKICAgICAgICB9CgogICAgICAgIHRhYmxlIHRib2R5IHRyOm50aC1vZi10eXBlKGV2ZW4pIHsKICAgICAgICAgICAgYmFja2dyb3VuZC1jb2xvcjogIzI4MmIzMCAgIDsKICAgICAgICB9CgogICAgICAgIHRhYmxlIHRib2R5IHRyOmxhc3Qtb2YtdHlwZSB7CiAgICAgICAgICAgIGJvcmRlci1ib3R0b206IDJweCBzb2xpZCAjNzI4OURBOwogICAgICAgIH0KICAgICAgICB0YWJsZSB0Ym9keSB0ci5hY3RpdmUtcm93IHsKICAgICAgICAgICAgZm9udC13ZWlnaHQ6IGJvbGQ7CiAgICAgICAgICAgIGNvbG9yOiAjNzI4OURBOwogICAgICAgIH0KICAgICAgICAjYXBpRG9jIHRhYmxlLCAjYXBpRG9jIHRhYmxlIHRkIHsgLyogZm9ybWF0dGluZyBpcyBmdWNrZWQgdXAgZm9yIHNvbWUgcmVhc29uIC1LeWxlICovCiAgICAgICAgICAgIGJvcmRlcjogMXB4IHNvbGlkICNBQUE7CiAgICAgICAgfQogICAgPC9zdHlsZT4KICA8L2hlYWQ+CiAgPGJvZHk+CiAgICA8cD5UaGlzIHBhZ2UgaXMgYSB0b29sIGZvciBkZWJ1Z2dpbmcgb3VyIHByb2plY3QncyBBUEkuIENoZWNrIGNvbnNvbGUgb3V0cHV0IGluIERldlRvb2xzIHdpbmRvd3MgKGN0cmwrc2hpZnQraSkuPC9wPgogICAgPHRleHRhcmVhIGlkPSJlbmRwb2ludCIgc3BlbGxjaGVjaz0iZmFsc2UiIHBsYWNlaG9sZGVyPSJFbmRwb2ludCI+L2FwaS9kYXRhYmFzZS9nZXQ8L3RleHRhcmVhPgogICAgPGRpdj4KICAgICAgICA8ZGl2PgogICAgICAgICAgICA8dGV4dGFyZWEgaWQ9InJlcXVlc3RCb2R5IiBzcGVsbGNoZWNrPSJmYWxzZSIgcGxhY2Vob2xkZXI9IlJlcXVlc3QgQm9keSAoSlNPTiBmb3JtYXR0ZWQpIj57ICJxdWVyeSI6ICJTRUxFQ1Qgcm9sZUlkIEZST00gcm9sZVR5cGVzIiwgInR5cGUiOiAiaGV4IiB9PC90ZXh0YXJlYT4KICAgICAgICAgICAgPGRpdiBpZD0iYnV0dG9ucyI+CiAgICAgICAgICAgICAgICA8c2VsZWN0IG5hbWU9Im1ldGhvZHMiIGlkPSJtZXRob2RzIj4KICAgICAgICAgICAgICAgICAgICA8b3B0aW9uIHZhbHVlPSJHRVQiPkdFVDwvb3B0aW9uPgogICAgICAgICAgICAgICAgICAgIDxvcHRpb24gdmFsdWU9IlBPU1QiIHNlbGVjdGVkPlBPU1Q8L29wdGlvbj4KICAgICAgICAgICAgICAgICAgICA8b3B0aW9uIHZhbHVlPSJPUFRJT05TIj5PUFRJT05TPC9vcHRpb24+CiAgICAgICAgICAgICAgICAgICAgPG9wdGlvbiB2YWx1ZT0iUFVUIj5QVVQ8L29wdGlvbj4KICAgICAgICAgICAgICAgIDwvc2VsZWN0PgogICAgICAgICAgICAgICAgPGJ1dHRvbiBvbmNsaWNrPSJtYWluKCkiPlNlbmQgUmVxdWVzdDwvYnV0dG9uPgogICAgICAgICAgICA8L2Rpdj4KICAgICAgICA8L2Rpdj4KICAgICAgICA8ZGl2PgogICAgICAgICAgICA8dGV4dGFyZWEgcmVhZG9ubHkgc3BlbGxjaGVjaz0iZmFsc2UiIHBsYWNlaG9sZGVyPSJSZXNwb25zZSBEYXRhIiBpZD0icmVzcG9uc2VCb2R5Ij48L3RleHRhcmVhPgogICAgICAgICAgICA8ZGl2PgogICAgICAgICAgICAgICAgPHRleHRhcmVhIHJlYWRvbmx5IHNwZWxsY2hlY2s9ImZhbHNlIiBwbGFjZWhvbGRlcj0iUmVwb25zZSBDb2RlIiBpZD0icmVzcG9uc2VTdGF0dXMiPjwvdGV4dGFyZWE+CiAgICAgICAgICAgIDwvZGl2PgogICAgICAgIDwvZGl2PgogICAgICAgIDxzcGFuIGlkPSJhcGlEb2MiPjwvc3Bhbj4KICAgICAgICA8c3BhbiBpZD0iZGJEb2MiPgogICAgICAgICAgICA8aDI+RGF0YWJhc2UgUmVmZXJlbmNlPC9oMj4KICAgICAgICAgICAgPHA+VGhpcyBpcyBhIHJvdWdoIGRyYWZ0IHRoYXQgd2FzIHByb3ZpZGVkIGJ5IEVyZHMsIGFuZCBpcyBub3QgdXBkYXRlZC48L3A+CiAgICAgICAgICAgIDx0YWJsZSBib3JkZXI9MT4KICAgICAgICAgICAgPHRyPgogICAgICAgICAgICA8dGQgYmdjb2xvcj1zaWx2ZXIgY2xhc3M9J21lZGl1bSc+cm9sZUlEIHwgYnl0ZVszMl0gPGJyPiAgQllURVMoMzIpIC0gU0hBMjU2KHJvbGUpPC90ZD4KICAgICAgICAgICAgPHRkIGJnY29sb3I9c2lsdmVyIGNsYXNzPSdtZWRpdW0nPnJvbGUgfCBTdHJpbmcgPGJyPiBWQVJDSEFSKDEwKTwvdGQ+CiAgICAgICAgICAgIDwvdHI+CgogICAgICAgICAgICA8dHI+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Li4uPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz5TdGFmZjwvdGQ+CiAgICAgICAgICAgIDwvdHI+CgogICAgICAgICAgICA8dHI+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Li4uPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz5QYXRpZW50PC90ZD4KICAgICAgICAgICAgPC90cj4KCiAgICAgICAgICAgIDx0cj4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPkFkbWluPC90ZD4KICAgICAgICAgICAgPC90cj4KICAgICAgICAgICAgPC90YWJsZT4KCiAgICAgICAgICAgIDxicj4gCgogICAgICAgICAgICA8dGFibGUgYm9yZGVyPTE+CiAgICAgICAgICAgIDx0cj4KICAgICAgICAgICAgPHRkIGJnY29sb3I9c2lsdmVyIGNsYXNzPSdtZWRpdW0nPnVzZXJJRCB8IGJ5dGVbMzJdIDxicj4gQllURVMoMzIpIC0gU0hBMjU2KGVtYWlsKTwvdGQ+CiAgICAgICAgICAgIDx0ZCBiZ2NvbG9yPXNpbHZlciBjbGFzcz0nbWVkaXVtJz5lbWFpbCB8IFN0cmluZyA8YnI+IFZBUkNIQVIoMTAwKTwvdGQ+CiAgICAgICAgICAgIDx0ZCBiZ2NvbG9yPXNpbHZlciBjbGFzcz0nbWVkaXVtJz5oYXNoZWRQYXNzIHwgYnl0ZVszMl0gPGJyPiBCWVRFUygzMikgLSBTSEEyNTYocGFzc3dvcmQpPC90ZD4KICAgICAgICAgICAgPHRkIGJnY29sb3I9c2lsdmVyIGNsYXNzPSdtZWRpdW0nPmZpcnN0TmFtZSB8IFN0cmluZyA8YnI+IFZBUkNIQVIoMzUpPC90ZD4KICAgICAgICAgICAgPHRkIGJnY29sb3I9c2lsdmVyIGNsYXNzPSdtZWRpdW0nPmxhc3ROYW1lIHwgU3RyaW5nIDxicj4gVkFSQ0hBUigzNSk8L3RkPgogICAgICAgICAgICA8dGQgYmdjb2xvcj1zaWx2ZXIgY2xhc3M9J21lZGl1bSc+c2V4IHwgY2hhciA8YnI+IENIQVI8L3RkPgogICAgICAgICAgICA8dGQgYmdjb2xvcj1zaWx2ZXIgY2xhc3M9J21lZGl1bSc+YmlydGhEYXRlIHwgaW50IDxicj4gSU5URUdFUiAtLSBVVENfU0VDT05EUzwvdGQ+CiAgICAgICAgICAgIDx0ZCBiZ2NvbG9yPXNpbHZlciBjbGFzcz0nbWVkaXVtJz5hZGRyZXNzIHwgU3RyaW5nIDxicj4gVkFSQ0hBUigxMDApPC90ZD4KICAgICAgICAgICAgPHRkIGJnY29sb3I9c2lsdmVyIGNsYXNzPSdtZWRpdW0nPnBob25lTnVtYmVyIHwgU3RyaW5nIDxicj4gVkFSQ0hBUigxMCk8L3RkPgogICAgICAgICAgICA8dGQgYmdjb2xvcj1zaWx2ZXIgY2xhc3M9J21lZGl1bSc+cm9sZUlEIHwgYnl0ZVszMl0gPGJyPiBCWVRFUygzMikgLSBTSEEyNTYocm9sZSk8L3RkPgogICAgICAgICAgICA8dGQgYmdjb2xvcj1zaWx2ZXIgY2xhc3M9J21lZGl1bSc+ZGV0YWlsIHwgU3RyaW5nIDxicj4gVEVYVDwvdGQ+CiAgICAgICAgICAgIDwvdHI+CgogICAgICAgICAgICA8dHI+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Li4uPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz5KYXlTb2huQGVtYWlsLmNvbTwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Li4uPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz5KYXk8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPlNvaG48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPk08L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjk0ODYxNDQwMDwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+MTIzIEFkZHJlc3MgQ3Q8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjkxNjM1OTc0Mzc8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+PC90ZD4KICAgICAgICAgICAgPC90cj4KCiAgICAgICAgICAgIDx0cj4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPlN0ZXBoRnVAc2NoZWR1bGVkc21pbGVzLmNvbTwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Li4uPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz5TdGVwaDwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+RnU8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPkY8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjU2NzY0ODAwMDwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+NDU2IEFkZHJlc3MgQXZlPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz45MTY5Njc4MTIxPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjwvdGQ+CiAgICAgICAgICAgIDwvdHI+CgogICAgICAgICAgICA8dHI+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Li4uPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz5BZGFtTWluaEBzY2hlZHVsZWRzbWlsZXMuY29tPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPkFkYW08L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPk1pbmg8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPk08L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjE2OTE3MTIwMDwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Nzg5IEFkZHJlc3MgV2F5PC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz45MTY2NTM0MTI0PC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjwvdGQ+CiAgICAgICAgICAgIDwvdHI+CgogICAgICAgICAgICA8dHI+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Li4uPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz5TdGV3YXJ0RmVycmlzQHNjaGVkdWxlZHNtaWxlcy5jb208L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+U3Rld2FydDwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+RmVycmlzPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz5NPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz45MDU5OTA0MDA8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjQ1NiBBZGRyZXNzIEJsdmQ8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjkxNjAyNTg0Mjk8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+PC90ZD4KICAgICAgICAgICAgPC90cj4KCiAgICAgICAgICAgIDx0cj4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPkpvaG5Eb2VAZW1haWwuY29tPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPkpvaG48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPkRvZTwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+TTwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+NzU5MjgzMjAwPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4xMjMgQWRkcmVzcyBMYW5lPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4xMjM0NTY3ODkwPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjwvdGQ+CiAgICAgICAgICAgIDwvdHI+CgogICAgICAgICAgICA8dHI+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Li4uPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz5Bbm5pZVllYWdlckBzY2hlZHVsZWRzbWlsZXMuY29tPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPkFubmllPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz5ZZWFnZXI8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPkY8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjM5NzUyNjQwMDwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Nzg5IEFkZHJlc3MgRHI8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjkxNjc5NTQzMjk8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+PC90ZD4KICAgICAgICAgICAgPC90cj4KCiAgICAgICAgICAgIDx0cj4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPkVsaXNlRmxvc3Ntb3JlQHNjaGVkdWxlZHNtaWxlcy5jb208L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+RWxpc2U8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPkZsb3NzbW9yZTwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+RjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Nzc0NzA1NjAwPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz40NTYgQWRkcmVzcyBEcjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+OTE2NTU5MjA2MzwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Li4uPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz48L3RkPgogICAgICAgICAgICA8L3RyPgoKICAgICAgICAgICAgPHRyPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+SmFuZURvZUBlbWFpbC5jb208L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+SmFuZTwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+RG9lPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz5GPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz44MjY3NjE2MDA8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjEyMyBBZGRyZXNzIExhbmU8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjMxNDE1OTI2NTQ8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+PC90ZD4KICAgICAgICAgICAgPC90cj4KICAgICAgICAgICAgPC90YWJsZT4KCiAgICAgICAgICAgIDxicj4KCiAgICAgICAgICAgIDx0YWJsZSBib3JkZXI9MT4KICAgICAgICAgICAgPHRyPgogICAgICAgICAgICAgICAgPHRkIGJnY29sb3I9c2lsdmVyIGNsYXNzPSdtZWRpdW0nPnN0YWZmSUQgfCBieXRlWzMyXSA8YnI+IEJZVEVTKDMyKSAtIFNIQTI1NihlbWFpbCk8L3RkPgogICAgICAgICAgICAgICAgPHRkIGJnY29sb3I9c2lsdmVyIGNsYXNzPSdtZWRpdW0nPmhybHlXYWdlIHwgZG91YmxlIDxicj4gREVDSU1BTCgxMCwgMikgPC90ZD4KICAgICAgICAgICAgPC90cj4KCiAgICAgICAgICAgIDx0cj4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjg4Ljc1PC90ZD4KICAgICAgICAgICAgPC90cj4KCiAgICAgICAgICAgIDx0cj4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjM0LjUwPC90ZD4KICAgICAgICAgICAgPC90cj4KCiAgICAgICAgICAgIDx0cj4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjU1Ljc1PC90ZD4KICAgICAgICAgICAgPC90cj4KICAgICAgICAgICAgPC90YWJsZT4KCiAgICAgICAgICAgIDxicj4KCiAgICAgICAgICAgIDx0YWJsZSBib3JkZXI9MT4KICAgICAgICAgICAgPHRyPgogICAgICAgICAgICA8dGQgYmdjb2xvcj1zaWx2ZXIgY2xhc3M9J21lZGl1bSc+YXBwb2ludG1lbnRJRCB8IGJ5dGVbMzJdIDxicj4gQllURVMoMzIpIC0gU0hBMjU2KCdBcHBvaW50bWVudCcgKyB1bmlxdWVJRCkpPC90ZD4KICAgICAgICAgICAgPHRkIGJnY29sb3I9c2lsdmVyIGNsYXNzPSdtZWRpdW0nPnBhdGllbnRJRCB8IGJ5dGVbMzJdIDxicj4gQllURVMoMzIpIC0gU0hBMjU2KGVtYWlsKTwvdGQ+CiAgICAgICAgICAgIDx0ZCBiZ2NvbG9yPXNpbHZlciBjbGFzcz0nbWVkaXVtJz5zdGFydFRpbWUgfCBpbnQgPGJyPiBJTlRFR0VSPC90ZD4KICAgICAgICAgICAgPHRkIGJnY29sb3I9c2lsdmVyIGNsYXNzPSdtZWRpdW0nPnN0YWZmMUlEIHwgYnl0ZVszMl0gPGJyPiBCWVRFUygzMikgLSBTSEEyNTYoZW1haWwpPC90ZD4KICAgICAgICAgICAgPHRkIGJnY29sb3I9c2lsdmVyIGNsYXNzPSdtZWRpdW0nPnN0YWZmMklEIHwgYnl0ZVszMl0gPGJyPiBCWVRFUygzMikgLSBTSEEyNTYoZW1haWwpPC90ZD4KICAgICAgICAgICAgPHRkIGJnY29sb3I9c2lsdmVyIGNsYXNzPSdtZWRpdW0nPnN0YWZmM0lEIHwgYnl0ZVszMl0gPGJyPiBCWVRFUygzMikgLSBTSEEyNTYoZW1haWwpPC90ZD4KICAgICAgICAgICAgPHRkIGJnY29sb3I9c2lsdmVyIGNsYXNzPSdtZWRpdW0nPmlzQ2FuY2VsZWQgfCBib29sZWFuIDxicj4gVElOWUlOVCgxKTwvdGQ+CiAgICAgICAgICAgIDwvdHI+CgogICAgICAgICAgICA8dHI+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Li4uPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjE3MzEwOTI0MDA8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+TlVMTDwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+TlVMTDwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+MTwvdGQ+CiAgICAgICAgICAgIDwvdHI+CgogICAgICAgICAgICA8dHI+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Li4uPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjE3MzEwODcwMDA8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+TlVMTDwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+TlVMTDwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+MDwvdGQ+CiAgICAgICAgICAgIDwvdHI+CgogICAgICAgICAgICA8dHI+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Li4uPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjE3MzEwOTA2MDA8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+TlVMTDwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+TlVMTDwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+MDwvdGQ+CiAgICAgICAgICAgIDwvdHI+CgogICAgICAgICAgICA8dHI+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Li4uPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjE3MzIyOTQ4MDA8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Li4uPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjA8L3RkPgogICAgICAgICAgICA8L3RyPgoKICAgICAgICAgICAgPHRyPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Li4uPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4xNzMxNjkwMDAwPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+TlVMTDwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+MDwvdGQ+CiAgICAgICAgICAgIDwvdHI+CgogICAgICAgICAgICA8dHI+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Li4uPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPjE3MzE2OTM2MDA8L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+TlVMTDwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+TlVMTDwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+MDwvdGQ+CiAgICAgICAgICAgIDwvdHI+CiAgICAgICAgICAgIDwvdGFibGU+CgogICAgICAgICAgICA8YnI+CgogICAgICAgICAgICA8dGFibGUgYm9yZGVyPTE+CiAgICAgICAgICAgIDx0cj4KICAgICAgICAgICAgPHRkIGJnY29sb3I9c2lsdmVyIGNsYXNzPSdtZWRpdW0nPnR5cGVJRCB8IGJ5dGVbMzJdIDxicj4gQllURVMoMzIpIC0gU0hBMjU2KGFwcG9pbnRtZW5UeXBlKSk8L3RkPgogICAgICAgICAgICA8dGQgYmdjb2xvcj1zaWx2ZXIgY2xhc3M9J21lZGl1bSc+YXBwb2ludG1lbnRUeXBlIHwgU3RyaW5nIDxicj4gVkFSQ0hBUig1MCk8L3RkPgogICAgICAgICAgICA8dGQgYmdjb2xvcj1zaWx2ZXIgY2xhc3M9J21lZGl1bSc+Y29zdCB8IGRvdWJsZSA8YnI+IERFQ0lNQUwoMTAsIDIpPC90ZD4KICAgICAgICAgICAgPC90cj4KCiAgICAgICAgICAgIDx0cj4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPkNsZWFuaW5nPC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4yNTAuMDA8L3RkPgogICAgICAgICAgICA8L3RyPgoKICAgICAgICAgICAgPHRyPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+Q2hlY2t1cDwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+MjAwLjAwPC90ZD4KICAgICAgICAgICAgPC90cj4KCiAgICAgICAgICAgIDx0cj4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPkVtZXJnZW5jeTwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+NzUwLjAwPC90ZD4KICAgICAgICAgICAgPC90cj4KCiAgICAgICAgICAgIDx0cj4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4uLi48L3RkPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPlgtUmF5PC90ZD4KICAgICAgICAgICAgPHRkIGNsYXNzPSdub3JtYWwnIHZhbGlnbj0ndG9wJz4yMDAuMDA8L3RkPgogICAgICAgICAgICA8L3RyPgoKICAgICAgICAgICAgPHRyPgogICAgICAgICAgICA8dGQgY2xhc3M9J25vcm1hbCcgdmFsaWduPSd0b3AnPi4uLjwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+RmlsbGluZzwvdGQ+CiAgICAgICAgICAgIDx0ZCBjbGFzcz0nbm9ybWFsJyB2YWxpZ249J3RvcCc+MzAwLjAwPC90ZD4KICAgICAgICAgICAgPC90cj4KICAgICAgICAgICAgPC90YWJsZT4KICAgICAgICA8L3NwYW4+CiAgICA8L2Rpdj4KICA8L2JvZHk+CjwvaHRtbD4="
                ), CHARSET);
                sendResponse(STATUS_CODES.get("OK"), HEADER_TYPES.get("HTML"), testingHtmlB64.length(), new ByteArrayInputStream(testingHtmlB64.getBytes(CHARSET)));
            } else if (requestPath.length == 0) {
                // idk, redirect to home page i guess
                handleRequest();
            } else if(requestPath[1].equals("api")) {
                System.out.println("API endpoint called");
                handleApiRequest();
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