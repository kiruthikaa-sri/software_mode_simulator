import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class TrafficSimulator extends Thread {
    private final ExperimentModeController controller;
    private final LogBuffer logBuffer;
    private final SystemMetrics metrics;
    private volatile boolean running = true;
    private volatile boolean chaosEnabled = false;
    private final Random rand = new Random();

    // 2-argument constructor (creates default metrics)
    public TrafficSimulator(ExperimentModeController controller, LogBuffer logBuffer) {
        this(controller, logBuffer, new SystemMetrics());
    }

    // 3-argument constructor
    public TrafficSimulator(ExperimentModeController controller, LogBuffer logBuffer, SystemMetrics metrics) {
        this.controller = controller;
        this.logBuffer = logBuffer;
        this.metrics = metrics != null ? metrics : new SystemMetrics();
        setDaemon(true);
    }

    public void setChaosEnabled(boolean enabled) {
        this.chaosEnabled = enabled;
    }

    public void stopSimulator() {
        this.running = false;
        this.interrupt();
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(900);
                simulateCycle();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void simulateCycle() {
        if (controller == null || controller.getCurrentConfig() == null) {
            return;
        }

        ModeConfig cfg = controller.getCurrentConfig();
        Mode mode = controller.getCurrentMode();

        Behavior rateLimit = cfg.getBehaviors().get("Rate limiting");
        Behavior verbose = cfg.getBehaviors().get("Verbose output");

        boolean isRateLimited = rateLimit != null && rateLimit.isEnabled();
        boolean isVerbose = verbose != null && verbose.isEnabled();

        int cpu = (isVerbose ? 45 : 15) + (chaosEnabled ? 35 : 0) + rand.nextInt(12);
        int mem = (Mode.DEBUG.equals(mode) ? 750 : 220) + rand.nextInt(50);
        int latency = (isRateLimited ? 25 : 85) + (chaosEnabled ? 180 : 0) + rand.nextInt(20);

        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String endpoint = "/api/v2/service/" + rand.nextInt(50);
        boolean blocked = false;

        if (chaosEnabled && rand.nextInt(10) > 6) {
            logBuffer.append("[" + time + "] [500 CHAOS FAULT] Node dropped -> " + endpoint);
            cpu = Math.min(100, cpu + 25);
        } else if (isRateLimited && rand.nextInt(10) > 7) {
            logBuffer.append("[" + time + "] [429 RATE LIMIT] Throttled -> " + endpoint);
            blocked = true;
        } else if (isVerbose || Mode.DEBUG.equals(mode)) {
            logBuffer.append("[" + time + "] [200 TRACE] Inspected trace -> " + endpoint);
        } else {
            logBuffer.append("[" + time + "] [200 OK] Served request -> " + endpoint);
        }

        metrics.update(Math.min(100, cpu), mem, latency, blocked);
        controller.notifyListeners();
    }
}