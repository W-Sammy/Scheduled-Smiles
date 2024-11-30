package Server;

import Server.utils.DatabaseGenericParameter;
import java.sql.*;
import java.util.*;

public class DatabaseConnection implements AutoCloseable {
    final private String hostname = "scheduledsmiles.cdmwceky6go6.us-west-1.rds.amazonaws.com";
    final private int port = 3306; // 3306 is default
    final private String databaseName = "scheduledSmiles"; // case sensitive, will refuse connection if the name doesn't match.
    final private String username = "root";
    final private String password = "password";
    private String databaseUrl;
    private volatile Connection con = null;
    public DatabaseConnection() {
        databaseUrl = String.format("jdbc:mysql://%s:%s/%s", hostname, port, databaseName);
        connect();
    }
    private byte toByte(boolean value) {
        return (byte) (value ? 1 : 0 );
    }
    public boolean isConnected() {
        return this.con != null;
    }
    // also does inserts
    public int update(String query) {
        int result = 0;
        if (this.isConnected()) {
            try (final Statement transaction = this.con.createStatement()) {
                result = transaction.executeUpdate(query);
            } catch (SQLDataException e) {
                System.out.println("DatabaseConnection Error: Bad query. Detail:\n" + e.getMessage());
            } catch (Exception e) {
                System.out.println(String.format("DatabaseConnection Error: Failed to execute database update for query, \"%s\". Trace:", query));
                e.printStackTrace();
            }
        }
        return result;
    }
    public List<List<DatabaseGenericParameter>> query(String query) {
        final List<List<DatabaseGenericParameter>> resultArray = new ArrayList<>();
        if (this.isConnected()) {
            try (final Statement transaction = this.con.createStatement()) {
                final ResultSet results = transaction.executeQuery(query);
                final ResultSetMetaData resultMetaData = results.getMetaData();
                final int columnCount = resultMetaData.getColumnCount();
                while(results.next()) {
                    resultArray.add(new ArrayList<DatabaseGenericParameter>());
                    int i = 1;
                    while (i <= columnCount) {
                        int columnType = results.getMetaData().getColumnType(i); // inefficent to get column type for every row after the first one but I don't see a better way -Kyle
                        // Might need to stick this into it's own function -Kyle
                        switch (columnType) {
                            case Types.NULL:
                                resultArray.get(resultArray.size() - 1).add(new DatabaseGenericParameter()); // empty constructor defaults the type to null
                            break;
                            case Types.TINYINT: // Retrieve sql TINYINT as java boolean, instead of converting them to bulkier int in java
                                resultArray.get(resultArray.size() - 1).add(new DatabaseGenericParameter(results.getBoolean(i)));
                            break;
                            case Types.CHAR:
                                resultArray.get(resultArray.size() - 1).add(new DatabaseGenericParameter(results.getString(i).charAt(0)));
                            break;
                            case Types.VARCHAR:
                            case Types.LONGVARCHAR:
                                resultArray.get(resultArray.size() - 1).add(new DatabaseGenericParameter(results.getString(i)));
                            break;
                            case Types.DECIMAL:
                                resultArray.get(resultArray.size() - 1).add(new DatabaseGenericParameter(results.getDouble(i)));
                            break;
                            case Types.BIGINT:
                            case Types.INTEGER:
                                resultArray.get(resultArray.size() - 1).add(new DatabaseGenericParameter(results.getInt(i)));
                            break;
                            case Types.BINARY:
                            default:
                                final byte[] unknown = results.getBytes(i);
                                //System.out.println(results.wasNull());
                                // scuffed way to check for null, since not properly implemented by JDBC for some fucking reason -Kyle
                                if (results.wasNull()) {
                                    resultArray.get(resultArray.size() - 1).add(new DatabaseGenericParameter());
                                } else {
                                    // when in doubt get as bytes
                                    resultArray.get(resultArray.size() - 1).add(new DatabaseGenericParameter(unknown));
                                }
                        }
                        i++;
                    }
                }
                return resultArray;
            } catch (SQLDataException e) {
                System.out.println("DatabaseConnection Error: Bad query. Detail:\n" + e.getMessage());
            } catch (Exception e) {
                System.out.println(String.format("DatabaseConnection Error: Failed to execute database update for query, \"%s\". Trace:", query));
                e.printStackTrace();
            }
        } else {
            System.out.println("DatabaseConnection Error: Failed to start query operation, database is not connected!");
        }
        return null;
    }
    public void connect() {
        try {
            this.con = DriverManager.getConnection(
                this.databaseUrl,
                this.username,
                this.password
            );
        } catch (SQLTimeoutException e) {
            System.out.println("DatabaseConnection Error: Failed to connect to database, connection timed out.");
        } catch (SQLException e) {
            System.out.println("DatabaseConnection Error: Failed to connect to database, invalid url or login credentials.");
        }
    }
    @Override
    public void close() {
        try {
            if (con != null) {
                con.close();
                con = null;
            }
        } catch (Exception e) {
            System.out.println("DatabaseConnection Error: Failed to close database connection. Is a connection active? Trace:");
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