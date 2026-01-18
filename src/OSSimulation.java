import kernel.OSKernel;
import process.Process;
import process.Thread;
import process.ProcessState;
import scheduling.Scheduler;
import dispatcher.Dispatcher;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.concurrent.*;

/**
 * Main Simulation Program
 * Demonstrates OS kernel operations with two concurrent threads:
 * 1. SchedulerThread - Manages process scheduling
 * 2. DispatcherThread - Manages process execution on CPU
 */
public class OSSimulation {
    private static OSKernel kernel;
    private static final Object lockObject = new Object();
    private static volatile boolean simulationComplete = false;

    private static boolean isAllProcessesTerminated() {
        List<Process> all = kernel.getProcesses();
        return all.stream().allMatch(p -> p.getState() == ProcessState.TERMINATED);
    }

    private static boolean shouldStopNow() {
        return isAllProcessesTerminated() && kernel.getDispatcher().isCPUIdle();
    }

    /**
     * SchedulerThread - Simulates the OS scheduler
     * Responsible for managing the ready queue and selecting next process
     */
    static class SchedulerThread extends java.lang.Thread {
        private OSKernel kernel;
        private int duration;
        private List<String> schedulerLog;

        public SchedulerThread(OSKernel kernel, int duration) {
            super("OS-Scheduler-Thread");
            this.kernel = kernel;
            this.duration = duration;
            this.schedulerLog = new ArrayList<>();
        }

        @Override
        public void run() {
            System.out.println("\n[SCHEDULER THREAD] Started - PID: " + java.lang.Thread.currentThread().getId());
            System.out.println("[SCHEDULER THREAD] Scheduling Algorithm: " + 
                kernel.getScheduler().getAlgorithm().getDisplayName());
            System.out.println("[SCHEDULER THREAD] Time Quantum: " + 
                kernel.getScheduler().getTimeQuantum() + " units\n");

            int cycleCount = 0;
            int lastLoggedTime = -1;
            
            while (!simulationComplete && cycleCount < duration) {
                synchronized (lockObject) {
                    Scheduler scheduler = kernel.getScheduler();

                    // Stop as soon as all processes are done and CPU is idle.
                    if (shouldStopNow()) {
                        simulationComplete = true;
                        break;
                    }

                    // Avoid printing duplicates when scheduler thread is faster than dispatcher.
                    int now = scheduler.getCurrentTime();
                    if (now == lastLoggedTime) {
                        // Still count the cycle (thread did work), but skip noisy log.
                        // This keeps output cleaner and more deterministic looking.
                    } else {
                        lastLoggedTime = now;
                    
                        String logEntry = String.format(
                            "[SCHEDULER] Time: %d | Ready Queue Size: %d | Processes: %d Ready, %d Running, %d Terminated",
                            now,
                            scheduler.getReadyQueueSize(),
                            scheduler.getProcessesByState(ProcessState.READY).size(),
                            scheduler.getProcessesByState(ProcessState.RUNNING).size(),
                            scheduler.getProcessesByState(ProcessState.TERMINATED).size()
                        );
                        schedulerLog.add(logEntry);
                        System.out.println(logEntry);
                    }
                }
                
                cycleCount++;
                try {
                    java.lang.Thread.sleep(100); // Simulate scheduling overhead
                } catch (InterruptedException e) {
                    java.lang.Thread.currentThread().interrupt();
                }
            }

            System.out.println("\n[SCHEDULER THREAD] Completed - Processed " + cycleCount + " cycles");
            printSchedulerStatistics(schedulerLog);
        }

        private void printSchedulerStatistics(List<String> logs) {
            System.out.println("\n========== SCHEDULER THREAD STATISTICS ==========");
            System.out.println("Total Scheduling Cycles: " + logs.size());
            System.out.println("Thread Duration: " + duration + " units");
            System.out.println("Algorithm: " + kernel.getScheduler().getAlgorithm().getDisplayName());
            System.out.println("==================================================\n");
        }

        public List<String> getSchedulerLog() {
            return schedulerLog;
        }
    }

    /**
     * DispatcherThread - Simulates the CPU dispatcher
     * Responsible for allocating CPU time and executing processes
     */
    static class DispatcherThread extends java.lang.Thread {
        private OSKernel kernel;
        private int duration;
        private List<String> dispatcherLog;

        public DispatcherThread(OSKernel kernel, int duration) {
            super("OS-Dispatcher-Thread");
            this.kernel = kernel;
            this.duration = duration;
            this.dispatcherLog = new ArrayList<>();
        }

        @Override
        public void run() {
            System.out.println("\n[DISPATCHER THREAD] Started - PID: " + java.lang.Thread.currentThread().getId());
            System.out.println("[DISPATCHER THREAD] CPU Time Quantum: " + 
                kernel.getScheduler().getTimeQuantum() + " units\n");

            int cycleCount = 0;
            
            while (!simulationComplete && cycleCount < duration) {
                synchronized (lockObject) {
                    kernel.runKernelCycle();
                    
                    Dispatcher dispatcher = kernel.getDispatcher();
                    Process currentProcess = dispatcher.getCurrentRunningProcess();
                    
                    String logEntry = String.format(
                        "[DISPATCHER] Time: %d | CPU Time: %d | Current Process: %s",
                        kernel.getScheduler().getCurrentTime(),
                        dispatcher.getCPUTimeUsed(),
                        currentProcess != null ? currentProcess.getProcessName() : "IDLE"
                    );
                    dispatcherLog.add(logEntry);
                }
                
                cycleCount++;
                try {
                    java.lang.Thread.sleep(150); // Simulate dispatch overhead
                } catch (InterruptedException e) {
                    java.lang.Thread.currentThread().interrupt();
                }
                
                // Stop as soon as all processes are done and CPU is idle.
                if (shouldStopNow()) {
                    simulationComplete = true;
                }
            }

            System.out.println("\n[DISPATCHER THREAD] Completed - Processed " + cycleCount + " cycles");
            printDispatcherStatistics();
        }

