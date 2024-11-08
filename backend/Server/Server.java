package Server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.File;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
// Used for testing to create shallow JSON strings. -Kyle
import java.lang.StringBuilder;

import static Server.Enum.HttpConstants.*;
import static Server.Enum.Pages.*;

public class Server implements Runnable {
    private static String hostname = "localhost"; // Change this if we actually host this on a domain. -Kyle
    private static final int backlog = -1; // maximum number of connections allowed in queue. >=0 values set value to OS-specific defualts. -Kyle
    private static final int serverCloseDelay = 2; // how many seconds to wait for connections to close themselves after server stop
    private static int port = 8081; // Defualt port for http servers is 8080 -Kyle
    private static final String localContext = "./"; // File context on local machine to serve requested files (NOT related to Java imports, this is for HTTP connections) -Kyle
    private static final String serverContext = "/"; // the root context to for the server to respond to when listening for requests. (the part that needs to go at the end of the URL) -Kyle
    
    private static HttpServer server;
    public Server(final String hostname, final int port) throws IOException {
        this.hostname = hostname;
        this.port = port;
        server = HttpServer.create(new InetSocketAddress(hostname, port), backlog);
        this.server.setExecutor(null); // creates a default executor
    }
    
    private static boolean isSocketUsed(final String hostname, final int port) { 
    try (Socket s = new Socket(hostname, port)) {
        return true;
    } catch (IOException ex) {
        // ignore, means that socket is occupied already
    }
    return false;
}
    
    public static boolean isRunning() {
        return isSocketUsed(hostname, port);
    }
    
    public static String getLocalContext() {
        return localContext;
    }
    
    public static String getServerContext() {
        return serverContext;
    }
    
    public static int getPort() {
        return port;
    }
    
    public static String getHostname() {
        return hostname;
    }
    
