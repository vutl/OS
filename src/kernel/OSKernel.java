package kernel;

import process.Process;
import process.Thread;
import scheduling.Scheduler;
import dispatcher.Dispatcher;
import java.util.*;

// Main kernel: coordinates scheduler, dispatcher, processes
public class OSKernel {
    private Scheduler scheduler;
    private Dispatcher dispatcher;
    private List<Process> processes;
    private Map<Integer, List<Thread>> processThreads;
    private int simulationTime;
    private boolean simulationRunning;

    // init kernel
    public OSKernel(Scheduler.SchedulingAlgorithm algorithm, int timeQuantum) {
        this.scheduler = new Scheduler(algorithm, timeQuantum);
        this.dispatcher = new Dispatcher(scheduler);
        this.processes = new ArrayList<>();
        this.processThreads = new HashMap<>();
        this.simulationTime = 0;
        this.simulationRunning = false;
    }

    // new process
    public Process createProcess(String processName, int priority, int burstTime) {
        Process process = new Process(processName, priority, burstTime);
        processes.add(process);
        scheduler.addProcess(process);
        processThreads.put(process.getPID(), new ArrayList<>());
        return process;
    }

    // new thread
    public Thread createThread(int parentPID, String threadName, int executionTime, Thread.ThreadPriority priority) {
        Thread thread = new Thread(parentPID, threadName, executionTime, priority);
        List<Thread> threads = processThreads.get(parentPID);
        if (threads != null) {
            threads.add(thread);
            return thread;
        }
        return null;
    }

    // run one kernel cycle
    public void runKernelCycle() {
        // Dispatcher advances "CPU time"; we keep Scheduler.currentTime aligned to CPU time inside Dispatcher.
        dispatcher.runCycle();
        simulationTime++;
    }

    /**
     * Run the complete simulation
     */
    public void runSimulation(int maxTime) {
        System.out.println("========== OS KERNEL SIMULATION STARTED ==========");
        System.out.println("Scheduling Algorithm: " + scheduler.getAlgorithm().getDisplayName());
        System.out.println("Time Quantum: " + scheduler.getTimeQuantum() + " units\n");
        
        simulationRunning = true;
        scheduler.setRunning(true);

        // Continue simulation until no more processes or max time reached
        while (simulationRunning && simulationTime < maxTime) {
            // Check if there are any incomplete processes
            List<Process> allProcesses = scheduler.getAllProcesses();
            boolean hasIncompleteProcesses = false;
            
            for (Process p : allProcesses) {
                if (p.getState() != process.ProcessState.TERMINATED) {
                    hasIncompleteProcesses = true;
                    break;
                }
            }

            if (!hasIncompleteProcesses && dispatcher.isCPUIdle()) {
                break; // All processes completed
            }

            dispatcher.runCycle();
            simulationTime++;
        }

        simulationRunning = false;
        scheduler.setRunning(false);
        System.out.println("\n========== OS KERNEL SIMULATION COMPLETED ==========");
        printSimulationResults();
    }

    /**
     * Print simulation results and statistics
     */
    public void printSimulationResults() {
        System.out.println("\n========== SIMULATION RESULTS ==========");
        System.out.println("Total Simulation Time: " + simulationTime + " units");
        System.out.println("\nProcess Statistics:");
        System.out.println("--------------------------------------------");

        List<Process> allProcesses = scheduler.getAllProcesses();
        int totalWaitingTime = 0;
        int totalTurnaroundTime = 0;

        for (Process p : allProcesses) {
            if (p.getState() == process.ProcessState.TERMINATED) {
                int turnaroundTime = p.getEndTime() - 0; // Assuming start at 0
                int waitingTime = turnaroundTime - p.getTotalBurstTime();
                
                System.out.printf("PID: %d | Name: %s | Priority: %d | End Time: %d | Turnaround Time: %d\n",
                    p.getPID(), p.getProcessName(), p.getPriority(), p.getEndTime(), turnaroundTime);
                
                totalWaitingTime += waitingTime;
                totalTurnaroundTime += turnaroundTime;
            }
        }

        int completedProcesses = (int) allProcesses.stream()
            .filter(p -> p.getState() == process.ProcessState.TERMINATED)
            .count();

        if (completedProcesses > 0) {
            System.out.println("\nAverage Waiting Time: " + (totalWaitingTime / completedProcesses));
            System.out.println("Average Turnaround Time: " + (totalTurnaroundTime / completedProcesses));
        }
        
        System.out.println("=========================================\n");
    }

    /**
     * Print execution trace
     */
    public void printExecutionTrace() {
        System.out.println("\n========== EXECUTION TRACE ==========");
        List<String> logs = dispatcher.getDispatchLog();
        for (String log : logs) {
            System.out.println(log);
        }
        System.out.println("====================================\n");
    }

    /**
     * Get scheduler
     */
    public Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * Get dispatcher
     */
    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    /**
     * Get all processes
     */
    public List<Process> getProcesses() {
        return new ArrayList<>(processes);
    }

    /**
     * Get threads of a process
     */
    public List<Thread> getProcessThreads(int parentPID) {
        return processThreads.getOrDefault(parentPID, new ArrayList<>());
    }

    /**
     * Stop simulation
     */
    public void stopSimulation() {
        simulationRunning = false;
    }
}
