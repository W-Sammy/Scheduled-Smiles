package Users;
import com.google.gson.annotations.SerializedName;
import Users.User;
import static Users.Enum.RoleConstant.*;

public class Admin extends User {
    // Attributes
    @SerializedName("hourlyRate")
    private double hourlyRate;
    // constructor
    public Admin(byte[] userID, String firstName, String lastName, String address, char sex, String phoneNumber, String email, int birthDate, double hourlyRate) {
        super(userID, ROLE_IDS.get("Admin"), firstName, lastName, address, sex, phoneNumber, email, birthDate);
        this.hourlyRate = hourlyRate;
    } // end constructor
    
    // Getter for hourlyRate
    public double getHourlyRate() {
        return hourlyRate;
    }
      
      // Setter for hourlyRate
    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }    
}