    public static void stop() {
        try {
            server.stop(serverCloseDelay);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void run() {
        try {
            this.server.createContext(this.serverContext, new ServerConnectionHandler());
            this.server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(final String... args) throws IOException {
        server = HttpServer.create(new InetSocketAddress(hostname, port), backlog);
        server.createContext(serverContext, new ServerConnectionHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println(String.format("Server open on port %s", port));
        System.out.println(String.format("Connect via: http://localhost:%s", port));
        // testing
        System.out.println(" ".repeat(5) + "Press enter to quit");
        System.in.read();
        System.out.println(String.format("Stopping server, waiting up to %s seconds...", serverCloseDelay));
        // -----
        server.stop(serverCloseDelay);
        if (isRunning()) {
            System.out.println(String.format("Server was not properly close on hostname %s, port %s", hostname, port));
        } else {
            System.out.println(String.format("Server close on port %s", port));
        }
    }
    
    private static String decodeUrlComponent(final String urlComponent) {
        try {
            return URLDecoder.decode(urlComponent, CHARSET.name());
        } catch (final UnsupportedEncodingException e) {
            return null;
        }
    }
    
    private static String getRequestFragment(final String rawRequestFragment) {
        if (rawRequestFragment != null) {
            return decodeUrlComponent(rawRequestFragment);
        }
        return null;
    }
    
    private static Map<String, String> getRequestQueryParameters(final String rawRequestQuery) {
        final Map<String, String> queryParameters = new LinkedHashMap<>();
        if (rawRequestQuery != null) {
            final String[] keyValues = rawRequestQuery.split("[&;]", -1);
            for (final String keyValue : keyValues) {
                final String[] parameter = keyValue.split("=", 2);
                final String key = decodeUrlComponent(parameter[0]);
                final String value = (parameter.length > 1) ? decodeUrlComponent(parameter[1]) : null;
                queryParameters.putIfAbsent(key, value);
            }
        }
        return queryParameters;
    }
    
    // Helper function, remove before deployment
    private static String convertToShallowJsonString(Map<String, String> obj) {
        StringBuilder jsonString = new StringBuilder();
        jsonString.append("[");
        for (var entry : obj.entrySet()) {
            jsonString.append(String.format(
                "\"%s\": \"%s\", ",
                entry.getKey(), entry.getValue()
            ));
        } // who is this jason guy anyways?- KT.
        jsonString.append("]");
        return jsonString.toString();
    }
    
    private static File getFile(final String rawRequestPath) {
        final String requestPath = rawRequestPath.split(serverContext, 2)[1];
        final String filePath = (MAPPED_FILES.containsKey(requestPath)) ? MAPPED_FILES.get(requestPath) : localContext + requestPath;
        final File file = new File(filePath);
        return (file.exists() && !file.isDirectory()) ? file : null;
    }
    
    private static String getFileType(final String filename) {
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }
    
    private static void setReponseContentTypeHeaders(final Headers headers, final String filename) {
        final String fileType = getFileType(filename);
        switch (fileType) {
            case ".css":
                headers.set(HEADER_KEYS.get("CONTENT_TYPE"), HEADER_TYPES.get("CSS"));
            break;
            case ".js":
                headers.set(HEADER_KEYS.get("CONTENT_TYPE"), HEADER_TYPES.get("JS"));
            break;
            case ".htm":
            case ".html":
                headers.set(HEADER_KEYS.get("CONTENT_TYPE"), HEADER_TYPES.get("HTML"));
            break;
            // Might not need these last two: txt, json
            case ".txt":
                headers.set(HEADER_KEYS.get("CONTENT_TYPE"), HEADER_TYPES.get("TEXT"));
            break;
            case ".json":
                headers.set(HEADER_KEYS.get("CONTENT_TYPE"), HEADER_TYPES.get("JSON"));
            break;
            default:
                // Sending unknown filetype
                headers.set(HEADER_KEYS.get("CONTENT_DISPOSITION"), "attachment; filename=" + filename);
        }
    }
    
    private static void sendFileResponse(final HttpExchange con) throws IOException {
        try (final OutputStream responseBodyStream = con.getResponseBody()) {
            final String rawRequestPath = con.getRequestURI().getRawPath();
            // Getting file
            final File file = getFile(rawRequestPath);
            if (file != null) {
                // Determine filetype
                final String filename = file.getName();
                final Headers headers = con.getResponseHeaders();
                setReponseContentTypeHeaders(headers, filename);
                // Sending file bytestream
                con.sendResponseHeaders(STATUS_CODES.get("OK"), file.length());
                try (FileInputStream fs = new FileInputStream(file)) {
                    final byte[] buffer = new byte[0x10000];
                    int count = 0;
                    while ((count = fs.read(buffer)) >= 0) {
                        responseBodyStream.write(buffer, 0, count);
                    }
                }
            } else {
                // TODO: handle file not found differently based on the type of file requested -Kyle
                sendTextResponse(con, "File: " + rawRequestPath + " not found.", STATUS_CODES.get("NOT_FOUND"));
            }
        }
    }
    
    // Helper function, sends text response back
    private static void sendTextResponse(final HttpExchange con, final String responseBody, final int statusCode) throws IOException {
        con.getResponseHeaders().set(HEADER_KEYS.get("CONTENT_TYPE"), HEADER_TYPES.get("TEXT"));
        con.sendResponseHeaders(statusCode, responseBody.length());
        con.getResponseBody().write(responseBody.getBytes(CHARSET));
    }
    
    // Helper function for debugging
    private static void sendDebugResonse(final HttpExchange con, final Map<String, String> paramenters) throws IOException {
        // Gather request components
        final URI requestUri = con.getRequestURI();
        final String requestFragment = getRequestFragment(requestUri.getRawFragment()); // currently broken- uri does not contain fragment for some reason -Kyle
        final String rawRequestPath = requestUri.getRawPath();
        
        final String responseBody = String.format(
            "{\"uri\": %s, "
            + "\"params\": %s, "
            + "\"fragment\": %s, "  
            + "\"path\": %s}",
            requestUri,
            convertToShallowJsonString(paramenters),
            requestFragment,
            rawRequestPath
        );
        con.getResponseHeaders().set(HEADER_KEYS.get("CONTENT_TYPE"), HEADER_TYPES.get("JSON"));
        con.sendResponseHeaders(STATUS_CODES.get("OK"), responseBody.length());
        con.getResponseBody().write(responseBody.getBytes(CHARSET));
    }
    
    private static class ServerConnectionHandler implements HttpHandler {
        @Override
        public void handle(final HttpExchange con) throws IOException {
            int responseStatus = STATUS_CODES.get("NOT_ALLOWED");
            int responseLength = NO_RESPONSE_LENGTH;
            try {
                final String requestMethod = con.getRequestMethod().toUpperCase();
                // Switch statements require compile-time constants. That means literals only, no variables whatsoever in the cases. -Kyle
                switch (requestMethod) {
                    case "GET":
                        // Get request parameters
                        final URI reuqestUri = con.getRequestURI();
                        final Map<String, String> requestParameters = getRequestQueryParameters(reuqestUri.getRawQuery());
                        System.out.println("Got a GET request: " + reuqestUri.toString());
                        if (!requestParameters.containsKey("debug")) {
                            sendFileResponse(con);
                        } else {
                            // Remove before deloyment, meant for testing
                            sendDebugResonse(con, requestParameters);
                        }
                    break;
                    case "OPTIONS":
                        con.getResponseHeaders().set(HEADER_KEYS.get("ALLOW"), ALLOWED_METHODS);
                        con.sendResponseHeaders(STATUS_CODES.get("OK"), NO_RESPONSE_LENGTH);
                    break;
                    default:
                        con.sendResponseHeaders(STATUS_CODES.get("NOT_ALLOWED"), NO_RESPONSE_LENGTH);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                con.close();
            }
        }
    }
}