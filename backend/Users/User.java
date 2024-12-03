package Users;

import static Users.Enum.RoleConstant.*;

import java.util.ArrayList;
import com.google.gson.annotations.SerializedName;

public class User {
    // attributes
    @SerializedName("userId")
    protected byte[] userID = new byte[32];
    @SerializedName("roleId")
    protected byte[] roleID = new byte[32]; 
    @SerializedName("firstName")
    protected String firstName;
    @SerializedName("lastName")
    protected String lastName;
    @SerializedName("address")
    protected String address;
    @SerializedName("sex")
    protected char sex;
    @SerializedName("phone")
    protected String phoneNumber; // max is 10 characters
    @SerializedName("email")
    protected String email;
    @SerializedName("birthDate")
    protected int birthDate;
    @SerializedName("detail")
    protected String detail;

    // constructor
    public User(byte[] userID, byte[] roleID, String firstName, String lastName, String address, 
                char sex, String phoneNumber, String email, int birthDate, String detail) {
        this.userID = userID;
        this.roleID = roleID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.sex = sex;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.birthDate = birthDate;
        this.detail = detail;
    } // end constructor
    // getter functions
    public byte[] getUserID() {
        return userID;
    } // end getUserID()

    public byte[] getRoleID() {
        return roleID;
    } // end getRoleID()

    public String getFirstName() {
        return firstName;
    } // end getFirstName()

    public String getLastName() {
        return lastName;
    } // end getLastName()

    public String getAddress() {
        return address;
    } // end getAddress()

    public char getSex() {
        return sex;
    } // end getSex()

    public String getPhoneNumber() {
        return phoneNumber;
    } // end getPhoneNumber()

    public String getEmail() {
        return email;
    } // end getEmail()

    public int getBirthDate() {
        return birthDate;
    } // end getBirthDate()

    // setter functions
    public void setUserID(byte[] userID) {
        this.userID = userID;
    } // end setUserID()

    public void setRoleID(byte[] roleID) {
        this.roleID = roleID;
    } // end setRoleID()

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    } // end setFirstName()

    public void setLastName(String lastName) {
        this.lastName = lastName;
    } // end setLastName()

    public void setAddress(String address) {
        this.address = address;
    } // end setAddress()

    public void setSex(char sex) {
        this.sex = sex;
    } // end setSex()

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    } // end setPhoneNumber()

    public void setEmail(String email) {
        this.email = email;
    } // end setEmail()

    public void setBirthDate(int birthDate) {
        this.birthDate = birthDate;
    } // end setBirthDate()
}