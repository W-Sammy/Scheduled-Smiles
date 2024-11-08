package Server;

import java.sql.*;
import java.util.*;
import java.lang.StringBuilder;

public class DatabaseConnection implements AutoCloseable {
    final static private String hostname = "scheduledsmiles.cdmwceky6go6.us-west-1.rds.amazonaws.com";
    final static private int port = 3306; // 3306 is default
    final static private String databaseName = "scheduledSmiles"; // case sensitive, will refuse connection if the name doesn't match.
    final static private String username = "root";
    final static private String password = "password";
    static private String databaseUrl;
    private static volatile Connection con = null;
    public DatabaseConnection() {
        databaseUrl = String.format("jdbc:mysql://%s:%s/%s", hostname, port, databaseName);
        connect();
    }
    public boolean isConnected() {
        return this.con != null;
    }

    // Helper function, executes a query
    public List<List<byte[]>> queryBytes(String query) {
        final List<List<byte[]>> resultBytes = new ArrayList<>();
        if (this.isConnected()) {
            try (final Statement transaction = this.con.createStatement()) {
                final ResultSet results = transaction.executeQuery(query);
                final ResultSetMetaData resultMetaData = results.getMetaData();
                final int columnCount = resultMetaData.getColumnCount();
                while(results.next()) {
                    resultBytes.add(new ArrayList<byte[]>());
                    int i = 1;
                    while (i <= columnCount) {
                        resultBytes.get(resultBytes.size() - 1).add(results.getBytes(i));
                        i++;
                    }
                }
                return resultBytes;
            } catch (Exception e) {
                System.out.println("Error: Failed to open a database transaction. Trace:");
                e.printStackTrace();
            }
        }
        return null;
    }
    
    public List<List<String>> queryStrings(String query) {
        final List<List<String>> resultBytes = new ArrayList<>();
        if (this.isConnected()) {
            try (final Statement transaction = this.con.createStatement()) {
                final ResultSet results = transaction.executeQuery(query);
                final ResultSetMetaData resultMetaData = results.getMetaData();
                final int columnCount = resultMetaData.getColumnCount();
                while(results.next()) {
                    resultBytes.add(new ArrayList<String>());
                    int i = 1;
                    while (i <= columnCount) {
                        resultBytes.get(resultBytes.size() - 1).add(results.getString(i));
                        i++;
                    }
                }
                return resultBytes;
            } catch (Exception e) {
                System.out.println("Error: Failed to open a database transaction. Trace:");
                e.printStackTrace();
            }
        }
        return null;
    }
    
    // Intended to retrieve sql TINYINT type as java booleans, instead of converting them to bulkier Int type in java
    public List<List<Boolean>> queryBooleans(String query) {
        final List<List<Boolean>> resultBytes = new ArrayList<>();
        if (this.isConnected()) {
            try (final Statement transaction = this.con.createStatement()) {
                final ResultSet results = transaction.executeQuery(query);
                final ResultSetMetaData resultMetaData = results.getMetaData();
                final int columnCount = resultMetaData.getColumnCount();
                while(results.next()) {
                    resultBytes.add(new ArrayList<Boolean>());
                    int i = 1;
                    while (i <= columnCount) {
                        resultBytes.get(resultBytes.size() - 1).add(results.getBoolean(i));
                        i++;
                    }
                }
                return resultBytes;
            } catch (Exception e) {
                System.out.println("Error: Failed to open a database transaction. Trace:");
                e.printStackTrace();
            }
        }
        return null;
    }
    
    public List<List<Integer>> queryIntegers(String query) {
        final List<List<Integer>> resultBytes = new ArrayList<>();
        if (this.isConnected()) {
            try (final Statement transaction = this.con.createStatement()) {
                final ResultSet results = transaction.executeQuery(query);
                final ResultSetMetaData resultMetaData = results.getMetaData();
                final int columnCount = resultMetaData.getColumnCount();
                while(results.next()) {
                    resultBytes.add(new ArrayList<Integer>());
                    int i = 1;
                    while (i <= columnCount) {
                        resultBytes.get(resultBytes.size() - 1).add(results.getInt(i));
                        i++;
                    }
                }
                return resultBytes;
            } catch (Exception e) {
                System.out.println("Error: Failed to open a database transaction. Trace:");
                e.printStackTrace();
            }
        }
        return null;
    }
    
    public String getHexBytes(byte[] bytes) {
        final StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02X", b));
        }
        return hexString.toString();
    }
    
    public void connect() {
        try {
            this.con = DriverManager.getConnection(
                this.databaseUrl,
                this.username,
                this.password
            );
        } catch (Exception e) {
            System.out.println("Error: Failed to connect to database. Trace:");
            e.printStackTrace();
        }
    }
    
    @Override
    public void close() {
        try {
            con.close();
            con = null;
        } catch (Exception e) {
            System.out.println("Error: Failed to close database connection. Is a connection active? Trace:");
            e.printStackTrace();
        }
    }
    public String getUrl() {
        return this.databaseUrl;
    }
    public String getUsername() {
        return this.username;
    }
    public String getPassword() {
        return this.password;
    }
}