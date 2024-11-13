import java.util.ArrayList;
import java.util.Arrays;
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

	public ArrayList<String> getHealthCondition() {
		return healthCondition;
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

    //setter methods
    public void setTypeID(byte[] typeID) {
        this.typeID = typeID;
    }

	public void setPatientID(byte[] patientID) {
		this.patientID = patientID;
	}
	
	public void setStaffList(ArrayList<byte[]> staffList) {
		this.staffList = staffList;
	}

	public void setStationNumber(int stationNumber) {
		this.stationNumber = stationNumber;
	}

	public void setTreatment(String treatment) {
		this.treatment = treatment;
	}

	public void setHealthCondition(ArrayList<String> healthCondition) {
		this.healthCondition = healthCondition;
	}

	public void setNotes(String notes) {
		this.notes = notes;
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
	// Iterator equivalent
	public String whatsInside(ArrayList<byte[]> identifier) {
		String s = "";
		for(byte[] b : identifier) {
			s = s + Arrays.toString(b) + ", ";
		}
		return s;
	}
	// ToString equivalent
    public String viewAppointment()
    {
        return "\nAppointmentID: " + Arrays.toString(typeID) +
		"\n\nPatientID: " + Arrays.toString(patientID) +
		"\n\nRelated Staff: " + whatsInside(staffList) +
		"\n\nStation Number: " + stationNumber +
		"\n\nTreatment Type: " + treatment +
		"\n\nHealth Condition(s): " + healthCondition.toString() +
		"\n\nNotes: " + notes +
		"\n\nTimestamp: " + timestamp +
		"\n\nCompleted: " + completionStatus +
		"\n\nCanceled: " + cancelStatus;

    }
    /*FOR TESTING PURPOSES ONLY
	
	public static void main(String[] args) {
		byte[] typeID = new byte[]{1, 2, 3};
		byte[] patientID = new byte[]{3, 2, 1};
		ArrayList<byte[]> staffList = new ArrayList<>();
		byte[] staffOne = new byte[]{12, 13, 14};
		byte[] staffTwo = new byte[]{15, 16, 17};
		staffList.add(staffOne);
		staffList.add(staffTwo);
		int stationNumber = 2;
		String treatment = "Check-Up";
		ArrayList<String> healthCondition = new ArrayList<>();
		healthCondition.add("Underbite");
		healthCondition.add("Gum Disease");
		String notes = "Patient scheduled for fillers";
		int timestamp = 102931;
		boolean completionStatus = false;
		boolean cancelStatus = false;
		Appointment testCase = new Appointment(typeID, patientID, staffList, stationNumber, treatment, healthCondition, notes, timestamp, completionStatus, cancelStatus);
		System.out.println(testCase.viewAppointment());
		

	}
	*/
}