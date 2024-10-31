import java.sql.*;
import java.util.*;
class DatabaseConnection {
    private String databaseUrl = "jdbc:mysql://localhost:3306";
    private String databaseUsername = "admin";
    private String databasePassword = "password";
    private Connection con = null;
    private Statement transaction = null;
    DatabaseConnection() {
        this.registerDriver();
    }
    DatabaseConnection(String url) {
        this.setUrl(url);
        this.registerDriver();
    }
    DatabaseConnection(String url, String username, String password) {
        this.setUrl(url);
        this.setUsername(username);
        this.setPassword(password);
        this.registerDriver();
    }
    private void registerDriver() {
        // We let the exception pass here, but output to console- nothing else will work if this fails anyways. -Kyle
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception wtfHappened) {
            System.out.println("Error: Failed to register JDBC Driver. Trace:");
            wtfHappened.printStackTrace();
        }
    }
    public boolean isConnected() {
        return this.con != null;
    }
    // Gets all columns at once, connection can be closed immedately after calling
    // T is assumed to be the type of ALL returned columns- cannot return columns that are a mix of different types
    public ArrayList<Object>  executeQuery(String query) {
        boolean connectionWasActive = this.isConnected();
        if (!connectionWasActive) {
            this.connect();
        }
        ResultSet results = null;
        ResultSetMetaData resultMetaData = null;
        int columnCount = 0;
        ArrayList<Object> resultData = null;
        try {
            this.transaction = this.con.createStatement();
            results = this.transaction.executeQuery(query);
            resultMetaData = results.getMetaData();
            columnCount = resultMetaData.getColumnCount();
            resultData = new ArrayList<Object>(columnCount);
        } catch (Exception wtfHappened) {
            System.out.println("Error: Failed to open a database transaction. Trace:");
            wtfHappened.printStackTrace();
        }
        try {
            while (results.next()) {
                int i = 1;
                while(i <= columnCount) {
                        resultData.add(results.getObject(i++));
                }
            }
        } catch (Exception wtfHappened) {
            System.out.println("Error: Failed while getting result columns. Trace:");
            wtfHappened.printStackTrace();
        }
        this.closeTransaction();
        // If connection was not already open, then assume that this is going to be a one-time thing after the transaction -Kyle
        if (!connectionWasActive) {
            this.disconnect();
        }
        return resultData;
    }
    public void connect() {
        try {
            this.con = DriverManager.getConnection(
                this.databaseUrl,
                this.databaseUsername,
                this.databasePassword
            );
        } catch (Exception wtfHappened) {
            System.out.println("Error: Failed to connect to database. Trace:");
            wtfHappened.printStackTrace();
        }
    }
    public void closeTransaction() {  
        try {
            this.transaction.close();
            this.transaction = null;
        } catch (Exception wtfHappened) {
            System.out.println("Error: Failed to close transaction. Is a transaction active? Trace:");
            wtfHappened.printStackTrace();
        }
    }
    public void disconnect() {
        try {
            this.con.close();
            this.con = null;
        } catch (Exception wtfHappened) {
            System.out.println("Error: Failed to close database connection. Is a connection active? Trace:");
            wtfHappened.printStackTrace();
        }
    }
    public void setUrl(String url) {
        this.databaseUrl = "jdbc:mysql:// " + url + ":3306";
    }
    public String getUrl() {
        return this.databaseUrl;
    }
    public void setUsername(String username) {
        this.databaseUsername = username;
    }
    public String getUsername() {
        return this.databaseUsername;
    }
    public void setPassword(String password) {
        this.databasePassword = password;
    }
    public String getPassword() {
        return this.databasePassword;
    }
}