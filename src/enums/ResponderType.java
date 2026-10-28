package enums;

public enum ResponderType {
    MEDIC("Medic", "MED", 1),
    RESCUER("Rescuer", "RES", 2),
    FIREFIGHTER("Firefighter", "FIR", 3),
    ENGINEER("Engineer", "ENG", 4),
    TRICYCLE_DRIVER("Moto-Transport", "MTO", 5);

    private final String label;
    private final String code;
    private final int hotkey;

    ResponderType(String label, String code, int hotkey) {
        this.label = label;
        this.code = code;
        this.hotkey = hotkey;
    }

    public String getLabel() {
        return label;
    }

    public String getCode() {
        return code;
    }

    public int getHotkey() {
        return hotkey;
    }

    public static ResponderType fromHotkey(int hotkey) {
        for (ResponderType type : values()) {
            if (type.hotkey == hotkey) {
                return type;
            }
        }
        return null;
    }
}
