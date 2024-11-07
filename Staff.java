public class Staff extends User {
    // Private Attributes
    private int roleID = 1;
    private double hourlyRate;

    // Contructor
    public Staff(byte userID, int roleID, String firstName, String lastName, String address,
            char sex, int phoneNumber, String email, int birthDate, double hourlyRate) {
        super(userID, roleID, firstName, lastName, address, sex, phoneNumber, email, birthDate);
        this.hourlyRate = hourlyRate;
    }

    // Getter for roleID
    public int getRoleID() {
        return roleID;
    }

    // Getter for hourlyRate
    public double getHourlyRate() {
        return hourlyRate;
    }

    // Setter for hourlyRate
    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

}