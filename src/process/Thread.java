package process;

// User thread: has own ID, parent PID, remaining time
public class Thread {
    private static int nextThreadID = 2000;
    private final int threadID;
    private final int parentPID;
    private String threadName;
    private ProcessState state;
    private int executionTime;
    private int remainingTime;
    private ThreadPriority priority;
    private long creationTime;

    public enum ThreadPriority {
        LOW(1), NORMAL(5), HIGH(10);

        public final int value;

        ThreadPriority(int value) {
            this.value = value;
        }
    }

    // New thread in process
    public Thread(int parentPID, String threadName, int executionTime, ThreadPriority priority) {
        this.threadID = nextThreadID++;
        this.parentPID = parentPID;
        this.threadName = threadName;
        this.executionTime = executionTime;
        this.remainingTime = executionTime;
        this.state = ProcessState.NEW;
        this.priority = priority;
        this.creationTime = System.currentTimeMillis();
    }

    // getters/setters
    public int getThreadID() {
        return threadID;
    }

    public int getParentPID() {
        return parentPID;
    }

    public String getThreadName() {
        return threadName;
    }

    public ProcessState getState() {
        return state;
    }

    public void setState(ProcessState state) {
        this.state = state;
    }

    public int getExecutionTime() {
        return executionTime;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void reduceRemainingTime(int amount) {
        this.remainingTime = Math.max(0, remainingTime - amount);
    }

    public boolean isComplete() {
        return remainingTime <= 0;
    }

    public ThreadPriority getPriority() {
        return priority;
    }

    public void setPriority(ThreadPriority priority) {
        this.priority = priority;
    }

    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public String toString() {
        return String.format("[TID: %d, Name: %s, ParentPID: %d, Priority: %s, State: %s, Remaining: %d/%d]",
                threadID, threadName, parentPID, priority, state.getDisplayName(), remainingTime, executionTime);
    }
}
