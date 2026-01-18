package scheduling;

import process.Process;
import process.ProcessState;
import java.util.*;

// Scheduler: picks next process by priority + RR
public class Scheduler {
    private Queue<Process> readyQueue;
    private PriorityQueue<Process> priorityQueue;

    // Pick highest priority, then RR within same priority
    private final NavigableMap<Integer, Deque<Process>> prioRrQueues;
    private int prioRrSize;

    private List<Process> allProcesses;
    private SchedulingAlgorithm algorithm;
    private int timeQuantum;  // Time slice for round-robin
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

    // init scheduler
    public Scheduler(SchedulingAlgorithm algorithm, int timeQuantum) {
        this.algorithm = algorithm;
        this.timeQuantum = timeQuantum;
        this.readyQueue = new LinkedList<>();
        this.priorityQueue = new PriorityQueue<>((p1, p2) -> Integer.compare(p2.getPriority(), p1.getPriority()));
        this.prioRrQueues = new TreeMap<>(Collections.reverseOrder());
        this.prioRrSize = 0;
        this.allProcesses = new ArrayList<>();
        this.currentTime = 0;
        this.running = false;
    }

    // add to ready queue
    public synchronized void addProcess(Process process) {
        process.setState(ProcessState.READY);
        allProcesses.add(process);

        if (algorithm == SchedulingAlgorithm.ROUND_ROBIN) {
            readyQueue.add(process);
        } else if (algorithm == SchedulingAlgorithm.PRIORITY_BASED) {
            priorityQueue.add(process);
        } else { // MIXED
            prioRrQueues.computeIfAbsent(process.getPriority(), k -> new ArrayDeque<>()).addLast(process);
            prioRrSize++;
        }
    }

    // pick next process
    public synchronized Process getNextProcess() {
        if (algorithm == SchedulingAlgorithm.ROUND_ROBIN) {
            return readyQueue.poll();
        } else if (algorithm == SchedulingAlgorithm.PRIORITY_BASED) {
            return priorityQueue.poll();
        } else { // MIXED
            if (prioRrSize == 0) return null;

            Map.Entry<Integer, Deque<Process>> entry = prioRrQueues.firstEntry();
            while (entry != null && entry.getValue().isEmpty()) {
                prioRrQueues.remove(entry.getKey());
                entry = prioRrQueues.firstEntry();
            }
            if (entry == null) {
                prioRrSize = 0;
                return null;
            }

            Process p = entry.getValue().pollFirst();
            prioRrSize--;

            if (entry.getValue().isEmpty()) {
                prioRrQueues.remove(entry.getKey());
            }
            return p;
        }
    }

    // put back at end of queue (or context switch)
    public synchronized void requeueProcess(Process process) {
        if (!process.isComplete()) {
            process.setState(ProcessState.READY);

            if (algorithm == SchedulingAlgorithm.ROUND_ROBIN) {
                readyQueue.add(process);
            } else if (algorithm == SchedulingAlgorithm.PRIORITY_BASED) {
                priorityQueue.add(process);
            } else {
                prioRrQueues.computeIfAbsent(process.getPriority(), k -> new ArrayDeque<>()).addLast(process);
                prioRrSize++;
            }
        }
    }

    // see any process waiting
    public synchronized boolean hasReadyProcesses() {
        if (algorithm == SchedulingAlgorithm.ROUND_ROBIN) {
            return !readyQueue.isEmpty();
        } else if (algorithm == SchedulingAlgorithm.PRIORITY_BASED) {
            return !priorityQueue.isEmpty();
        } else {
            return prioRrSize > 0;
        }
    }

    // queue size
    public synchronized int getReadyQueueSize() {
        if (algorithm == SchedulingAlgorithm.ROUND_ROBIN) {
            return readyQueue.size();
        } else if (algorithm == SchedulingAlgorithm.PRIORITY_BASED) {
            return priorityQueue.size();
        } else {
            return prioRrSize;
        }
    }

    public synchronized List<Process> getAllProcesses() {
        return new ArrayList<>(allProcesses);
    }

    public synchronized List<Process> getProcessesByState(ProcessState state) {
        List<Process> result = new ArrayList<>();
        for (Process p : allProcesses) {
            if (p.getState() == state) result.add(p);
        }
        return result;
    }

    // Getters
    public SchedulingAlgorithm getAlgorithm() { return algorithm; }
    public int getTimeQuantum() { return timeQuantum; }
    public int getCurrentTime() { return currentTime; }
    public void setCurrentTime(int time) { this.currentTime = time; }
    public void incrementCurrentTime() { this.currentTime++; }
    public boolean isRunning() { return running; }
    public void setRunning(boolean running) { this.running = running; }
}
