import java.io.*;
import java.util.*;

public class ExperimentModeController {

    public interface StateObserver {
        void onUpdate();
    }

    private ModeConfig current;
    private final List<Mode> availableModes = new ArrayList<>();
    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();
    private final List<StateObserver> observers = new ArrayList<>();

    public ExperimentModeController() {
        availableModes.add(Mode.STABLE);
        availableModes.add(Mode.EXPERIMENTAL);
        availableModes.add(Mode.DEBUG);
        reset();
    }

    public void addObserver(StateObserver o) {
        observers.add(o);
    }

    public void notifyListeners() {
        for (StateObserver o : observers) {
            o.onUpdate();
        }
    }

    public void addCustomMode(String label, String description) {
        Mode custom = new Mode(label, description);
        availableModes.add(custom);
        switchMode(custom);
    }

    public List<Mode> getAvailableModes() {
        return availableModes;
    }

    public void execute(Command cmd) {
        cmd.execute();
        undoStack.push(cmd);
        redoStack.clear();
        notifyListeners();
    }

    public boolean toggleBehavior(String name) {
        Behavior b = current.getBehaviors().get(name);
        if (b == null) return false;

        boolean target = !b.isEnabled();

        if (current.getMode().equals(Mode.STABLE) && "Verbose output".equals(name) && !target) {
            return false;
        }

        execute(new Command() {
            @Override
            public void execute() {
                b.setEnabled(target);
            }

            @Override
            public void undo() {
                b.setEnabled(!target);
            }

            @Override
            public String getDescription() {
                return "Toggled " + name + " -> " + (target ? "ON" : "OFF");
            }
        });
        return true;
    }

    public void switchMode(Mode targetMode) {
        if (targetMode.equals(current.getMode())) return;
        ModeConfig previous = new ModeConfig(current);

        execute(new Command() {
            @Override
            public void execute() {
                current = new ModeConfig(targetMode);
            }

            @Override
            public void undo() {
                current = new ModeConfig(previous);
            }

            @Override
            public String getDescription() {
                return "Switched -> " + targetMode.getLabel();
            }
        });
    }

    public boolean undo() {
        if (undoStack.isEmpty()) return false;
        Command cmd = undoStack.pop();
        cmd.undo();
        redoStack.push(cmd);
        notifyListeners();
        return true;
    }

    public boolean redo() {
        if (redoStack.isEmpty()) return false;
        Command cmd = redoStack.pop();
        cmd.execute();
        undoStack.push(cmd);
        notifyListeners();
        return true;
    }

    public void reset() {
        current = new ModeConfig(Mode.STABLE);
        undoStack.clear();
        redoStack.clear();
        notifyListeners();
    }

    public void exportProfile(File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("MODE=" + current.getMode().getLabel());
            writer.newLine();
            for (Map.Entry<String, Behavior> entry : current.getBehaviors().entrySet()) {
                writer.write(entry.getKey() + "=" + entry.getValue().isEnabled());
                writer.newLine();
            }
        }
    }

    public void importProfile(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            Mode loadedMode = Mode.STABLE;
            Map<String, Boolean> loadedToggles = new HashMap<>();

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("=", 2);
                if (parts.length < 2) continue;

                if (parts[0].equals("MODE")) {
                    for (Mode m : availableModes) {
                        if (m.getLabel().equalsIgnoreCase(parts[1])) {
                            loadedMode = m;
                            break;
                        }
                    }
                } else {
                    loadedToggles.put(parts[0], Boolean.parseBoolean(parts[1]));
                }
            }

            current = new ModeConfig(loadedMode);
            for (Map.Entry<String, Boolean> entry : loadedToggles.entrySet()) {
                Behavior b = current.getBehaviors().get(entry.getKey());
                if (b != null) b.setEnabled(entry.getValue());
            }
            undoStack.clear();
            redoStack.clear();
            notifyListeners();
        }
    }

    public Mode getCurrentMode() {
        return current.getMode();
    }

    public ModeConfig getCurrentConfig() {
        return current;
    }

    public Deque<Command> getUndoStack() {
        return undoStack;
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
}