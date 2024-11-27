import Server.Server;
import java.io.IOException;
public class Main {
    public static void main(String[] args) throws IOException {
        new Server("localhost", 8081, "./frontend/").run();
        System.out.println(String.format("Server open on port %s from context %s", 8081, "./frontend/"));
        System.out.println(String.format("Connect via: http://localhost:%s", 8081));
    }
}