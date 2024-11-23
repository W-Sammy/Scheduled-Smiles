package Users.Enum;

import java.util.Map;

import static Server.utils.Requests.*; // only need the hash256 method, is there a better way to import this? -Kyle

public class AppointmentType {
    private AppointmentType() {
        // restrict instantiation -Kyle
    }
    public static final Map<String, byte[]> APPOINTMENT_TYPE = Map.of(
        "Cleaning", hash256("Cleaning"),
        "Checkup", hash256("Checkup"),
        "Emergency", hash256("Emergency"),
        "X-Ray", hash256("X-Ray"),
        "Filling", hash256("Filling"),
        "Other", hash256("Other") // Misc. case just in case... -Kyle
    );
    public static final Map<String, Double> APPOINTMENT_TYPE_FEE = Map.of(
        "Cleaning", 250.00,
        "Checkup", 200.00,
        "Emergency", 750.00,
        "X-Ray", 200.00,
        "Filling", 300.00,
        "Other", 0.0 // Misc. case just in case... -Kyle
    );
}