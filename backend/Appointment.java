import java.util.ArrayList;
public class Appointment{
    //private attributes
    private byte[] typeID = new byte[32];	//ID assigned to each appointment
    private byte[] patientID = new byte[32];	//Who the patient this appointment is for
    private ArrayList<byte[]> staffList = new ArrayList<>();	//list of staff who worked on this appointment, dynamic storage should not cause errors unless information is somehow manipulated in backend
    private int stationNumber;	//Which station the appointment took place in; reflects an input accepted from frontend
    private String treatment;	//Which treatment was given to the patient, finalizing as singular treatment option only, also used to calculate cost of treatment for billing
    private ArrayList<String> healthCondition = new ArrayList<>();	//list of possible health conditions retreived from frontend; can have mulitple or none
    private String notes;	//Singular input box for notes about the patient written by staff; retrieved from frontend 
    private int timestamp;	//Scheduled start time of the appointment
    private boolean completionStatus;	//Check if appointment has been completed
    private boolean cancelStatus;	//Check if appointment has been cancelled

    //constructor
    public Appointment(byte[] typeID, byte[] patientID, ArrayList<byte[]> staffList, int stationNumber, String treatment,
						ArrayList<String> healthCondition, String notes, int timestamp, boolean completionStatus, boolean cancelStatus) {
        this.typeID = typeID;
		this.patientID = patientID;
		this.staffList = staffList;
		this.stationNumber = stationNumber;
		this.treatment = treatment;
		this.healthCondition = healthCondition;
		this.notes = notes;
		this.timestamp = timestamp;
		this.completionStatus = completionStatus;
		this.cancelStatus = cancelStatus;
    }


    //getter methods
    public byte[] getTypeID() {
        return typeID;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public boolean getCompletionStatus() {
        return completionStatus; 
    }

    public boolean getCancelStatus() {
        return cancelStatus;
    }

    public double getCost(){
        return cost;
    }    

    //setter methods
    public void setTypeID(byte[] typeID) {
        this.typeID = typeID;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public void setCompletionStatus(boolean completionStatus) {
        this.completionStatus = completionStatus;
    }

    public void setCancelStatus(boolean cancelStatus) {
        this.cancelStatus = cancelStatus;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    /* needs to be connected to the database
    public void scheduleAppointment()
    {
        
    }

    public void cancelAppointment()
    {
        
    }

    public void rescheduleAppointment()
    {
        
    }

    public void viewAppointment()
    {
        
    }
    */
}