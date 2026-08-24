import java.util.Objects;

public class Mode {
    private final String label;
    private final String description;

    // Public constructor allows creating both predefined and custom runtime modes
    public Mode(String label, String description) {
        this.label = label;
        this.description = description;
    }

    // Standard preset modes
    public static final Mode STABLE = new Mode("Stable", "Safe, production ready");
    public static final Mode EXPERIMENTAL = new Mode("Experimental", "Testing new features");
    public static final Mode DEBUG = new Mode("Debug", "Deep analysis mode");

    // Array of standard modes to satisfy Mode.values() calls
    public static Mode[] values() {
        return new Mode[] { STABLE, EXPERIMENTAL, DEBUG };
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Mode)) return false;
        Mode mode = (Mode) o;
        return Objects.equals(label, mode.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label);
    }

    @Override
    public String toString() {
        return label;
    }
}