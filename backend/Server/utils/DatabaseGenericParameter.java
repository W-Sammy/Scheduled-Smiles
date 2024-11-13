package Server.utils;
import java.io.Serializable;

public class DatabaseGenericParameter implements Serializable {
    private String stringValue = "";
    // semantics not clearly defined, sorry to anyone having to work on this -Kyle
    private String stringType;
    public DatabaseGenericParameter() { 
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
    public String getAsParameter() {
        if(isBoolean()) {
            return String.format("TINYINT(%s)", (Boolean.parseBoolean(stringValue)) ? 1 : 0);
        } else if(isBytes()) {
            return String.format("UNHEX(%s)", stringValue);
        } else if(isInteger()) {
            return stringType;
        } else { // must be string, or we don't care.
            return String.format("\'%s\'", stringValue);
        }
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
    public String getType() {
        return stringType;
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