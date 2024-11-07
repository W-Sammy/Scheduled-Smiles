import java.util.*;

public class Patient extends User {
    // Attributes
    private int roleID = 0;
    // private ArrayList<Appointment> upcomingAppointments; // List of upcoming
    // appointments

    // Constructor
    public Patient(byte userID, int roleID, String firstName, String lastName, String address,
            char sex, int phoneNumber, String email, int birthDate) {
        super(userID, roleID, firstName, lastName, address, sex, phoneNumber, email, birthDate);
        // this.upcomingAppointments = new ArrayList<>(); // Initializes appointment
        // list
    }

    // Getter for roleID
    public int getRoleID() {
        return roleID;
    }
    /*
     * // Gets upcoming appointments
     * public ArrayList<Appointments> getUpcomingAppointments(){
     * return upcomingAppointments;
     * }
     * 
     * // Adds an appointment to upcomingAppointments
     * public void addAppointment(Appointment appointment){
     * this.upcomingAppointments.add(appointment);
     * }
     * 
     * // Removes appointment from upcomingAppointments
     * public void removeAppointment(Appointment appointment){
     * this.upcomingAppointment.remove(appointment);
     * }
     */
}
