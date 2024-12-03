package Users;
import com.google.gson.annotations.SerializedName;
import Users.User;
import static Users.Enum.RoleConstant.*;

public class Patient extends User {    
    // Constructor
    public Patient(byte[] userID, String firstName, String lastName, String address, char sex, String phoneNumber, String email, int birthDate, String detail) {
        super(userID, ROLE_IDS.get("Patient"), firstName, lastName, address, sex, phoneNumber, email, birthDate, detail);
    }
}