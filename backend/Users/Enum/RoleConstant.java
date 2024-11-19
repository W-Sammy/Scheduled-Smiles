package Users.Enum;

import java.util.Map;

import static Server.utils.Requests.*; // only need the hash256 method, is there a better way to import this? -Kyle

public class RoleConstant {
    private RoleConstant() {
        // restrict instantiation -Kyle
    }
    public static final Map<String, byte[]> ROLE_IDS = Map.of(
        "Staff", hash256("Staff"),
        "Patient", hash256("Patient"),
        "Admin", hash256("Admin")
    );
    public static final Map<String, String> ROLE_DOMAINS = Map.of( // email extensions, after the ampersand (...@[domain])
        "Staff", "scheduledsmiles.com",
        "Admin", "scheduledsmiles.adm.com"
        // Don't include patient email since it'd just be a wildcard. -Kyle
    );
}