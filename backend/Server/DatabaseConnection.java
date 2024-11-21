package Server;

import java.sql.*;
import java.util.*;
import java.lang.StringBuilder;
import java.lang.IllegalArgumentException;

import Server.utils.DatabaseGenericParameter;   

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
    private byte toByte(boolean value) {
        return (byte) (value ? 1 : 0 );
    }
    public boolean isConnected() {
        return this.con != null;
    }
    public int updateToTable(final String tablename, final String[] columnValues, final String[] selectors) {
        int resultRows = 0;
        if (this.isConnected()) {
            final String colString = String.join(", ", columnValues);
            final String valString = String.join(", ", selectors);
            final String queryString = String.format("UPDATE %s SET %s WHERE %s", tablename, colString, valString);
            try (final PreparedStatement ps = con.prepareStatement(queryString)) {
                resultRows = ps.executeUpdate();
            } catch (SQLTimeoutException e) {
                System.out.println("DatabaseConnection Error: Failed to connect to database, connection timed out.");
            } catch (SQLDataException e) { // maybe the error message should be made shorter... -Kyle
                System.out.println("DatabaseConnection Error: Update failed- does the table exist? Were the correct columns specified? Did the values have correct types? Detail:\n" + e.getMessage());
            } catch (Exception e) {
                System.out.println("DatabaseConnection Error: Failed to execute database transaction. Trace:");
                e.printStackTrace();
            }
        }
        return resultRows;
    }
    public int insertToTable(final String tablename, final String[] columns, final String[] values) {
        int resultRows = 0;
        if (columns.length != values.length) {
            throw new IllegalArgumentException("Every column must have a value to insert!");
        }
        if (this.isConnected()) {
            final String colString = String.join(", ", columns);
            final String valString = String.join(", ", values);
            final String queryString = String.format("INSERT INTO %s (%s) VALUES (%s)", tablename, colString, valString);
            try (final Statement transaction = this.con.createStatement()) {
                resultRows = transaction.executeUpdate(queryString);    
            } catch (SQLTimeoutException e) {
                System.out.println("DatabaseConnection Error: Failed to connect to database, connection timed out.");
            } catch (SQLDataException e) { // maybe the error message should be made shorter... -Kyle
                System.out.println("DatabaseConnection Error: Update failed- does the table exist? Were the correct columns specified? Did the values have correct types? Detail:\n" + e.getMessage());
            } catch (Exception e) {
                System.out.println("DatabaseConnection Error: Failed to execute database transaction. Trace:");
                e.printStackTrace();    
            }
        }
        return resultRows;
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
                System.out.println("DatabaseConnection Error: Failed to execute database update. Trace:");
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
                            case Types.TINYINT: // Retrieve sql TINYINT as java boolean, instead of converting them to bulkier int in java
                                resultArray.get(resultArray.size() - 1).add(new DatabaseGenericParameter(results.getBoolean(i)));
                            break;
                            case Types.CHAR:
                            case Types.VARCHAR:
                            case Types.LONGVARCHAR:
                                resultArray.get(resultArray.size() - 1).add(new DatabaseGenericParameter(results.getString(i)));
                            break;
                            case Types.BIGINT:
                            case Types.INTEGER:
                                resultArray.get(resultArray.size() - 1).add(new DatabaseGenericParameter(results.getInt(i)));
                            break;
                            case Types.BINARY:
                            default: // when in doubt get as bytes
                                resultArray.get(resultArray.size() - 1).add(new DatabaseGenericParameter(results.getBytes(i)));
                        }
                        i++;
                    }
                }
                return resultArray;
            } catch (SQLDataException e) {
                System.out.println("DatabaseConnection Error: Bad query. Detail:\n" + e.getMessage());
            } catch (Exception e) {
                System.out.println("DatabaseConnection Error: Failed to execute database update. Trace:");
                e.printStackTrace();
            }
        }
        return null;
    }
    /*
    // null values
    public int insertToTable(final String tablename, final String[] columns) {
        int resultRows = 0;
        if (this.isConnected()) {
            final String colString = String.join(", ", columns);
            final String valString = String.join(", ", "?".repeat(columns.length));
            final String queryString = String.format("INSERT INTO %s (%s) VALUES (%s)", tablename, colString, valString);
            try (final PreparedStatement ps = con.prepareStatement(queryString)) {
                int i = 1;
                while (i <= columns.length) {
                    ps.setNull(i++, Types.NULL);
                }
                resultRows = ps.executeUpdate();
            } catch (SQLTimeoutException e) {
                System.out.println("DatabaseConnection Error: Failed to connect to database, connection timed out.");
            } catch (SQLDataException e) { // maybe the error message should be made shorter... -Kyle
                System.out.println("DatabaseConnection Error: Update failed- does the table exist? Were the correct columns specified? Did the values have correct types? Detail:\n" + e.getMessage());
            } catch (Exception e) {
                System.out.println("DatabaseConnection Error: Failed to execute database transaction. Trace:");
                e.printStackTrace();
            }
        }
        return resultRows;
    }
    public int insertToTable(final String tablename, final String[] columns, final byte[][] values) {
        int resultRows = 0;
        if (columns.length != values.length) {
            throw new IllegalArgumentException("Every column must have a value to insert!");
        }
        if (this.isConnected()) {
            final String colString = String.join(", ", columns);
            final String valString = String.join(", ", "?".repeat(values.length));
            final String queryString = String.format("INSERT INTO %s (%s) VALUES (%s)", tablename, colString, valString);
            try (final PreparedStatement ps = con.prepareStatement(queryString)) {
                int i = 1;
                while (i <= values.length) {
                    ps.setBytes(i++, values[i-1]);
                }
                resultRows = ps.executeUpdate();
            } catch (SQLTimeoutException e) {
                System.out.println("DatabaseConnection Error: Failed to connect to database, connection timed out.");
            } catch (SQLDataException e) { // maybe the error message should be made shorter... -Kyle
                System.out.println("DatabaseConnection Error: Update failed- does the table exist? Were the correct columns specified? Did the values have correct types? Detail:\n" + e.getMessage());
            } catch (Exception e) {
                System.out.println("DatabaseConnection Error: Failed to execute database transaction. Trace:");
                e.printStackTrace();
            }
        }
        return resultRows;
    }
    public int insertToTable(final String tablename, final String[] columns, final boolean[] values) {
        int resultRows = 0;
        if (columns.length != values.length) {
            throw new IllegalArgumentException("Every column must have a value to insert!");
        }
        if (this.isConnected()) {
            final String colString = String.join(", ", columns);
            final String valString = String.join(", ", "?".repeat(values.length));
            final String queryString = String.format("INSERT INTO %s (%s) VALUES (%s)", tablename, colString, valString);
            try (final PreparedStatement ps = con.prepareStatement(queryString)) {
                int i = 1;
                while (i <= values.length) {
                    ps.setByte(i++, toByte(values[i-1]));
                }
                resultRows = ps.executeUpdate();
            } catch (SQLTimeoutException e) {
                System.out.println("DatabaseConnection Error: Failed to connect to database, connection timed out.");
            } catch (SQLDataException e) { // maybe the error message should be made shorter... -Kyle
                System.out.println("DatabaseConnection Error: Update failed- does the table exist? Were the correct columns specified? Did the values have correct types? Detail:\n" + e.getMessage());
            } catch (Exception e) {
                System.out.println("DatabaseConnection Error: Failed to execute database transaction. Trace:");
                e.printStackTrace();
            }
        }
        return resultRows;
    }
    public int insertToTable(final String tablename, final String[] columns, final int[] values) {
        int resultRows = 0;
        if (columns.length != values.length) {
            throw new IllegalArgumentException("Every column must have a value to insert!");
        }
        if (this.isConnected()) {
            final String colString = String.join(", ", columns);
            final String valString = String.join(", ", "?".repeat(values.length));
            final String queryString = String.format("INSERT INTO %s (%s) VALUES (%s)", tablename, colString, valString);
            try (final PreparedStatement ps = con.prepareStatement(queryString)) {
                int i = 1;
                while (i <= values.length) {
                    ps.setInt(i++, values[i-1]);
                }
                resultRows = ps.executeUpdate();
            } catch (SQLTimeoutException e) {
                System.out.println("DatabaseConnection Error: Failed to connect to database, connection timed out.");
            } catch (SQLDataException e) { // maybe the error message should be made shorter... -Kyle
                System.out.println("DatabaseConnection Error: Update failed- does the table exist? Were the correct columns specified? Did the values have correct types? Detail:\n" + e.getMessage());
            } catch (Exception e) {
                System.out.println("DatabaseConnection Error: Failed to execute database transaction. Trace:");
                e.printStackTrace();
            }
        }
        return resultRows;
    }
    public int insertToTable(final String tablename, final String[] columns, final String[] values) {
        int resultRows = 0;
        if (columns.length != values.length) {
            throw new IllegalArgumentException("Every column must have a value to insert!");
        }
        if (this.isConnected()) {
            final String colString = String.join(", ", columns);
            final String valString = String.join(", ", "?".repeat(values.length));
            final String queryString = String.format("INSERT INTO %s (%s) VALUES (%s)", tablename, colString, valString);
            try (final PreparedStatement ps = con.prepareStatement(queryString)) {
                int i = 1;
                while (i <= values.length) {
                    ps.setString(i++, values[i-1]);
                }
                resultRows = ps.executeUpdate();
            } catch (SQLTimeoutException e) {
                System.out.println("DatabaseConnection Error: Failed to connect to database, connection timed out.");
            } catch (SQLDataException e) { // maybe the error message should be made shorter... -Kyle
                System.out.println("DatabaseConnection Error: Update failed- does the table exist? Were the correct columns specified? Did the values have correct types? Detail:\n" + e.getMessage());
            } catch (Exception e) {
                System.out.println("DatabaseConnection Error: Failed to execute database transaction. Trace:");
                e.printStackTrace();
            }
        }
        return resultRows;
    }
    public int insertToTable(final String tablename, final String[] columns, final char[] values) {
        int resultRows = 0;
        if (columns.length != values.length) {
            throw new IllegalArgumentException("Every column must have a value to insert!");
        }
        if (this.isConnected()) {
            final String colString = String.join(", ", columns);
            final String valString = String.join(", ", "?".repeat(values.length));
            final String queryString = String.format("INSERT INTO %s (%s) VALUES (%s)", tablename, colString, valString);
            try (final PreparedStatement ps = con.prepareStatement(queryString)) {
                int i = 1;
                while (i <= values.length) {
                    ps.setString(i++, String.valueOf(values[i-1])); // SQLDriver converts values to LONGVARCHAR(string) or VARCHAR(char) accordingly for us. Nice! -Kyle
                }
                resultRows = ps.executeUpdate();
            } catch (SQLTimeoutException e) {
                System.out.println("DatabaseConnection Error: Failed to connect to database, connection timed out.");
            } catch (SQLDataException e) { // maybe the error message should be made shorter... -Kyle
                System.out.println("DatabaseConnection Error: Update failed- does the table exist? Were the correct columns specified? Did the values have correct types? Detail:\n" + e.getMessage());
            } catch (Exception e) {
                System.out.println("DatabaseConnection Error: Failed to execute database transaction. Trace:");
                e.printStackTrace();
            }
        }
        return resultRows;
    } */
    /*
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
            } catch (SQLDataException e) {
                System.out.println("DatabaseConnection Error: Wrong value type requested. Detail:\n" + e.getMessage());
            } catch (Exception e) {
                System.out.println("DatabaseConnection Error: Failed to execute database update. Trace:");
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
            } catch (SQLDataException e) {
                System.out.println("DatabaseConnection Error: Wrong value type requested. Detail:\n" + e.getMessage());
            } catch (Exception e) {
                System.out.println("DatabaseConnection Error: Failed to execute database transaction. Trace:");
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
            } catch (SQLDataException e) {
                System.out.println("DatabaseConnection Error: Wrong value type requested. Detail:\n" + e.getMessage());
            } catch (Exception e) {
                System.out.println("DatabaseConnection Error: Failed to execute database transaction. Trace:");
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
            } catch (SQLDataException e) {
                System.out.println("DatabaseConnection Error: Wrong value type requested. Detail:\n" + e.getMessage());
            } catch (Exception e) {
                System.out.println("DatabaseConnection Error: Failed to execute database transaction. Trace:");
                e.printStackTrace();
            }
        }
        return null;
    }
    
    public List<List<String>> queryHexStrings(String query) {
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
                        resultBytes.get(resultBytes.size() - 1).add(getHexBytes(results.getBytes(i)));
                        i++;
                    }
                }
                return resultBytes;
            } catch (SQLDataException e) {
                System.out.println("DatabaseConnection Error: Wrong value type requested. Detail:\n" + e.getMessage());
            } catch (Exception e) {
                System.out.println("DatabaseConnection Error: Failed to execute database transaction. Trace:");
                e.printStackTrace();
            }
        }
        return null;
    }
    
    public String getHexBytes(byte[] bytes) {
        final StringBuilder hexString = new StringBuilder(); // technically can just use a normal string, but since we're import stringbuilder in the other classes anyways, it is faster at runtime. -Kyle
        for (byte b : bytes) {
            hexString.append(String.format("%02X", b));
        }
        return hexString.toString();
    }
    */
    
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
            con.close();
            con = null;
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