public class Bill extends Appointment
{
    //private attributes
    private byte billID;
    private double amountDue;
    private boolean status;

    //constuctor
    public Bill(byte typeID, int timestamp, boolean completionStatus, boolean cancelStatus, double cost, byte billID, double amountDue, boolean status)
    {
        super(typeID, timestamp, completionStatus, cancelStatus, cost);
        this.billID = billID;
        this.amountDue = amountDue;
        this.status = status;
    }

    //getter methods
    public byte getBillID()
    {
        return billID;
    }

    public double getAmountDue()
    {
        return amountDue;
    }

    public boolean getStatus()
    {
        return status;
    }

    public void setBillID(byte billID)
    {
        this.billID = billID;
    }
    public void setAmountDue(double amountDue)
    {
        this.amountDue = amountDue;
    }

    public void setStatus(boolean status)
    {
        this.status = status;
    }
}