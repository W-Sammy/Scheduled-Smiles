package Users.Enum;

import java.util.Map;

public class AppointmentType {
    private AppointmentType() {
        // restrict instantiation -Kyle
    }
    public static final Map<String, Double> APPOINTMENT_TYPE_FEE = Map.of(
        "Cleaning", 250.00,
        "Checkup", 200.00,
        "Emergency", 750.00,
        "X-Ray", 200.00,
        "Filling", 300.00,
        "Other", 0.0 // Misc. case just in case... -Kyle
    );
}