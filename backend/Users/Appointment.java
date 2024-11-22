package Users;

import java.util.List;
import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;

public class Appointment {
    //private attributes
    @SerializedName("appointmentID")
    private byte[] appointmentID = new byte[32]; //Who the patient this appointment is for
    @SerializedName("staffList")
    private List<byte[]> staffList;
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
    public Appointment(byte[] appointmentID, byte[] patientID, List<byte[]> staffList, int stationNumber, String treatment, String notes, int timestamp, boolean completionStatus, boolean cancelStatus, boolean paid) {
        this.appointmentID = appointmentID; 
		this.patientID = patientID;
		this.staffList = staffList;
		this.stationNumber = stationNumber;
		this.treatment = treatment;
		this.notes = notes;
		this.timestamp = timestamp;
		this.completionStatus = completionStatus;
		this.cancelStatus = cancelStatus;
		this.paid = paid;
    }


    //getter methods (dont need setter since there is no empty constructor)
    public byte[] getTypeID() {
        return typeID;
    }

	public byte[] getpatientID() {
		return patientID;
	}
	
	public ArrayList<byte[]> getStaffList() {
		return staffList;
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