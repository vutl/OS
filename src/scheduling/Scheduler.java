package scheduling;

import process.Process;
import process.ProcessState;
import java.util.*;

/**
 * Scheduler implements both Round-Robin and Priority-Based scheduling algorithms
 * Manages the ready queue and determines which process should run next
 */
public class Scheduler {
    private Queue<Process> readyQueue;
    private PriorityQueue<Process> priorityQueue;
    private List<Process> allProcesses;
    private SchedulingAlgorithm algorithm;
    private int timeQuantum;  // Time slice for round-robin (in time units)
    private int currentTime;
    private boolean running;

    public enum SchedulingAlgorithm {
        ROUND_ROBIN("Round Robin"),
        PRIORITY_BASED("Priority Based"),
        MIXED("Mixed (Priority with Round Robin)");

        private final String displayName;

        SchedulingAlgorithm(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Constructor for Scheduler
     */
    public Scheduler(SchedulingAlgorithm algorithm, int timeQuantum) {
        this.algorithm = algorithm;
        this.timeQuantum = timeQuantum;
        this.readyQueue = new LinkedList<>();
        this.priorityQueue = new PriorityQueue<>((p1, p2) -> Integer.compare(p2.getPriority(), p1.getPriority()));
        this.allProcesses = new ArrayList<>();
        this.currentTime = 0;
        this.running = false;
    }

    /**
     * Add a new process to the scheduler
     */
    public synchronized void addProcess(Process process) {
        process.setState(ProcessState.READY);
        allProcesses.add(process);
        
        if (algorithm == SchedulingAlgorithm.ROUND_ROBIN) {
            readyQueue.add(process);
        } else if (algorithm == SchedulingAlgorithm.PRIORITY_BASED || algorithm == SchedulingAlgorithm.MIXED) {
            priorityQueue.add(process);
        }
    }

    /**
     * Get the next process to be executed based on scheduling algorithm
     */
    public synchronized Process getNextProcess() {
        if (algorithm == SchedulingAlgorithm.ROUND_ROBIN) {
            return readyQueue.poll();
        } else if (algorithm == SchedulingAlgorithm.PRIORITY_BASED || algorithm == SchedulingAlgorithm.MIXED) {
            return priorityQueue.poll();
        }
        return null;
    }

    /**
     * Re-queue a process that hasn't completed (used in time quantum expiration)
     */
    public synchronized void requeueProcess(Process process) {
        if (!process.isComplete()) {
            process.setState(ProcessState.READY);
            if (algorithm == SchedulingAlgorithm.ROUND_ROBIN) {
                readyQueue.add(process);
            } else if (algorithm == SchedulingAlgorithm.PRIORITY_BASED || algorithm == SchedulingAlgorithm.MIXED) {
                priorityQueue.add(process);
            }
        }
    }

    /**
     * Check if there are any processes ready to run
     */
    public synchronized boolean hasReadyProcesses() {
        if (algorithm == SchedulingAlgorithm.ROUND_ROBIN) {
            return !readyQueue.isEmpty();
        } else if (algorithm == SchedulingAlgorithm.PRIORITY_BASED || algorithm == SchedulingAlgorithm.MIXED) {
            return !priorityQueue.isEmpty();
        }
        return false;
    }

    /**
     * Get queue size for monitoring
     */
    public synchronized int getReadyQueueSize() {
        if (algorithm == SchedulingAlgorithm.ROUND_ROBIN) {
            return readyQueue.size();
        } else {
            return priorityQueue.size();
        }
    }

    /**
     * Get all processes
     */
    public synchronized List<Process> getAllProcesses() {
        return new ArrayList<>(allProcesses);
    }

    /**
     * Get processes by state
     */
    public synchronized List<Process> getProcessesByState(ProcessState state) {
        List<Process> result = new ArrayList<>();
        for (Process p : allProcesses) {
            if (p.getState() == state) {
                result.add(p);
            }
        }
        return result;
    }

    // Getters
    public SchedulingAlgorithm getAlgorithm() {
        return algorithm;
    }

    public int getTimeQuantum() {
        return timeQuantum;
    }

    public int getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(int time) {
        this.currentTime = time;
    }

    public void incrementCurrentTime() {
        this.currentTime++;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
