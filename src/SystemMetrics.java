public class SystemMetrics {
    private int cpuUsagePercent;
    private int memoryUsageMb;
    private int latencyMs;
    private int totalRequests;
    private int blockedRequests;

    public synchronized void update(int cpu, int mem, int latency, boolean blocked) {
        this.cpuUsagePercent = cpu;
        this.memoryUsageMb = mem;
        this.latencyMs = latency;
        this.totalRequests++;
        if (blocked) this.blockedRequests++;
    }

    public synchronized int getCpuUsagePercent() { return cpuUsagePercent; }
    public synchronized int getMemoryUsageMb() { return memoryUsageMb; }
    public synchronized int getLatencyMs() { return latencyMs; }
    public synchronized int getTotalRequests() { return totalRequests; }
    public synchronized int getBlockedRequests() { return blockedRequests; }
}