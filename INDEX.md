# OS Kernel Simulation - Complete Project Index

## 📑 Documentation Guide

### For Quick Start (Start Here!)
1. **[QUICK_REFERENCE.md](QUICK_REFERENCE.md)** - 5-minute quick start
   - How to compile and run
   - Key commands to remember
   - Understanding the output

### For Complete Setup
2. **[README.md](README.md)** - Comprehensive guide
   - Full project description
   - Detailed setup instructions
   - Component descriptions
   - Configuration options

### For Learning OS Concepts
3. **[docs/SOLARIS_DOCUMENTATION.md](docs/SOLARIS_DOCUMENTATION.md)** - Advanced OS guide
   - SOLARIS process model (detailed)
   - Thread model and multicore execution
   - Real-world 16-core system example
   - Synchronization mechanisms
   - Performance considerations

### For Understanding Architecture
4. **[CLASS_DIAGRAM.txt](CLASS_DIAGRAM.txt)** - System architecture
   - Complete class diagrams
   - Method and field definitions
   - Data structure representations
   - Scheduling algorithms
   - State transitions

### For Project Overview
5. **[SUMMARY.md](SUMMARY.md)** - Project completion report
   - All deliverables listed
   - Requirements fulfilled checklist
   - Execution results
   - Project statistics

---

## 🎯 Source Code Structure

### Process Management (`src/process/`)

#### ProcessState.java
```
Enumeration defining process lifecycle states:
- NEW: Process created, not yet in scheduler
- READY: Process ready to execute
- RUNNING: Currently executing on CPU
- BLOCKED: Waiting for I/O or resources
- TERMINATED: Process finished
```

#### Process.java (180 lines)
```
Core process class with:
- Unique PID (auto-incrementing)
- Priority levels (1-10)
- Execution time tracking
- State management
- Timing metrics (wait time, turnaround time)
```

#### Thread.java (155 lines)
```
User-level thread representation:
- Unique ThreadID
- Parent process association
- Thread priority (LOW, NORMAL, HIGH)
- Execution time independent of process
- State management
```

### Scheduling Module (`src/scheduling/`)

#### Scheduler.java (205 lines)
```
Process scheduling implementation:
- Priority-based ready queue
- Multiple scheduling algorithms supported
- Queue management (add, get next, requeue)
- Process state tracking
- Time quantum enforcement
```

**Key Methods:**
- `addProcess(Process)` - Add to ready queue
- `getNextProcess()` - Select highest priority ready process
- `requeueProcess(Process)` - Move back on preemption
- `hasReadyProcesses()` - Check queue status

### Dispatching Module (`src/dispatcher/`)

#### Dispatcher.java (215 lines)
```
CPU allocation and context switching:
- Process selection and dispatch
- Time unit execution simulation
- Context switching on preemption
- Execution history tracking
- Statistics collection
```

**Key Methods:**
- `dispatch()` - Select next process, handle switching
- `executeTimeUnit()` - Execute for 1 time unit
- `runCycle()` - Complete dispatcher cycle
- `getCurrentRunningProcess()` - Get currently executing process

### Kernel Module (`src/kernel/`)

#### OSKernel.java (185 lines)
```
Main kernel coordinator:
- Manages scheduler and dispatcher
- Process and thread creation
- Kernel cycle execution
- Statistics collection and reporting
- Simulation orchestration
```

### Simulation Entry Point (`src/`)

#### OSSimulation.java (392 lines)
```
Main simulation with two concurrent threads:

SchedulerThread:
- Monitors ready queue
- Logs process states (50 cycles)
- Runs concurrently with dispatcher

DispatcherThread:
- Executes kernel cycles
- Manages CPU time (50 cycles)
- Handles context switching

Synchronization:
- Both threads use synchronized blocks
- Shared lockObject prevents race conditions
```

---

## 📊 Data Flow Diagram

```
OSSimulation.main()
    ↓
├─ Create OSKernel
│  ├─ Instantiate Scheduler (priority-based)
│  ├─ Instantiate Dispatcher
│  └─ Ready for operation
│
├─ Create Processes (4 total)
│  ├─ Process-C: Priority 9, Burst 6
│  ├─ Process-A: Priority 8, Burst 12
│  ├─ Process-B: Priority 5, Burst 8
│  └─ Process-D: Priority 3, Burst 10
│
├─ Add processes to Scheduler
│  └─ Ready queue populated
│
├─ Create SchedulerThread
│  └─ Starts monitoring queue
│
├─ Create DispatcherThread
│  └─ Starts executing cycles
│
├─ Both threads run concurrently (50 cycles)
│  ├─ Synchronized execution via shared lock
│  ├─ Scheduler logs queue status
│  └─ Dispatcher executes processes
│
└─ Print final statistics
   ├─ Process states
   ├─ Timing information
   ├─ Thread statistics
   └─ Execution history
```

