package dispatcher;

import process.Process;
import process.ProcessState;
import scheduling.Scheduler;
import java.util.*;

// CPU dispatcher: picks process, executes, handles context switch
public class Dispatcher {
    private Scheduler scheduler;
    private Process currentRunningProcess;
    private int timeSliceCounter;
    private int cpuTimeUsed;
    private int currentSliceStartTime;
    private List<String> dispatchLog;
    private List<ProcessExecution> executionHistory;

    public static class ProcessExecution {
        public Process process;
        public int startTime;
        public int endTime;
        public String reason;

        public ProcessExecution(Process process, int startTime, int endTime, String reason) {
            this.process = process;
            this.startTime = startTime;
            this.endTime = endTime;
            this.reason = reason;
        }
    }

    // init dispatcher
    public Dispatcher(Scheduler scheduler) {
        this.scheduler = scheduler;
        this.currentRunningProcess = null;
        this.timeSliceCounter = 0;
        this.cpuTimeUsed = 0;
        this.currentSliceStartTime = -1;
        this.dispatchLog = new ArrayList<>();
        this.executionHistory = new ArrayList<>();
    }

    // pick next process, handle switch/terminate
    public synchronized void dispatch() {
        // Check if current process needs to be suspended or terminated
        if (currentRunningProcess != null) {
            // Check if process is complete
            if (currentRunningProcess.isComplete()) {
                currentRunningProcess.setState(ProcessState.TERMINATED);
                currentRunningProcess.setEndTime(cpuTimeUsed);
                log("PROCESS TERMINATED: " + currentRunningProcess.getProcessName() + 
                    " completed execution at time " + cpuTimeUsed);
                executionHistory.add(new ProcessExecution(
                    currentRunningProcess,
                    currentSliceStartTime,
                    cpuTimeUsed,
                    "COMPLETED"
                ));
                currentRunningProcess = null;
                timeSliceCounter = 0;
                currentSliceStartTime = -1;
            }
            // Check if time quantum expired
            else if (timeSliceCounter >= scheduler.getTimeQuantum()) {
                log("CONTEXT SWITCH: Suspending " + currentRunningProcess.getProcessName() + 
                    " (Time quantum expired)");
                currentRunningProcess.setState(ProcessState.READY);
                scheduler.requeueProcess(currentRunningProcess);
                executionHistory.add(new ProcessExecution(
                    currentRunningProcess,
                    currentSliceStartTime,
                    cpuTimeUsed,
                    "TIME_QUANTUM_EXPIRED"
                ));
                currentRunningProcess = null;
                timeSliceCounter = 0;
                currentSliceStartTime = -1;
            }
        }

        // Select next process from ready queue
        if (currentRunningProcess == null && scheduler.hasReadyProcesses()) {
            currentRunningProcess = scheduler.getNextProcess();
            if (currentRunningProcess != null) {
                currentRunningProcess.setState(ProcessState.RUNNING);
                if (currentRunningProcess.getStartTime() < 0) {
                    currentRunningProcess.setStartTime(cpuTimeUsed);
                }
                timeSliceCounter = 0;
                currentSliceStartTime = cpuTimeUsed;
                log("DISPATCH: Process " + currentRunningProcess.getProcessName() + 
                    " (PID: " + currentRunningProcess.getPID() + ") assigned to CPU at time " + cpuTimeUsed);
            }
        }
    }

    // run process for 1 unit
    public synchronized void executeTimeUnit() {
        if (currentRunningProcess != null && currentRunningProcess.getState() == ProcessState.RUNNING) {
            currentRunningProcess.reduceRemainingTime(1);
            timeSliceCounter++;
            cpuTimeUsed++;
            // Keep scheduler's logical time aligned to CPU time.
            scheduler.setCurrentTime(cpuTimeUsed);
            
            log("EXECUTE: " + currentRunningProcess.getProcessName() + 
                " executing (Remaining: " + currentRunningProcess.getRemainingBurstTime() + ")");
        } else {
            cpuTimeUsed++;
            scheduler.setCurrentTime(cpuTimeUsed);
        }
    }

    // dispatch + execute
    public synchronized void runCycle() {
        dispatch();
        executeTimeUnit();
    }

    /**
     * Get current running process
     */
    public synchronized Process getCurrentRunningProcess() {
        return currentRunningProcess;
    }

    /**
     * Check if CPU is idle
     */
    public synchronized boolean isCPUIdle() {
        return currentRunningProcess == null;
    }

    /**
     * Get the total CPU time used
     */
    public synchronized int getCPUTimeUsed() {
        return cpuTimeUsed;
    }

    /**
     * Get execution history
     */
    public synchronized List<ProcessExecution> getExecutionHistory() {
        return new ArrayList<>(executionHistory);
    }

    /**
     * Log dispatcher activities
     */
    private void log(String message) {
        String logEntry = "[T=" + cpuTimeUsed + "] " + message;
        dispatchLog.add(logEntry);
        System.out.println(logEntry);
    }

    /**
     * Get dispatch log
     */
    public synchronized List<String> getDispatchLog() {
        return new ArrayList<>(dispatchLog);
    }

    /**
     * Clear logs
     */
    public synchronized void clearLogs() {
        dispatchLog.clear();
    }

    /**
     * Print scheduling statistics
     */
    public synchronized void printStatistics() {
        System.out.println("\n========== DISPATCHER STATISTICS ==========");
        System.out.println("Total CPU Time Used: " + cpuTimeUsed);
        System.out.println("Total Processes Executed: " + executionHistory.size());
        System.out.println("Current Running Process: " + 
            (currentRunningProcess != null ? currentRunningProcess.getProcessName() : "None"));
        System.out.println("=========================================\n");
    }
}
