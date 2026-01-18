package process;

// Process: unique ID, priority, state, burst time
public class Process {
    private static int nextPID = 1000;
    private final int PID;
    private final String processName;
    private int priority;           // Higher number = higher priority (1-10)
    private ProcessState state;
    private int totalBurstTime;     // Total time needed for execution
    private int remainingBurstTime; // Remaining execution time
    private int startTime;
    private int endTime;
    private int waitingTime;
    private int turnaroundTime;
    private long creationTime;

    // Create new process
    public Process(String processName, int priority, int burstTime) {
        this.PID = nextPID++;
        this.processName = processName;
        this.priority = priority;
        this.totalBurstTime = burstTime;
        this.remainingBurstTime = burstTime;
        this.state = ProcessState.NEW;
        this.startTime = -1; // -1 = unset, avoid overwriting time 0
        this.endTime = -1;
        this.waitingTime = 0;
        this.turnaroundTime = 0;
        this.creationTime = System.currentTimeMillis();
    }

    // getters/setters
    public int getPID() {
        return PID;
    }

    public String getProcessName() {
        return processName;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public ProcessState getState() {
        return state;
    }

    public void setState(ProcessState state) {
        this.state = state;
    }

    public int getTotalBurstTime() {
        return totalBurstTime;
    }

    public int getRemainingBurstTime() {
        return remainingBurstTime;
    }

    public void setRemainingBurstTime(int remaining) {
        this.remainingBurstTime = Math.max(0, remaining);
    }

    public void reduceRemainingTime(int amount) {
        this.remainingBurstTime = Math.max(0, remainingBurstTime - amount);
    }

    // check if done
    public boolean isComplete() {
        return remainingBurstTime <= 0;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public int getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime;
    }

    public int getTurnaroundTime() {
        return turnaroundTime;
    }

    public void setTurnaroundTime(int turnaroundTime) {
        this.turnaroundTime = turnaroundTime;
    }

    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public String toString() {
        return String.format("[PID: %d, Name: %s, Priority: %d, State: %s, Remaining: %d/%d]",
                PID, processName, priority, state.getDisplayName(), remainingBurstTime, totalBurstTime);
    }
}
