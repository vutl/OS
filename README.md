# OS Kernel Simulation - Multi-threaded Scheduling & Dispatching

A comprehensive Java implementation demonstrating operating system kernel operations including process scheduling, CPU dispatching, context switching, and multi-threaded execution on multicore systems.

## 📋 Project Overview

This project simulates core OS kernel functionalities:
- **Process Management**: Creation, state transitions, priority levels
- **Scheduling**: Priority-based queue management with time quantum fairness
- **Dispatching**: CPU allocation and preemptive context switching
- **Multi-threading**: Two concurrent kernel threads (Scheduler & Dispatcher) demonstrating thread synchronization
- **Thread Support**: User-level threads within processes

## 🏗️ Project Structure

```
/Users/vutl2004/Documents/OS/
├── src/
│   ├── process/
│   │   ├── ProcessState.java       # Process state enumeration
│   │   ├── Process.java            # Process class with lifecycle
│   │   └── Thread.java             # User-level thread representation
│   │
│   ├── scheduling/
│   │   └── Scheduler.java          # Priority-based scheduler implementation
│   │
│   ├── dispatcher/
│   │   └── Dispatcher.java         # CPU dispatcher & context switching
│   │
│   ├── kernel/
│   │   └── OSKernel.java           # Main kernel coordinator
│   │
│   └── OSSimulation.java           # Main simulation entry point
│
├── bin/                            # Compiled .class files
│
├── docs/
│   └── SOLARIS_DOCUMENTATION.md    # Detailed SOLARIS process/thread execution
│
├── CLASS_DIAGRAM.txt               # Architecture & class relationships
├── dashboard.html                  # Interactive visualization dashboard
├── README.md                        # This file
└── SETUP.md                         # Detailed setup instructions

```

## 🚀 Quick Start

### Prerequisites

- **JDK 17 or newer** (Tested with JDK 25.0.1)
- **macOS/Linux/Windows** with Java installed

### Setup (macOS with JDK 25)

```bash
# Set up Java environment
export JAVA_HOME=/Users/vutl2004/Downloads/jdk-25.0.1.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"

# Verify Java installation
java -version
javac -version

# Navigate to project
cd /Users/vutl2004/Documents/OS

# Create bin directory if needed
mkdir -p bin

# Compile all source files
javac -d bin \
  src/process/ProcessState.java \
  src/process/Process.java \
  src/process/Thread.java \
  src/scheduling/Scheduler.java \
  src/dispatcher/Dispatcher.java \
  src/kernel/OSKernel.java \
  src/OSSimulation.java

# Run the simulation
java -cp bin OSSimulation
```

## 📊 Running the Simulation

### Command Line Execution

```bash
# From /Users/vutl2004/Documents/OS directory
java -cp bin OSSimulation
```

**Expected Output**:
```
╔════════════════════════════════════════════════════════════╗
║        OS KERNEL SIMULATION - Multi-threaded Demo          ║
║     Demonstrating Scheduling, Dispatching & Threading      ║
╚════════════════════════════════════════════════════════════╝

========== CREATING PROCESSES ==========
Created 4 processes with different priorities
[PID: 1000, Name: Process-A, Priority: 8, State: READY, Remaining: 12/12]
[PID: 1001, Name: Process-B, Priority: 5, State: READY, Remaining: 8/8]
[PID: 1002, Name: Process-C, Priority: 9, State: READY, Remaining: 6/6]
[PID: 1003, Name: Process-D, Priority: 3, State: READY, Remaining: 10/10]
...

[SCHEDULER THREAD] Started - PID: 25
[DISPATCHER THREAD] Started - PID: 26

[T=0] DISPATCH: Process Process-C (PID: 1002) assigned to CPU at time 0
[T=1] EXECUTE: Process-C executing (Remaining: 5)
...

========== SIMULATION COMPLETE ==========
```

## 🎨 Interactive Dashboard

Open the HTML visualization in a web browser:

```bash
open /Users/vutl2004/Documents/OS/dashboard.html
```

Or use the file browser to navigate and open `dashboard.html` in your default browser.

**Dashboard Features**:
- Real-time system statistics
- Process status overview
- Thread information display
- CPU execution timeline
- Architecture diagrams
- Key features summary

## 📚 Documentation

### 1. **CLASS_DIAGRAM.txt**
   - Complete class architecture
   - Data structures and relationships
   - Scheduling algorithms overview
   - Dispatcher operations flow
   - Multi-threading model diagram

### 2. **SOLARIS_DOCUMENTATION.md**
   - Detailed process model explanation
   - Thread model and LWPs (Lightweight Processes)
   - Multicore execution model
   - Real-world SOLARIS example with 16-core system
   - Synchronization mechanisms
   - Performance considerations

## 🔍 Key Components Explained

### Process Class (`src/process/Process.java`)
```java
public class Process {
    - Unique Process ID (auto-incrementing)
    - Priority level (1-10, higher = more important)
    - State management (NEW → READY → RUNNING → BLOCKED → TERMINATED)
    - Burst time tracking for CPU scheduling
    - Timing metrics (wait time, turnaround time)
}
```

### Thread Class (`src/process/Thread.java`)
```java
public class Thread {
    - Thread ID (unique within system)
    - Parent Process ID (association)
    - Priority level (LOW, NORMAL, HIGH)
    - Independent execution time tracking
    - State management
}
```

