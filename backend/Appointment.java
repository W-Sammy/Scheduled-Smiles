import java.util.ArrayList;
import java.util.Arrays;
public class Appointment{
    //private attributes
    private byte[] typeID = new byte[32];	//ID assigned to each appointment
    private byte[] patientID = new byte[32];	//Who the patient this appointment is for
    private ArrayList<byte[]> staffList = new ArrayList<>();	//list of staff who worked on this appointment, dynamic storage should not cause errors unless information is somehow manipulated in backend
    private int stationNumber;	//Which station the appointment took place in; reflects an input accepted from frontend
    private String treatment;	//Which treatment was given to the patient, finalizing as singular treatment option only, also used to calculate cost of treatment for billing
    private String notes;	//Singular input box for notes about the patient written by staff; retrieved from frontend 
    private int timestamp;	//Scheduled start time of the appointment
    private boolean completionStatus;	//Check if appointment has been completed
    private boolean cancelStatus;	//Check if appointment has been cancelled
	private boolean paid; //Check if this appointment has been paid by patient (for payment page)

    //constructor
    public Appointment(byte[] typeID, byte[] patientID, ArrayList<byte[]> staffList, int stationNumber, String treatment,
						String notes, int timestamp, boolean completionStatus, boolean cancelStatus, boolean paid) {
        this.typeID = typeID; 
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


    //getter methods
    public byte[] getTypeID() {
        return typeID;
    }

	public byte[] getPatientID() {
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

	public void setPaid(boolean paid) {
		this.paid = paid;
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
		"\n\nNotes: " + notes +
		"\n\nTimestamp: " + timestamp +
		"\n\nCompleted: " + completionStatus +
		"\n\nCanceled: " + cancelStatus +
		"\n\nPaid: " + paid;
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
		String notes = "Patient scheduled for fillers";
		int timestamp = 102931;
		boolean completionStatus = false;
		boolean cancelStatus = false;
		boolean paid = false;
		Appointment testCase = new Appointment(typeID, patientID, staffList, stationNumber, treatment, notes, timestamp, completionStatus, cancelStatus, paid);
		System.out.println(testCase.viewAppointment());
		

	}
	*/
}
