import java.util.ArrayList;
import java.util.List;

public class LogBuffer {
    private final String[] data;
    private int head = 0;
    private int count = 0;
    private final int capacity;

    public LogBuffer(int capacity) {
        this.capacity = capacity;
        this.data = new String[capacity];
    }

    public synchronized void append(String entry) {
        int index = (head + count) % capacity;
        data[index] = entry;
        if (count < capacity) {
            count++;
        } else {
            head = (head + 1) % capacity;
        }
    }

    public synchronized List<String> getAll() {
        List<String> logs = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            logs.add(data[(head + i) % capacity]);
        }
        return logs;
    }
}