package process;

/**
 * Enumeration for process states in the operating system
 * Represents the various states a process can be in during its lifecycle
 */
public enum ProcessState {
    NEW("NEW"),               // Process has been created but not yet admitted to ready queue
    READY("READY"),           // Process is ready to run, waiting for CPU allocation
    RUNNING("RUNNING"),       // Process is currently executing on CPU
    BLOCKED("BLOCKED"),       // Process is waiting for I/O or other resources
    TERMINATED("TERMINATED"); // Process has finished execution

    private final String displayName;

    ProcessState(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
