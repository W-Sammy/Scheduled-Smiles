package Server.utils;
import java.lang.UnsupportedOperationException;
import java.io.Serializable;
import java.util.HexFormat;

public class DatabaseGenericParameter implements Serializable {
    private String stringValue = "";
    // semantics not clearly defined, sorry to anyone having to work on this -Kyle
    private String stringType;
    public DatabaseGenericParameter() {
        stringType = "null";
    }
    public DatabaseGenericParameter(String value) {
        stringValue = value;
        stringType = "str";
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
            return String.format("TINYINT(%s)", (Boolean.parseBoolean(stringValue)) ? 1 : 0);
        } else if(isBytes()) {
            return String.format("UNHEX(\'%s\')", stringValue);
        } else if(isInteger()) {
            return stringValue;
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
        }
        throw new UnsupportedOperationException("Value is not of type boolean");
    }
    public String getAsString() throws UnsupportedOperationException {
        if (this.isString()) {
            return stringValue;
        }
        throw new UnsupportedOperationException("Value is not of type int");
    }
    public byte[] getAsBytes() throws UnsupportedOperationException {
        if (this.isBytes()) {
            return HexFormat.of().parseHex(stringValue);
        }
        throw new UnsupportedOperationException("Value is not of type byte[]");
    }
    public int getAsInteger() throws UnsupportedOperationException {
        if (this.isInteger()) {
            return Integer.parseInt(stringValue);
        }
        throw new UnsupportedOperationException("Value is not of type int");
    }
    public double getAsDouble() throws UnsupportedOperationException {
        if (this.isDouble()) {
            return Double.parseDouble(stringValue);
        }
        throw new UnsupportedOperationException("Value is not of type double");
    }
    public char getAsChar() throws UnsupportedOperationException {
        if (this.isChar()) {
            return stringValue.charAt(0);
        }
        throw new UnsupportedOperationException("Value is not of type char");
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