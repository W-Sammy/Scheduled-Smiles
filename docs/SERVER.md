# Running the server
Run as a thread (non blocking)
```Java
final String hostname = "localhost";
final int port = 8081;
final String localContext = "./"
Thread serverThread = new Thread(new Server(hostname, port, localContext));
serverThread.start();
```
Run normally (blocking)
```Java
final String hostname = "localhost";
final int port = 8081;
final String localContext = "./frontend/"
Server server = new Server(hostname, port, localContext);
server.run();
// stopping the server
server.stop();
```
