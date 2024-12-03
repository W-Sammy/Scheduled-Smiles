package Users;
import com.google.gson.annotations.SerializedName;
import Users.User;
import static Users.Enum.RoleConstant.*;

public class Staff extends User {
    // Private Attributes
    @SerializedName("hourlyRate")
    private double hourlyRate;
    
    // Contructor
    public Staff(byte[] userID, String firstName, String lastName, String address,
                 char sex, String phoneNumber, String email, int birthDate, String detail, double hourlyRate){
      super(userID, ROLE_IDS.get("Staff"), firstName, lastName, address, sex, phoneNumber, email, birthDate, detail);         
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