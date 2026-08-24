public class Behavior {
    private final String name;
    private boolean enabled;
    private final String valueDescription;

    public Behavior(String name, boolean enabled, String valueDescription) {
        this.name = name;
        this.enabled = enabled;
        this.valueDescription = valueDescription;
    }

    public Behavior(Behavior other) {
        this(other.name, other.enabled, other.valueDescription);
    }

    public String getName() { return name; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getValueDescription() { return valueDescription; }
}