---

## 🔄 Execution Flow (Detailed)

### Cycle Structure (Each Iteration)

```
┌─ Synchronized Block ──────────────────────┐
│                                            │
│ 1. Dispatcher.dispatch()                  │
│    ├─ Check if time quantum expired       │
│    ├─ Save current process context        │
│    ├─ Requeue if not completed            │
│    ├─ Select next process from queue      │
│    └─ Transition to RUNNING state         │
│                                            │
│ 2. Dispatcher.executeTimeUnit()           │
│    ├─ Reduce remaining burst time         │
│    ├─ Increment time counters             │
│    └─ Update statistics                   │
│                                            │
│ 3. Scheduler (concurrent) logs status:    │
│    ├─ Ready queue size                    │
│    ├─ Process state counts                │
│    └─ Current time                        │
│                                            │
└────────────────────────────────────────────┘
```

### Timeline Example (First 5 Cycles)

```
Cycle 1 (T=0):
  Scheduler: Queue size 4, Ready 4, Running 0, Terminated 0
  Dispatcher: DISPATCH Process-C (priority 9, highest)
  Dispatcher: EXECUTE Process-C for 1 unit (Remaining: 5/6)

Cycle 2 (T=1):
  Scheduler: Queue size 3, Ready 3, Running 1, Terminated 0
  Dispatcher: EXECUTE Process-C for 1 unit (Remaining: 4/6)

Cycle 3 (T=2):
  Scheduler: Queue size 3, Ready 3, Running 1, Terminated 0
  Dispatcher: EXECUTE Process-C for 1 unit (Remaining: 3/6)

Cycle 4 (T=3):
  Scheduler: Queue size 3, Ready 3, Running 1, Terminated 0
  Dispatcher: TIME QUANTUM EXPIRED!
  Dispatcher: CONTEXT SWITCH - Save Process-C, requeue
  Dispatcher: DISPATCH Process-A (priority 8, next highest)
  Dispatcher: EXECUTE Process-A for 1 unit

Cycle 5 (T=4):
  Scheduler: Queue size 3, Ready 3, Running 1, Terminated 0
  Dispatcher: EXECUTE Process-A for 1 unit
  ...
```

---

## 🧵 Multi-threading Architecture

### Thread Synchronization Model

```
Main Thread
    │
    ├─ Create & Start SchedulerThread
    │       │
    │       └─ Acquire lock
    │          ├─ Monitor queue
    │          ├─ Log statistics
    │          └─ Release lock
    │
    ├─ Create & Start DispatcherThread
    │       │
    │       └─ Acquire lock
    │          ├─ dispatch()
    │          ├─ executeTimeUnit()
    │          └─ Release lock
    │
    ├─ Both threads run in parallel
    │   └─ Serialized access via synchronized block
    │
    └─ join() both threads
       └─ Wait for completion
```

### Synchronization Details

- **Lock**: `Object lockObject = new Object()`
- **Protected**: All kernel state modifications
- **Access Pattern**: 
  ```java
  synchronized(lockObject) {
      // Only one thread at a time
      // Access to Scheduler and Dispatcher
  }
  ```

---

## 📈 Key Metrics & Statistics

### Process Metrics
- PID: Unique process identifier
- Priority: 1-10 (higher = more important)
- State: Current process state
- Burst Time: Total and remaining execution time
- Wait Time: Time spent waiting for CPU
- Turnaround Time: Total time from start to completion

### Thread Metrics
- ThreadID: Unique thread identifier
- ParentPID: Associated process
- Priority: LOW, NORMAL, or HIGH
- Execution Time: Total and remaining
- State: Current thread state

### System Metrics
- CPU Time Used: Total CPU cycles executed
- Context Switches: Number of preemptions
- Ready Queue Size: Processes waiting for CPU
- Scheduling Cycles: Iterations completed
- Dispatch Cycles: CPU allocations made

---

## 🎓 Learning Path

