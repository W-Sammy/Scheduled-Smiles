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
        switch (requestPath[3]) { // TODO: check if request body is malformed
            case "get":
                isValidRequest = membersMatch(requestJsonObject.keySet(), "query", "type");
                if (isValidRequest) {
                    final String queryString = requestJsonObject.get("query").getAsString();
                    final String queryType = requestJsonObject.get("type").getAsString();
                    // Databse connection
                    try (DatabaseConnection db = new DatabaseConnection()) {
                        if (db.isConnected()) {
                            switch (queryType) {    
                                case "string":
                                    results = convertFromJson(db.queryStrings(queryString), String.class);
                                break;
                                case "hex":
                                    results = convertFromJson(db.queryHexStrings(queryString), String.class);
                                break;
                                case "integer":
                                    results = convertFromJson(db.queryIntegers(queryString), int.class);
                                break;
                                case "boolean":
                                    results = convertFromJson(db.queryBooleans(queryString), boolean.class);
                                break;
                                default:
                                    isValidRequest = false;
                            }
                        }
                    } catch (Exception e) {
                        // Errors handled in DatabaseConnection, pass
                    }
                    sendResponse(STATUS_CODES.get("OK"), convertToJsonElement(results));
                }
            break;
            case "send":
                // TODO: implement queries that don't explicity return a value in DatabaseConnection before implementing here. -Kyle
                isValidRequest = membersMatch(requestJsonObject.keySet(), "query");
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
            "firstName", "lastName", "address", "sex", "phone", "email", "birthDate", "password"
        );
        if (isValidRequest) {
            // Testing
            sendResponse(200, "Valid request body");
        } else {
            // Malformed/Invalid request body
            sendResponse(400, "Malformed request body");
        }
    }
    
    private static void getUser() throws IOException {
        final boolean isValidRequest = membersMatch(getKeys(requestBodyJson),
            "email", "password"
        );
        if (isValidRequest) {
            // Testing
            sendResponse(200, "Valid request body");
        } else {
            // Malformed/Invalid request body
            sendResponse(400, "Malformed request body");
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
            sendResponse(400, "Malformed or invalid request");
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
                "PCFET0NUWVBFIGh0bWw+DQo8aHRtbD4NCiAgPGhlYWQ+DQogICAgPHRpdGxlPlRoaXMgaXMgdGhlIHRpdGxlIG9mIHRoZSB3ZWJwYWdlITwvdGl0bGU+DQogICAgPCEtLSBSZW5kZXJpbmcgbWFya2Rvd24gLS0+DQogICAgPHNjcmlwdCBzcmM9Imh0dHBzOi8vY2RuLmpzZGVsaXZyLm5ldC9ucG0vbWFya2Rvd24taXRAMTQuMS4wL2Rpc3QvbWFya2Rvd24taXQubWluLmpzIj48L3NjcmlwdD4NCiAgICA8c2NyaXB0Pg0KICAgICAgICBmdW5jdGlvbiBtYWluKCkgew0KICAgICAgICAgICAgdmFyIFtlbmRwb2ludCwgbWV0aG9kLCBib2R5XSA9IGdldERhdGEoKQ0KICAgICAgICAgICAgc2VuZFJlcXVlc3QoZW5kcG9pbnQsIG1ldGhvZCwgYm9keSkNCiAgICAgICAgICAgIA0KICAgICAgICB9DQoNCiAgICAgICAgZnVuY3Rpb24gZ2V0RGF0YSgpIHsNCiAgICAgICAgICAgIGNvbnN0IHJlcXVlc3RCb2R5QXJlYSA9IGRvY3VtZW50LmdldEVsZW1lbnRCeUlkKCJyZXF1ZXN0Qm9keSIpDQogICAgICAgICAgICBjb25zdCBlbmRwb2ludEFyZWEgPSBkb2N1bWVudC5nZXRFbGVtZW50QnlJZCgiZW5kcG9pbnQiKQ0KICAgICAgICAgICAgY29uc3QgbWV0aG9kc1NlbGVjdCA9IGRvY3VtZW50LmdldEVsZW1lbnRCeUlkKCJtZXRob2RzIikNCiAgICAgICAgICAgIHZhciByZXF1ZXN0Qm9keSA9IHJlcXVlc3RCb2R5QXJlYS52YWx1ZQ0KICAgICAgICAgICAgdmFyIG1ldGhvZCA9IG1ldGhvZHNTZWxlY3QudmFsdWUNCiAgICAgICAgICAgIHZhciBlbmRwb2ludCA9IGVuZHBvaW50QXJlYS52YWx1ZQ0KICAgICAgICAgICAgcmV0dXJuIFtlbmRwb2ludCwgbWV0aG9kLCByZXF1ZXN0Qm9keV0NCiAgICAgICAgfQ0KDQogICAgICAgIGZ1bmN0aW9uIHNlbmRSZXF1ZXN0KGVuZHBvaW50LCBtZXRob2QsIHJlcXVlc3RCb2R5KSB7DQogICAgICAgICAgICBjb25zb2xlLmxvZygiU2VuZGluZyAiICsgbWV0aG9kICsgIiB0byAiICsgZW5kcG9pbnQgKyAiOlxuIiArIHJlcXVlc3RCb2R5KTsNCiAgICAgICAgICAgIGZldGNoKHdpbmRvdy5sb2NhdGlvbi5vcmlnaW4gKyBlbmRwb2ludCwgew0KICAgICAgICAgICAgICAgIG1ldGhvZDogbWV0aG9kLA0KICAgICAgICAgICAgICAgIGJvZHk6IEpTT04uc3RyaW5naWZ5KEpTT04ucGFyc2UocmVxdWVzdEJvZHkpKSwNCiAgICAgICAgICAgICAgICBoZWFkZXJzOiB7DQogICAgICAgICAgICAgICAgICAgICdBY2NlcHQnOiAnYXBwbGljYXRpb24vanNvbicsDQogICAgICAgICAgICAgICAgICAgICJDb250ZW50LXR5cGUiOiAiYXBwbGljYXRpb24vanNvbjsgY2hhcnNldD1VVEYtOCINCiAgICAgICAgICAgICAgICB9DQogICAgICAgICAgICB9KS50aGVuKHJlc3BvbnNlID0+IHsNCiAgICAgICAgICAgICAgICByZXR1cm4gUHJvbWlzZS5hbGwoW3Jlc3BvbnNlLnN0YXR1cywgcmVzcG9uc2UudGV4dCgpXSkNCiAgICAgICAgICAgIH0sIG5ldHdvcmtFcnJvciA9PiB7DQogICAgICAgICAgICAgIGNvbnNvbGUubG9nKG5ldHdvcmtFcnJvci5tZXNzYWdlKQ0KICAgICAgICAgICAgICBsb2FkUmVzcG9uc2UoIiIsIG5ldHdvcmtFcnJvci5tZXNzYWdlKQ0KICAgICAgICAgICAgfSkudGhlbigodmFsdWVzKSA9PiB7DQogICAgICAgICAgICAgIHZhciByZXNwb25zZVRleHQgPSAgKHZhbHVlc1swXSA+PSA0MDApID8gdmFsdWVzWzFdIDogSlNPTi5zdHJpbmdpZnkoSlNPTi5wYXJzZSh2YWx1ZXNbMV0pLCBudWxsLCAyKQ0KICAgICAgICAgICAgICBjb25zb2xlLmxvZyh2YWx1ZXMpDQogICAgICAgICAgICAgIGxvYWRSZXNwb25zZSh2YWx1ZXNbMF0sIHJlc3BvbnNlVGV4dCkNCiAgICAgICAgICAgIH0pDQogICAgICAgIH0NCg0KICAgICAgICAvLyBEZXYgb25seS4gcmVtb3ZlIGJlZm9yZSBkZXBsb3ltZW50LiBSZXF1aXJlcyBpbXBvcnQgb2YgdGhlIG1hcmtlZCBKUyBsaWJyYXJ5IC1LeWxlDQogICAgICAgIGZ1bmN0aW9uIHJlbmRlckFwaURvYygpIHsNCiAgICAgICAgICAgIGNvbnN0IG1kID0gbWFya2Rvd25pdCh7DQogICAgICAgICAgICAgIGh0bWw6IHRydWUsDQogICAgICAgICAgICAgIGxpbmtpZnk6IHRydWUsDQogICAgICAgICAgICAgIHR5cG9ncmFwaGVyOiB0cnVlDQogICAgICAgICAgICB9KQ0KICAgICAgICAgIGZldGNoKCJodHRwczovL3Jhdy5naXRodWJ1c2VyY29udGVudC5jb20vVy1TYW1teS9TY2hlZHVsZWQtU21pbGVzL3JlZnMvaGVhZHMvYmFja2VuZC9kb2NzL0FQSV9SRUZFUkVOQ0UubWQiKSAgICAgIC8vIFRoZSBwYXRoIHRvIHRoZSByYXcgTWFya2Rvd24gZmlsZQ0KICAgICAgICAgICAgLnRoZW4ocmVzcG9uc2UgPT4gcmVzcG9uc2UudGV4dCgpKQ0KICAgICAgICAgICAgLnRoZW4obWFya2Rvd24gPT4gew0KICAgICAgICAgICAgICBkb2N1bWVudC5nZXRFbGVtZW50QnlJZCgiYXBpRG9jIikuaW5uZXJIVE1MID0gbWQucmVuZGVyKG1hcmtkb3duKS8vbWFya2VkLnBhcnNlKG1hcmtkb3duKQ0KICAgICAgICAgICAgfSkNCiAgICAgICAgfQ0KDQogICAgICAgIHdpbmRvdy5vbmxvYWQgPSAoKSA9PiB7DQogICAgICAgICAgICByZW5kZXJBcGlEb2MoKQ0KICAgICAgICB9DQogICAgICAgICAgICANCiAgICAgICAgZnVuY3Rpb24gbG9hZFJlc3BvbnNlKHJlc3BvbnNlU3RhdHVzLCByZXNwb25zZVRleHQpIHsNCiAgICAgICAgICAgIGNvbnN0IHJlc3BvbnNlQm9keUFyZWEgPSBkb2N1bWVudC5nZXRFbGVtZW50QnlJZCgicmVzcG9uc2VCb2R5IikNCiAgICAgICAgICAgIGNvbnN0IHJlc3BvbnNlU3RhdHVzQXJlYSA9IGRvY3VtZW50LmdldEVsZW1lbnRCeUlkKCJyZXNwb25zZVN0YXR1cyIpDQogICAgICAgICAgICByZXNwb25zZUJvZHlBcmVhLnZhbHVlID0gcmVzcG9uc2VUZXh0DQogICAgICAgICAgICByZXNwb25zZVN0YXR1c0FyZWEuaW5uZXJIVE1MID0gcmVzcG9uc2VTdGF0dXMNCiAgICAgICAgfQ0KICAgIDwvc2NyaXB0Pg0KICAgIDxzdHlsZT4NCiAgICAgICAgaHRtbCwgYm9keSB7DQogICAgICAgICAgICBiYWNrZ3JvdW5kLWNvbG9yOiAjMjMyNzJBOyAvKiBNWSBFWUVTISAqLw0KICAgICAgICAgICAgY29sb3I6IHdoaXRlOw0KICAgICAgICAgICAgZm9udC1mYW1pbHk6IEFyaWFsLCBIZWx2ZXRpY2EsIHNhbnMtc2VyaWY7IC8qIGxlc3MgaGFyc2ggdG8gbG9vayBhdCBkdXJpbmcgZGV2LCBzdWJqZWN0IHRvIGNoYW5nZSBpbiBhY3R1YWwgYXBwICovDQogICAgICAgIH0NCg0KICAgICAgICBib2R5ID4gZGl2ID4gKiB7DQogICAgICAgICAgICBmbG9hdDogbGVmdDsNCiAgICAgICAgICAgIHBhZGRpbmctcmlnaHQ6IDM1cHg7DQogICAgICAgIH0NCg0KICAgICAgICAjcmVzcG9uc2VTdGF0dXMgew0KICAgICAgICAgICAgYmFja2dyb3VuZC1jb2xvcjogIzQyNDU0OTsNCiAgICAgICAgICAgIGhlaWdodDogMS41ZW07DQogICAgICAgICAgICB3aWR0aDogMTBlbTsNCiAgICAgICAgICAgIGNvbG9yOiB3aGl0ZTsNCiAgICAgICAgfQ0KDQogICAgICAgICNlbmRwb2ludCB7DQogICAgICAgICAgICBiYWNrZ3JvdW5kLWNvbG9yOiAjNDI0NTQ5Ow0KICAgICAgICAgICAgaGVpZ2h0OiAxLjVlbTsNCiAgICAgICAgICAgIHdpZHRoOiA0NWVtOw0KICAgICAgICAgICAgY29sb3I6IHdoaXRlOw0KICAgICAgICB9DQoNCiAgICAgICAgI3JlcXVlc3RCb2R5LCAjcmVzcG9uc2VCb2R5IHsNCiAgICAgICAgICAgIGJvcmRlci1yYWRpdXM6IDE1cHg7DQogICAgICAgICAgICBiYWNrZ3JvdW5kLWNvbG9yOiAjNDI0NTQ5Ow0KICAgICAgICAgICAgcGFkZGluZzogMTBweDsNCiAgICAgICAgICAgIGhlaWdodDogMjAwcHg7DQogICAgICAgICAgICB3aWR0aDogMTUwcHg7DQogICAgICAgICAgICBjb2xvcjogd2hpdGU7DQogICAgICAgIH0NCg0KICAgICAgICAjcmVzcG9uc2VCb2R5IHsNCiAgICAgICAgICAgIHdpZHRoOiA1MDBweDsNCiAgICAgICAgfQ0KDQogICAgICAgIHNlbGVjdCB7DQogICAgICAgICAgYXBwZWFyYW5jZTogbm9uZTsNCiAgICAgICAgICBwYWRkaW5nOiA1cHg7DQogICAgICAgIH0NCiAgICAgICAgb3B0aW9uIHsNCiAgICAgICAgICAgIGJhY2tncm91bmQtY29sb3I6ICM0MjQ1NDkgIWltcG9ydGFudDsNCiAgICAgICAgfQ0KICAgICAgICAjYnV0dG9ucyAqIHsNCiAgICAgICAgICAgIGJhY2tncm91bmQtY29sb3I6IGluaGVyaXQ7DQogICAgICAgICAgICBjb2xvcjogaW5oZXJpdDsNCiAgICAgICAgfQ0KDQogICAgICAgICNhcGlEb2Mgew0KICAgICAgICAgICAgbWFyZ2luOiA1cHg7ICAgIA0KICAgICAgICAgICAgZGlzcGxheTogYmxvY2s7DQogICAgICAgICAgICBib3JkZXItcmFkaXVzOiAxNXB4Ow0KICAgICAgICAgICAgYmFja2dyb3VuZC1jb2xvcjogIzQyNDU0OSAhaW1wb3J0YW50Ow0KICAgICAgICAgICAgcGFkZGluZzogMTBweDsNCiAgICAgICAgfQ0KDQogICAgICAgICNhcGlEb2MgKiB7DQogICAgICAgICAgd2hpdGUtc3BhY2UgOiBwcmUtd3JhcCAhaW1wb3J0YW50Ow0KICAgICAgICB9DQogICAgPC9zdHlsZT4NCiAgPC9oZWFkPg0KICA8Ym9keT4NCiAgICA8cD5UaGlzIHBhZ2UgaXMgYSB0b29sIGZvciBkZWJ1Z2dpbmcgb3VyIHByb2plY3QncyBBUEkuIENoZWNrIGNvbnNvbGUgb3V0cHV0IGluIERldlRvb2xzIHdpbmRvd3MgKGN0cmwrc2hpZnQraSkuPC9wPg0KICAgIDx0ZXh0YXJlYSBpZD0iZW5kcG9pbnQiIHNwZWxsY2hlY2s9ImZhbHNlIiBwbGFjZWhvbGRlcj0iRW5kcG9pbnQiPi9hcGkvZGF0YWJhc2UvZ2V0PC90ZXh0YXJlYT4NCiAgICA8ZGl2Pg0KICAgICAgICA8ZGl2Pg0KICAgICAgICAgICAgPHRleHRhcmVhIGlkPSJyZXF1ZXN0Qm9keSIgc3BlbGxjaGVjaz0iZmFsc2UiIHBsYWNlaG9sZGVyPSJSZXF1ZXN0IEJvZHkgKEpTT04gZm9ybWF0dGVkKSI+eyAicXVlcnkiOiAiU0VMRUNUIHJvbGVJZCBGUk9NIHJvbGVUeXBlcyIsICJ0eXBlIjogImhleCIgfTwvdGV4dGFyZWE+DQogICAgICAgICAgICA8ZGl2IGlkPSJidXR0b25zIj4NCiAgICAgICAgICAgICAgICA8c2VsZWN0IG5hbWU9Im1ldGhvZHMiIGlkPSJtZXRob2RzIj4NCiAgICAgICAgICAgICAgICAgICAgPG9wdGlvbiB2YWx1ZT0iR0VUIj5HRVQ8L29wdGlvbj4NCiAgICAgICAgICAgICAgICAgICAgPG9wdGlvbiB2YWx1ZT0iUE9TVCIgc2VsZWN0ZWQ+UE9TVDwvb3B0aW9uPg0KICAgICAgICAgICAgICAgICAgICA8b3B0aW9uIHZhbHVlPSJPUFRJT05TIj5PUFRJT05TPC9vcHRpb24+DQogICAgICAgICAgICAgICAgICAgIDxvcHRpb24gdmFsdWU9IlBVVCI+UFVUPC9vcHRpb24+DQogICAgICAgICAgICAgICAgPC9zZWxlY3Q+DQogICAgICAgICAgICAgICAgPGJ1dHRvbiBvbmNsaWNrPSJtYWluKCkiPlNlbmQgUmVxdWVzdDwvYnV0dG9uPg0KICAgICAgICAgICAgPC9kaXY+DQogICAgICAgIDwvZGl2Pg0KICAgICAgICA8ZGl2Pg0KICAgICAgICAgICAgPHRleHRhcmVhIHJlYWRvbmx5IHNwZWxsY2hlY2s9ImZhbHNlIiBwbGFjZWhvbGRlcj0iUmVzcG9uc2UgRGF0YSIgaWQ9InJlc3BvbnNlQm9keSI+PC90ZXh0YXJlYT4NCiAgICAgICAgICAgIDxkaXY+DQogICAgICAgICAgICAgICAgPHRleHRhcmVhIHJlYWRvbmx5IHNwZWxsY2hlY2s9ImZhbHNlIiBwbGFjZWhvbGRlcj0iUmVwb25zZSBDb2RlIiBpZD0icmVzcG9uc2VTdGF0dXMiPjwvdGV4dGFyZWE+DQogICAgICAgICAgICA8L2Rpdj4NCiAgICAgICAgPC9kaXY+DQogICAgICAgIDxzcGFuIGlkPSJhcGlEb2MiPjwvc3Bhbj4NCiAgICA8L2Rpdj4NCiAgPC9ib2R5Pg0KPC9odG1sPg=="
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