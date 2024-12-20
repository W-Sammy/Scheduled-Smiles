package Server.utils;
import java.lang.UnsupportedOperationException;
import com.google.gson.JsonElement;
import java.io.Serializable;
import java.util.HexFormat;

public class DatabaseGenericParameter implements Serializable {
    private String stringValue = "";
    // semantics not clearly defined, sorry to anyone having to work on this -Kyle
    private String stringType;
    public DatabaseGenericParameter() {
        stringValue = null;
        stringType = "null";
    }
    public DatabaseGenericParameter(String value) {
        if (value.equals("null")) {
            stringValue = null;
            stringType = "null";
        } else {
            stringValue = value;
            stringType = "str";
        }
    }
    public DatabaseGenericParameter(String value, String type) {
        stringValue = value;
        stringType = type;
    }
    public DatabaseGenericParameter(int value) {
        stringValue = String.valueOf(value);
        stringType = "int";
    }
    public DatabaseGenericParameter(byte[] value) {
        for (byte i : value) {
            stringValue += String.format("%02X", i);
        }
        stringType = "bytes";
    }
    public DatabaseGenericParameter(boolean value) {
        stringValue = String.valueOf(value);
        stringType = "bool";
    }
    public DatabaseGenericParameter(double value) {
        stringValue = String.valueOf(value);
        stringType = "double";
    }
    public DatabaseGenericParameter(char value) {
        stringValue = String.valueOf(value);
        stringType = "char";
    }
    public String getAsParameter() {
        if(isBoolean()) {
            return String.format("%s", (Boolean.parseBoolean(stringValue)) ? 1 : 0);
        } else if(isBytes()) {
            return String.format("UNHEX(\'%s\')", stringValue);
        } else if(isInteger()) {
            return stringValue;
        } else if (isNull()) {
            return "NULL";
        } else { // must be string, or we don't care.
            return String.format("\'%s\'", stringValue);
        }
    }
    // unsafe as hell
    public DatabaseGenericParameter _cast(final String type) {
        stringType = type;
        return this; // return self for chaining -Kyle
    }
    // these might not be needed
    public String equalsTo(final String value) {
        return String.format("%s = %s", value, getAsParameter());
    }
    public String lessThan(final String value) {
        return String.format("%s > %s", value, getAsParameter());
    }
    public String greaterThan(final String value) {
        return String.format("%s < %s", value, getAsParameter());
    }
    public String lessEqualThan(final String value) {
        return String.format("%s >= %s", value, getAsParameter());
    }
    public String greaterEqualThan(final String value) {
        return String.format("%s <= %s", value, getAsParameter());
    }
    // need these
    public boolean isBoolean() {
        return stringType.equals("bool");
    }
    public boolean isString() {
        return stringType.equals("str");
    }
    public boolean isBytes() {
        return stringType.equals("bytes");
    }
    public boolean isInteger() {
        return stringType.equals("int");
    }
    public boolean isDouble() {
        return stringType.equals("double");
    }
    public boolean isChar() {
        return stringType.equals("char");
    }
    public boolean isNull() {
        return stringType.equals("null");
    }
    public String getType() {
        return stringType;
    }
    // Bad to intentionall throw errors, but we CANNOT be returning mismatched types. -Kyle
    public boolean getAsBoolean() throws UnsupportedOperationException {
        if (this.isBoolean()) {
            return Boolean.parseBoolean(stringValue);
        } else if (this.isNull()) {
            return false; // default value to stop things from breaking
        }
        throw new UnsupportedOperationException(String.format("Value is not of type boolean (%s, %s)", getType(), toString()));
    }
    public String getAsString() throws UnsupportedOperationException {
        if (this.isString()) {
            return stringValue;
        } else if (this.isNull()) {
            return null;
        }
        throw new UnsupportedOperationException(String.format("Value is not of type string (%s, %s)", getType(), toString()));
    }
    public byte[] getAsBytes() throws UnsupportedOperationException {
        if (this.isBytes()) {
            return HexFormat.of().parseHex(stringValue);
        } else if (this.isNull()) {
            return new byte[32]; // default value to stop things from breaking
        }
        throw new UnsupportedOperationException(String.format("Value is not of type byte[] (%s, %s)", getType(), toString()));
    }
    public int getAsInteger() throws UnsupportedOperationException {
        if (this.isInteger()) {
            return Integer.parseInt(stringValue);
        } else if (this.isNull()) {
            return -1; // default value to stop things from breaking
        }
        throw new UnsupportedOperationException(String.format("Value is not of type int (%s, %s)", getType(), toString()));
    }
    public double getAsDouble() throws UnsupportedOperationException {
        if (this.isDouble()) {
            return Double.parseDouble(stringValue);
        }
        throw new UnsupportedOperationException(String.format("Value is not of type double (%s, %s)", getType(), toString()));
    }
    public char getAsChar() throws UnsupportedOperationException {
        if (this.isChar()) {
            return stringValue.charAt(0);
        }
        throw new UnsupportedOperationException(String.format("Value is not of type char (%s, %s)", getType(), toString()));
    }
    @Override
    public String toString() {
        return stringValue;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        final DatabaseGenericParameter other = (DatabaseGenericParameter) obj;
        if ((this.stringValue == null) ? (other.stringValue != null) : !stringValue.equals(other.stringValue)) {
            return false;
        }
        if (this.stringType != other.stringType) {
            return false;
        }
        return true;
    }
}