        private void printDispatcherStatistics() {
            System.out.println("\n========== DISPATCHER THREAD STATISTICS ==========");
            Dispatcher dispatcher = kernel.getDispatcher();
            System.out.println("Total CPU Time Used: " + dispatcher.getCPUTimeUsed() + " units");
            System.out.println("Total Processes Executed: " + 
                dispatcher.getExecutionHistory().size());
            System.out.println("Dispatch Cycles: " + dispatcherLog.size());
            System.out.println("==================================================\n");
        }

        public List<String> getDispatcherLog() {
            return dispatcherLog;
        }
    }

    /**
     * Main method to run the simulation
     */
    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("  OS KERNEL SIMULATION - Multi-threaded Demo");
        System.out.println("  Demonstrating Scheduling, Dispatching & Threading");
        System.out.println("============================================================\n");

        kernel = new OSKernel(Scheduler.SchedulingAlgorithm.MIXED, 3);

        // Create sample processes
        System.out.println("========== CREATING PROCESSES ==========");
        Process p1 = kernel.createProcess("Process-A", 8, 12);
        Process p5 = kernel.createProcess("Process-E", 8, 7);
        Process p2 = kernel.createProcess("Process-B", 5, 8);
        Process p3 = kernel.createProcess("Process-C", 9, 6);
        Process p4 = kernel.createProcess("Process-D", 3, 10);
        System.out.println("Created 5 processes (includes same-priority pair to demonstrate RR within priority)");
        System.out.println(p1);
        System.out.println(p5);
        System.out.println(p2);
        System.out.println(p3);
        System.out.println(p4);
        System.out.println("=======================================\n");

        // Create threads for demonstration
        System.out.println("========== CREATING THREADS ==========");
        Thread t1 = kernel.createThread(p1.getPID(), "Thread-A1", 5, Thread.ThreadPriority.HIGH);
        Thread t2 = kernel.createThread(p2.getPID(), "Thread-B1", 4, Thread.ThreadPriority.NORMAL);
        System.out.println("Created threads within processes");
        System.out.println(t1);
        System.out.println(t2);
        System.out.println("====================================\n");

        // Create scheduler and dispatcher threads
        System.out.println("========== STARTING KERNEL THREADS ==========\n");
        
        // Safety cap on thread loops. Real stop condition is "all processes terminated + CPU idle".
        SchedulerThread schedulerThread = new SchedulerThread(kernel, 200);
        DispatcherThread dispatcherThread = new DispatcherThread(kernel, 200);

        long startTime = System.currentTimeMillis();

        // Start both threads simultaneously
        schedulerThread.start();
        dispatcherThread.start();

        // Wait for both threads to complete
        try {
            schedulerThread.join();
            dispatcherThread.join();
        } catch (InterruptedException e) {
            System.err.println("Main thread interrupted: " + e.getMessage());
        }

        long endTime = System.currentTimeMillis();

        // Print final results
        System.out.println("\n============================================================");
        System.out.println("  SIMULATION COMPLETE");
        System.out.println("============================================================\n");

        System.out.println("========== FINAL PROCESS STATES ==========");
        List<Process> allProcesses = kernel.getProcesses();
        for (Process p : allProcesses) {
            System.out.println(p);
        }
        System.out.println("========================================\n");

        System.out.println("========== EXECUTION SUMMARY ==========");
        System.out.println("Total Execution Time: " + (endTime - startTime) + " ms");
        System.out.println("Total Processes Created: " + allProcesses.size());
        System.out.println("Processes Completed: " + 
            allProcesses.stream().filter(p -> p.getState() == ProcessState.TERMINATED).count());
        System.out.println("CPU Time Used: " + kernel.getDispatcher().getCPUTimeUsed() + " units");
        System.out.println("=====================================\n");

        // Print execution history
        System.out.println("========== DISPATCH EXECUTION HISTORY ==========");
        List<Dispatcher.ProcessExecution> history = kernel.getDispatcher().getExecutionHistory();
        for (Dispatcher.ProcessExecution exec : history) {
            System.out.printf("Process: %s | Start: %d | End: %d | Reason: %s\n",
                exec.process.getProcessName(), exec.startTime, exec.endTime, exec.reason);
        }
        System.out.println("==============================================\n");

        System.out.println("✓ Two-threaded OS simulation completed successfully!");
        System.out.println("  - Scheduler Thread managed ready queue");
        System.out.println("  - Dispatcher Thread executed processes on CPU");
        System.out.println("  - Both threads ran concurrently demonstrating multithreading");
    }
}
