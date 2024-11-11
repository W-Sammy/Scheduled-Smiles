package Users.Enum;
public enum RoleConstant {
    PATIENT(0),
    STAFF(1),
    ADMIN(2);
    //Value does not change 
    private final int value;
    // Constructor assigning role value to User type
    RoleConstant(int value) {
        this.value = value;
    }
    // Getter 
    public int getValue() {
        return this.value;
    }
}
