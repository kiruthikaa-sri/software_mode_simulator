import java.time.LocalTime;// Import class to get current time (hours, minutes, seconds)
import java.time.format.DateTimeFormatter;// Import class to format time into a readable string

public class HistoryEntry {
    private final String description;// Stores a description of the event/action
    private final String timestamp; // Stores the time when this entry was created
    private final ModeConfig snapshot;// Stores a copy of the system state (ModeConfig)

    // Constructor to initialize a HistoryEntry object
    public HistoryEntry(String description, ModeConfig snapshot) {
        this.description = description;// Assign the given description to the field
        this.timestamp   = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")); // Get current time and format it as HH:mm:ss (e.g., 14:30:45)
        this.snapshot    = new ModeConfig(snapshot);/*Creates a copy of the snapshot object.
                                    This is important: it prevents changes to the original object from affecting this stored version.
                                    This assumes ModeConfig has a copy constructor.*/
    }

    public String getDescription()  { return description; } // Getter method to return description
    public String getTimestamp()    { return timestamp; } // Getter method to return timestamp
    public ModeConfig getSnapshot() { return snapshot; }/**/

    @Override/*Overrides the default toString() method from Object.
    This method defines how the object is represented as a string.*/
    public String toString() {
        return "[" + timestamp + "]  " + description;
    }
}

