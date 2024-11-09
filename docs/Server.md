# Running the server
Run as a thread (non blocking)
```Java
final String hostname = "localhost";
final int port = "8081;
Thread serverThread = new Thread(new Server());
serverThread.start();
```
Run normally (blocking)
```Java
final String hostname = "localhost";
final int port = "8081;
Server server = new Server(hostname, port);
server.run();
// stopping the server
server.stop();
```