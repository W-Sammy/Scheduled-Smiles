package Users;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Appointment {
    //private attributes
    @SerializedName("appointmentID")
    private byte[] appointmentID = new byte[32];
    @SerializedName("patientID")
    private byte[] patientID = new byte[32];
    @SerializedName("staff1ID")
    private byte[] staff1ID = new byte[32];
    @SerializedName("staff2ID")
    private byte[] staff2ID = new byte[32];
    @SerializedName("staff3ID")
    private byte[] staff3ID = new byte[32];
    @SerializedName("stationNumber")
    private int stationNumber;	//Which station the appointment took place in; reflects an input accepted from frontend
    @SerializedName("treatment")
    private String treatment;	//Which treatment was given to the patient, finalizing as singular treatment option only, also used to calculate cost of treatment for billing
    @SerializedName("notes")
    private String notes;	//Singular input box for notes about the patient written by staff; retrieved from frontend 
    @SerializedName("startTime")
    private int timestamp; //Scheduled start time of the appointment (UTC)
    @SerializedName("isComplete")
    private boolean completionStatus; //Check if appointment has been completed
    @SerializedName("isCanceled")
    private boolean cancelStatus; //Check if appointment has been cancelled
    @SerializedName("isPaid")
    private boolean paid; //Check if this appointment has been paid by patient (for payment page)
    
    // constructor
    public Appointment(byte[] appointmentID, byte[] patientID, byte[] staff1ID, byte[] staff2ID, byte[] staff3ID, int stationNumber, String treatment, String notes, int timestamp, boolean completionStatus, boolean cancelStatus, boolean paid) {
        this.appointmentID = appointmentID; 
		this.patientID = patientID;
		this.staff1ID = staff1ID;
        this.staff2ID = staff2ID;
        this.staff3ID = staff3ID;
		this.stationNumber = stationNumber;
		this.treatment = treatment;
		this.notes = notes;
		this.timestamp = timestamp;
		this.completionStatus = completionStatus;
		this.cancelStatus = cancelStatus;
		this.paid = paid;
    }


    //getter methods (dont need setter since there is no empty constructor)
	public byte[] getpatientID() {
		return patientID;
	}
	
	public List<byte[]> getStaffList() {
		return List.of(staff1ID, staff2ID, staff3ID);
	}

	public int getStationNumber() {
		return stationNumber;
	}

	public String getTreatment() {
		return treatment;
	}

	public String getNotes() {
		return notes;
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
	
	public boolean getPaid()
	{
		return paid;
	}
}