### Beginner (Start Here)
1. Read QUICK_REFERENCE.md
2. Run the simulation
3. View dashboard.html
4. Observe the output

### Intermediate
1. Study CLASS_DIAGRAM.txt
2. Review Process.java and Thread.java
3. Understand Scheduler.java
4. Learn Dispatcher.java

### Advanced
1. Read SOLARIS_DOCUMENTATION.md
2. Examine OSKernel.java
3. Study OSSimulation.java threads
4. Modify and experiment

### Expert
1. Modify scheduling algorithms
2. Add new process types
3. Implement I/O blocking
4. Add memory management
5. Create new features

---

## 🔧 Configuration & Customization

### Modify Scheduling Algorithm
In `src/OSSimulation.java` (line ~230):
```java
// Change from PRIORITY_BASED to other options:
kernel = new OSKernel(
    Scheduler.SchedulingAlgorithm.PRIORITY_BASED,  // ← Change here
    3  // Time quantum
);
```

### Adjust Time Quantum
```java
kernel = new OSKernel(
    Scheduler.SchedulingAlgorithm.PRIORITY_BASED,
    5  // ← Change quantum from 3 to 5 (or any value)
);
```

### Add/Modify Processes
```java
Process p1 = kernel.createProcess(
    "Process-A",  // Name
    8,            // Priority (1-10)
    12            // Burst time (units)
);
```

### Change Thread Sleep Times
In `SchedulerThread.run()` (line ~70):
```java
java.lang.Thread.sleep(100);  // ← Change from 100ms to desired value
```

In `DispatcherThread.run()` (line ~120):
```java
java.lang.Thread.sleep(150);  // ← Change from 150ms to desired value
```

---

## 📊 Visualization Guide

### Dashboard Features
- **System Overview**: Key statistics at a glance
- **Process Status**: Real-time state of all processes
- **Thread Information**: Thread metrics and priorities
- **Process Details**: Detailed table with all info
- **CPU Timeline**: Execution sequence visualization
- **Architecture Diagram**: System component flow
- **Execution Statistics**: Aggregated metrics

### How to Open
```bash
open /Users/vutl2004/Documents/OS/dashboard.html
```

---

## 🚀 Common Tasks

### Run Simulation
```bash
cd /Users/vutl2004/Documents/OS
export JAVA_HOME=/Users/vutl2004/Downloads/jdk-25.0.1.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
java -cp bin OSSimulation
```

### Recompile After Changes
```bash
javac -d bin src/process/*.java src/scheduling/*.java \
      src/dispatcher/*.java src/kernel/*.java src/OSSimulation.java
```

### View Documentation
```bash
open README.md
open QUICK_REFERENCE.md
open docs/SOLARIS_DOCUMENTATION.md
open CLASS_DIAGRAM.txt
```

### View HTML Dashboard
```bash
open dashboard.html
```

### Edit Source Code
```bash
open -a "VS Code" src/
```

---

## ✅ Verification Checklist

- [ ] JDK 25 installed and configured
- [ ] JAVA_HOME environment variable set
- [ ] Project directory exists: `/Users/vutl2004/Documents/OS`
- [ ] All Java files compiled successfully
- [ ] bin/ directory contains .class files
- [ ] Simulation runs without errors
- [ ] Two threads execute concurrently
- [ ] Dashboard.html displays correctly
- [ ] Documentation files readable
- [ ] SOLARIS_DOCUMENTATION.md provides detailed explanation

---

## 📞 Troubleshooting Quick Links

| Issue | Solution |
|-------|----------|
| "Cannot find Java" | Check JAVA_HOME and PATH |
| Compilation errors | Verify all source files exist |
| Runtime errors | Check class path (-cp bin) |
| Threads not running | Verify Java version ≥ 17 |
| Dashboard won't open | Use correct file path or drag to browser |

---

## 📚 Document Relationships

```
                     SUMMARY.md
                   (Project Report)
                        ↑
        ┌───────────────┼───────────────┐
        │               │               │
     README.md      QUICK_REF.md     CLASS_DIAG.txt
   (Complete      (Quick Start)    (Architecture)
   Documentation)      │
        │              │
        └──────────────┼──────────────┐
                       │              │
                  dashboard.html   SOLARIS_DOC.md
                (Visualization)   (OS Concepts)
```

---

**Last Updated**: January 17, 2026  
**Status**: ✅ Complete and Verified  
**Ready for**: Learning, Teaching, and Development
