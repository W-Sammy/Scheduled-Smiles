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
            if (requestPath.length == 0) {
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