### Scheduler Class (`src/scheduling/Scheduler.java`)
```java
Supports multiple scheduling algorithms:
1. ROUND_ROBIN - Fair time-slice distribution
2. PRIORITY_BASED - Highest priority first (used in simulation)
3. MIXED - Priority with round-robin fairness

Key methods:
- addProcess(Process) - Add to ready queue
- getNextProcess() - Select highest priority ready process
- requeueProcess(Process) - Move process back to queue on preemption
- hasReadyProcesses() - Check if queue non-empty
```

### Dispatcher Class (`src/dispatcher/Dispatcher.java`)
```java
CPU allocation and context switching:
- dispatch() - Select next process from scheduler
- executeTimeUnit() - Run current process for 1 time unit
- runCycle() - Complete scheduler + dispatcher cycle
- Context switching on time quantum expiration
- Tracks execution history with timestamps
```

### OSKernel Class (`src/kernel/OSKernel.java`)
```java
Main coordinator:
- Manages Scheduler and Dispatcher
- Process creation and initialization
- Thread creation within processes
- Simulation orchestration
- Statistics collection and reporting
```

## 🧵 Multi-threaded Execution

### Scheduler Thread
- Monitors ready queue continuously
- Logs process state changes
- Tracks queue statistics
- Runs concurrently with Dispatcher

### Dispatcher Thread
- Executes kernel cycles (dispatch + execute)
- Manages CPU time allocation
- Handles context switching
- Maintains execution history
- Synchronizes with Scheduler via shared lock

**Synchronization**: Both threads use `synchronized` blocks with a shared `lockObject` to prevent race conditions.

## 📊 Simulation Results

The simulation creates 4 processes with different priorities:

| Process | Priority | Burst Time | Status |
|---------|----------|-----------|--------|
| Process-A | 8 | 12 units | READY |
| Process-B | 5 | 8 units | READY |
| Process-C | 9 | 6 units | RUNNING |
| Process-D | 3 | 10 units | READY |

**Key Metrics**:
- Scheduling Algorithm: Priority-Based
- Time Quantum: 3 units
- Total Execution Time: ~7.7 seconds
- CPU Cycles Used: 50 units
- Context Switches: 1 (time quantum expiration)

## 🎯 Concepts Demonstrated

### 1. Process States
- State transitions based on scheduler decisions
- Process lifecycle management
- Status tracking and reporting

### 2. Scheduling Algorithms
- Priority-based selection
- Ready queue management
- Fairness enforcement via time quantum

### 3. Dispatching
- CPU allocation to processes
- Context switching mechanism
- Process preemption handling

### 4. Multi-threading
- Concurrent thread execution
- Thread synchronization (locks)
- Shared resource management
- Thread communication

### 5. Thread Management
- User-level threads within processes
- Thread-specific execution context
- Priority-based thread scheduling

## ⚙️ Configuration Options

To modify simulation parameters, edit `src/OSSimulation.java`:

```java
// Line: Create kernel with priority-based scheduling
kernel = new OSKernel(
    Scheduler.SchedulingAlgorithm.PRIORITY_BASED,  // Change algorithm here
    3  // Time quantum (change to adjust preemption frequency)
);

// Change number/priority of processes
Process p1 = kernel.createProcess("Process-A", 8, 12);  // priority, burst time
Process p2 = kernel.createProcess("Process-B", 5, 8);
// ... add/modify more processes
```

## 🔧 Troubleshooting

### "Cannot find symbol" Compilation Errors
**Solution**: Ensure all source files are in correct directories:
```bash
ls -la src/process/
ls -la src/scheduling/
ls -la src/dispatcher/
ls -la src/kernel/
```

### Java Version Mismatch
**Solution**: Check Java version matches JDK 17+:
```bash
java -version  # Should show version 17+
javac -version  # Should match java version
```

### Module Issues (Java 9+)
**Solution**: Use `-cp` (classpath) instead of module system for this project:
```bash
javac -d bin -cp bin src/**/*.java  # Include bin in classpath
```

## 📖 Learning Resources

### Key Concepts
1. **Process Model**: How OS abstracts program execution
2. **Scheduling**: Algorithm for selecting which process runs next
3. **Dispatching**: Mechanism for allocating CPU to processes
4. **Context Switching**: Saving/restoring process state
5. **Synchronization**: Preventing race conditions in multi-threaded code

### Further Reading
- See `docs/SOLARIS_DOCUMENTATION.md` for production OS details
- See `CLASS_DIAGRAM.txt` for architecture overview
- Review console output for execution trace

## 🚀 Extending the Simulation

### Add More Scheduling Algorithms
1. Implement new scheduling logic in `Scheduler.java`
2. Add `SchedulingAlgorithm` enum value
3. Update `getNextProcess()` method to handle new algorithm

### Add I/O Operations
1. Modify `Process` to track I/O events
2. Add `BLOCKED` state handling in Dispatcher
3. Implement event-driven wake-up mechanism

### Add Memory Management
1. Extend `Process` with memory address space
2. Track page tables, TLB
3. Simulate page faults and swapping

## 📞 Support & Questions

For detailed operation explanations, see:
- **Class Architecture**: `CLASS_DIAGRAM.txt`
- **OS Concepts**: `docs/SOLARIS_DOCUMENTATION.md`
- **Source Code Comments**: Each Java file contains detailed javadoc

## 📄 License

This is an educational project demonstrating OS kernel concepts.

---

**Created**: January 17, 2026
**Java Version Required**: JDK 17+
**Status**: ✅ Complete and Executable

**Key Achievement**: Demonstrates fundamental OS operations with two concurrent threads executing and synchronizing in real-time, showing practical multi-threading concepts in action.
