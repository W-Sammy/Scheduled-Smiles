public class User {
    // attributes
    private byte[] userID = new byte[32];
    private byte[] roleID;
    private String firstName;
    private String lastName;
    private String address;
    private char sex;
    private String phoneNumber; // max is 10 characters
    private String email;
    private int birthDate;

    // constructor
    public User(byte[] userID, byte[] roleID, String firstName, String lastName, String address, 
                char sex, String phoneNumber, String email, int birthDate) {
        this.userID = userID;
        this.roleID = roleID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.sex = sex;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.birthDate = birthDate;
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

class NewUser extends User {
    // attributes
    private byte[] userID;
    private byte[] roleID = new byte[32]; // set as default
    private String password;

    // constructor 
    public NewUser(byte[] userID, byte[] roleID, String firstName, String lastName, String address, 
                    char sex, String phoneNumber, String email, int birthDate, String password) {
        super(userID, roleID, firstName, lastName, 
                address, sex, phoneNumber, email, birthDate);
        this.password = password;
    } // end constructor

    // gets the password for the variable password
    public String getPassword() {
        return password;
    } // end getPassword()

    // sets the password for the variable password
    public void setPassword(String password) {
        this.password = password;
    } // end setPassword()
}

class Patient extends User {
    // Attributes
    private final byte[] roleID = new byte[32];
    // private ArrayList<Appointment> upcomingAppointments; // List of upcoming appointments
    
    // Constructor
    public Patient(byte[] userID, byte[] roleID, String firstName, String lastName, String address,
                  char sex, String phoneNumber, String email, int birthDate) {
      super(userID, roleID, firstName, lastName, address, sex, phoneNumber, email, birthDate);
    }

    /*
    // Gets upcoming appointments
    public ArrayList<Appointments> getUpcomingAppointments(){
       return upcomingAppointments;
    }
    
    // Adds an appointment to upcomingAppointments
    public void addAppointment(Appointment appointment){
       this.upcomingAppointments.add(appointment);
    }
    
    // Removes appointment from upcomingAppointments
    public void removeAppointment(Appointment appointment){
       this.upcomingAppointment.remove(appointment);
    }
    */
 }

 class Staff extends User {
    // Private Attributes
    private final byte[] roleID = new byte[32];
    private double hourlyRate;
    
    // Contructor
    public Staff(byte[] userID, byte[] roleID, String firstName, String lastName, String address,
                 char sex, String phoneNumber, String email, int birthDate, double hourlyRate){
      super(userID, roleID, firstName, lastName, address, sex, phoneNumber, email, birthDate);         
      this.hourlyRate = hourlyRate;
    }
    
    // Getter for hourlyRate
    public double getHourlyRate(){
      return hourlyRate;
    }
    
    // Setter for hourlyRate
    public void setHourlyRate(double hourlyRate){
      this.hourlyRate = hourlyRate;
    }
}

class Admin extends Staff {
    // attributes
    private final byte[] roleID = new byte[32];

    // constructor
    public Admin(byte[] userID, byte[] roleID, String firstName, String lastName, String address,
                    char sex, String phoneNumber, String email, int birthDate, double hourlyRate) {
        super(userID, roleID, firstName, lastName, address, sex, phoneNumber, email, birthDate, hourlyRate);
    } // end constructor
}
