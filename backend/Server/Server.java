package Server;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

// In house packages
import Server.utils.ServerConnectionHandler;

public class Server implements Runnable {
    private static String hostname = "localhost"; // Change this if we actually host this on a domain. -Kyle
    private static final int backlog = -1; // maximum number of connections allowed in queue. >=0 values set value to OS-specific defualts. -Kyle
    private static final int serverCloseDelay = 2; // how many seconds to wait for connections to close themselves after server stop
    private static int port = 8081; // Defualt port for http servers is 8080 -Kyle
    private static String localContext = "./"; // File context on local machine to serve requested files (NOT related to Java imports, this is for HTTP connections) -Kyle
    private static final String serverContext = "/"; // the root context to for the server to respond to when listening for requests. (the part that needs to go at the end of the URL) -Kyle
    private static HttpServer server;

    // Testing
    public static void main(final String... args) throws IOException {
        localContext = (args != null && args.length >= 1 && args[0] != null) ? args[0] : localContext;
        server = HttpServer.create(new InetSocketAddress(hostname, port), backlog);
        server.createContext(serverContext, new ServerConnectionHandler(serverContext, localContext));
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println(String.format("Server open on port %s from context %s", port, localContext));
        System.out.println(String.format("Connect via: http://localhost:%s", port));
        // testing
        System.out.println(" ".repeat(5) + "Press enter to quit");
        System.in.read();
        System.out.println(String.format("Stopping server, waiting up to %s seconds...", serverCloseDelay));
        // -----
        server.stop(serverCloseDelay);
        if (isRunning()) {
            System.out.println(String.format("Server was not properly closed on hostname %s, port %s", hostname, port));
        } else {
            System.out.println(String.format("Server close on port %s", port));
        }
    }
    
    public Server(final String hostname, final int port, final String context) throws IOException {
        this.localContext = context;
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
            this.server.createContext(this.serverContext, new ServerConnectionHandler(serverContext, localContext));
            this.server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}