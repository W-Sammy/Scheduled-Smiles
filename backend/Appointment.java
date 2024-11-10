public class Appointment{
    //private attributes
    private byte[] typeID = new byte[32];
    private int timestamp;
    private boolean completionStatus;
    private boolean cancelStatus;
    private double cost;

    //constructor
    public Appointment(byte[] typeID, int timestamp, boolean completionStatus, boolean cancelStatus, double cost) {
        this.typeID = typeID;
        this.timestamp = timestamp;
        this.completionStatus = completionStatus;
        this.cancelStatus = cancelStatus;
        this.cost = cost;
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