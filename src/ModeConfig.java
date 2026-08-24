import java.util.LinkedHashMap;
import java.util.Map;

public class ModeConfig {
    private final Mode mode;
    private final Map<String, Behavior> behaviors = new LinkedHashMap<>();

    public ModeConfig(Mode mode) {
        this.mode = mode;
        initBehaviors();
    }

    public ModeConfig(ModeConfig other) {
        this.mode = other.mode;
        for (Map.Entry<String, Behavior> entry : other.behaviors.entrySet()) {
            this.behaviors.put(entry.getKey(), new Behavior(entry.getValue()));
        }
    }

    private void initBehaviors() {
        if (Mode.STABLE.equals(mode)) {
            behaviors.put("Error logging", new Behavior("Error logging", true, "Standard output"));
            behaviors.put("Feature flags", new Behavior("Feature flags", false, "All disabled"));
            behaviors.put("Rate limiting", new Behavior("Rate limiting", true, "100 req/min"));
            behaviors.put("Verbose output", new Behavior("Verbose output", true, "Full request log"));
        } else if (Mode.EXPERIMENTAL.equals(mode)) {
            behaviors.put("Error logging", new Behavior("Error logging", true, "Detailed stack trace"));
            behaviors.put("Feature flags", new Behavior("Feature flags", false, "All enabled"));
            behaviors.put("Rate limiting", new Behavior("Rate limiting", true, "Disabled for testing"));
            behaviors.put("Verbose output", new Behavior("Verbose output", false, "Full request log"));
        } else if (Mode.DEBUG.equals(mode)) {
            behaviors.put("Error logging", new Behavior("Error logging", true, "With memory dump"));
            behaviors.put("Feature flags", new Behavior("Feature flags", false, "Selectable per-call"));
            behaviors.put("Rate limiting", new Behavior("Rate limiting", false, "Disabled"));
            behaviors.put("Verbose output", new Behavior("Verbose output", false, "Step-by-step trace"));
        } else {
            // Default template for dynamically created custom modes
            behaviors.put("Error logging", new Behavior("Error logging", true, "Custom runtime log"));
            behaviors.put("Feature flags", new Behavior("Feature flags", false, "Dynamic flag"));
            behaviors.put("Rate limiting", new Behavior("Rate limiting", true, "Custom throttling"));
            behaviors.put("Verbose output", new Behavior("Verbose output", false, "Dynamic trace"));
        }
    }

    public Mode getMode() {
        return mode;
    }

    public Map<String, Behavior> getBehaviors() {
        return behaviors;
    }

    public String getSafetyStatus() {
        Behavior rateLimit = behaviors.get("Rate limiting");
        Behavior features = behaviors.get("Feature flags");

        if (Mode.DEBUG.equals(mode) && rateLimit != null && !rateLimit.isEnabled()) {
            return "UNSAFE: Debug mode without active rate limiting";
        }
        if (Mode.EXPERIMENTAL.equals(mode) && features != null && features.isEnabled()) {
            return "RISK: All experimental features active";
        }
        return "SAFE";
    }
}