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
    private static volatile boolean stop = true; // ignored in main method
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
    
    public boolean isRunning() {
        return this.stop;
    }
    
    public String getLocalContext() {
        return this.localContext;
    }
    
    public String getServerContext() {
        return this.serverContext;
    }
    
    public int getPort() {
        return this.port;
    }
    
    public String getHostname() {
        return this.hostname;
    }
    
    public void stop() {
        try {
            this.server.stop(this.serverCloseDelay);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.stop = true;
        }
    }
    
    @Override
    public void run() {
        try {
            this.server.createContext(serverContext, new ServerConnectionHandler());
            this.server.start();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.stop = false;
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
        System.out.println(String.format("Server close on port %s", port));
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
        return (file.exists()) ? file : null;
    }
    
    private static class ServerConnectionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange con) throws IOException {
            int responseStatus = STATUS_CODES.get("NOT_ALLOWED");
            int responseLength = NO_RESPONSE_LENGTH;
            try {
                final Headers headers = con.getResponseHeaders();
                final String requestMethod = con.getRequestMethod().toUpperCase();
                // Switch statements require compile-time constants. That means literals only, no variables whatsoever in the cases. -Kyle
                switch (requestMethod) {
                    case "GET":
                        // Gather request components
                        final URI requestUri = con.getRequestURI();
                        final Map<String, String> requestParameters = getRequestQueryParameters(requestUri.getRawQuery());
                        final String requestFragment = getRequestFragment(requestUri.getRawFragment()); // currently broken- uri does not contain fragment for some reason -Kyle
                        final String rawRequestPath = requestUri.getRawPath();
                        
                        // TODO: do stuff here
                        System.out.println("Got a GET request: " + requestUri.toString());
                        OutputStream responseBodyStream = con.getResponseBody();
                        
                        // Getting file
                        final File file = getFile(rawRequestPath);
                        if (file != null) {
                            // Determine filetype
                            final String fileName = file.getName();
                            final String fileType = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
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
                                    headers.set(HEADER_KEYS.get("CONTENT_DISPOSITION"), "attachment; filename=" + fileName);
                            }
                            con.sendResponseHeaders(STATUS_CODES.get("OK"), file.length());
                            try (FileInputStream fs = new FileInputStream(file)) {
                                final byte[] buffer = new byte[0x10000];
                                int count = 0;
                                while ((count = fs.read(buffer)) >= 0) {
                                    responseBodyStream.write(buffer, 0, count);
                                }
                            }
                        } else {
                            final String responseBodyString = String.format("Error: File \"%s\" not found.", rawRequestPath);
                            headers.set(HEADER_KEYS.get("CONTENT_TYPE"), HEADER_TYPES.get("TEXT"));
                            con.sendResponseHeaders(STATUS_CODES.get("NOT_FOUND"), responseBodyString.length());
                            responseBodyStream.write(responseBodyString.getBytes(CHARSET));
                        }                        
                        responseBodyStream.flush();
                        responseBodyStream.close();
                    break;
                    case "OPTIONS":
                        headers.set(HEADER_KEYS.get("ALLOW"), ALLOWED_METHODS);
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