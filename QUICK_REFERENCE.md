# OS Kernel Simulation - Quick Reference Guide

## 🎯 What This Project Does

Simulates a **real operating system kernel** with:
- ✅ Process scheduling (priority-based)
- ✅ CPU dispatching with preemptive context switching
- ✅ Multi-threading (2 concurrent kernel threads)
- ✅ Process state management (NEW → READY → RUNNING → BLOCKED → TERMINATED)
- ✅ Time quantum enforcement for fair CPU allocation
- ✅ Thread synchronization with locks

## 🖥️ Quick Commands

### Setup (One-time)
```bash
# Set Java 25
export JAVA_HOME=/Users/vutl2004/Downloads/jdk-25.0.1.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"

# Go to project
cd /Users/vutl2004/Documents/OS
mkdir -p bin
```

### Compile
```bash
javac -d bin \
  src/process/ProcessState.java \
  src/process/Process.java \
  src/process/Thread.java \
  src/scheduling/Scheduler.java \
  src/dispatcher/Dispatcher.java \
  src/kernel/OSKernel.java \
  src/OSSimulation.java
```

### Run
```bash
java -cp bin OSSimulation
```

### View Dashboard
```bash
open /Users/vutl2004/Documents/OS/dashboard.html
```

## 📂 File Organization

```
OS/
├── src/process/          → Process & Thread classes
├── src/scheduling/       → Scheduler (priority queue)
├── src/dispatcher/       → Dispatcher (CPU allocation)
├── src/kernel/           → OSKernel (main coordinator)
├── bin/                  → Compiled Java files
├── docs/                 → SOLARIS documentation
├── CLASS_DIAGRAM.txt     → Architecture diagram
├── README.md             → Full documentation
├── dashboard.html        → Visual dashboard
└── QUICK_REFERENCE.md    → This file
```

## 🎓 Core Concepts

### 1. Process Scheduling
- **What**: Scheduler picks which process should run next
- **How**: Priority queue (higher priority = selected first)
- **Why**: Fair CPU allocation, responsiveness for important tasks
- **Code**: `src/scheduling/Scheduler.java`

### 2. CPU Dispatching
- **What**: Dispatcher assigns process to CPU core
- **How**: Allocates CPU time, handles context switches
- **Why**: Actual execution of processes
- **Code**: `src/dispatcher/Dispatcher.java`

### 3. Time Quantum
- **What**: Maximum time slice a process gets (3 units)
- **How**: If process doesn't finish in 3 units, it's preempted
- **Why**: Ensures no process monopolizes CPU
- **Result**: Fair scheduling, responsiveness

### 4. Context Switching
- **What**: Saving one process, loading another
- **How**: Save registers/state → Load next process's state
- **When**: Time quantum expired, process blocked on I/O
- **Cost**: ~1-10 microseconds per switch

### 5. Multi-threading
- **What**: Two concurrent threads within simulation
  - **Scheduler Thread**: Monitors ready queue
  - **Dispatcher Thread**: Executes processes
- **How**: Both run simultaneously, synchronized with locks
- **Why**: Demonstrates real kernel architecture

## 📊 Simulation Run Trace

```
1. OSSimulation.main() starts
   ↓
2. Create OSKernel with Priority-Based scheduling
   ↓
3. Create 4 processes:
   - Process-C (Priority 9) ← Will run first (highest)
   - Process-A (Priority 8)
   - Process-B (Priority 5)
   - Process-D (Priority 3)
   ↓
4. Start SchedulerThread
   └─ Monitors queue, prints status every cycle
   ↓
5. Start DispatcherThread
   └─ Runs kernel cycles (dispatch + execute)
   ↓
6. Both threads synchronize via shared lock
   ├─ Dispatcher: [dispatch → select highest priority] [execute 1 unit]
   ├─ Scheduler: [log queue status]
   └─ Repeat 50 times
   ↓
7. Threads complete after 50 cycles
   ↓
8. Print final statistics
   └─ Show which processes completed, timing info
```

## 🔄 Example Execution Flow (First 5 Cycles)

```
Cycle 1:
  Scheduler: "Ready Queue Size: 4, Ready: 4, Running: 0, Terminated: 0"
  Dispatcher: dispatch() → selects Process-C (priority 9)
              executeTimeUnit() → Process-C runs for 1 unit

Cycle 2:
  Scheduler: "Ready Queue Size: 3, Ready: 3, Running: 1, Terminated: 0"
  Dispatcher: dispatch() → Process-C continues (time counter = 2)
              executeTimeUnit() → Remaining time: 5 → 4

Cycle 3:
  Scheduler: "Ready Queue Size: 3, Ready: 3, Running: 1, Terminated: 0"
  Dispatcher: dispatch() → Process-C continues (time counter = 3)
              executeTimeUnit() → Remaining time: 4 → 3

Cycle 4:
  Scheduler: "Ready Queue Size: 3, Ready: 3, Running: 1, Terminated: 0"
  Dispatcher: CONTEXT SWITCH! Time quantum (3) expired
              dispatch() → Save Process-C, requeue it
                       → Select Process-A (priority 8, next highest)
              executeTimeUnit() → Process-A runs

Cycle 5:
  Scheduler: "Ready Queue Size: 3, Ready: 3, Running: 1, Terminated: 0"
  Dispatcher: dispatch() → Process-A continues
              executeTimeUnit() → Process-A remaining time decreases
```

## 📈 Key Statistics

**After 50 Simulation Cycles:**

```
Total Simulation Time: 50 units
Total CPU Time Used: 50 units
Processes Created: 4
Processes Completed: 0 (simulation stopped before all finish)
Context Switches: 1 (when Process-C's time quantum expired)

Scheduler Thread:
  - Completed 50 monitoring cycles
  - Tracked queue status transitions

Dispatcher Thread:
  - Completed 50 execution cycles
  - Handled 1 context switch
  - Maintained execution history
```

## 🧵 Understanding Multi-threading

### Scheduler Thread (java.lang.Thread)
```
┌─────────────────────────────────┐
│ SchedulerThread.run()           │
├─────────────────────────────────┤
│ while (cycles < 50) {           │
│   synchronized(lock) {          │
│     // Log queue status         │
│     // Check process states     │
│   }                             │
│   sleep(100ms)                  │
│   cycle++                       │
│ }                               │
└─────────────────────────────────┘
```

### Dispatcher Thread (java.lang.Thread)
```
┌─────────────────────────────────┐
│ DispatcherThread.run()          │
├─────────────────────────────────┤
│ while (cycles < 50) {           │
│   synchronized(lock) {          │
│     dispatcher.runCycle()       │
│     // dispatch() + execute()   │
│   }                             │
│   sleep(150ms)                  │
│   cycle++                       │
│ }                               │
└─────────────────────────────────┘
```

**Key**: Both threads use `synchronized` blocks on shared `lockObject` to prevent race conditions when accessing kernel state.

## 🔍 How to Read the Output

### Process Status Line
```
[PID: 1002, Name: Process-C, Priority: 9, State: RUNNING, Remaining: 3/6]
          ↑                      ↑           ↑                  ↑
       Process ID             Name      Current State     Time left/Total
```

### Dispatch Event
```
[T=0] DISPATCH: Process Process-C (PID: 1002) assigned to CPU at time 0
 ↑    ↑                           ↑
Time  Event Type                Process Info
```

### Execute Event
```
[T=1] EXECUTE: Process-C executing (Remaining: 5)
 ↑    ↑                              ↑
Time  Event Type                How many units left

```

### Context Switch Event
```
[T=3] CONTEXT SWITCH: Suspending Process-C (Time quantum expired)
 ↑                                                 ↑
Time                                          Why switched
```

## 💡 Key Insights

### Why Scheduling Matters
- **Without**: One process could run forever, others starve
- **With**: Fair CPU allocation, all processes make progress
- **Benefit**: Better responsiveness, fairness, throughput

### Why Preemption Matters
- **Without**: Long processes monopolize CPU
- **With**: Time quantum enforcement ensures fairness
- **Benefit**: Interactive response, prevents starvation

### Why Multi-threading Matters
- **Without**: Sequential simulation of kernel operations
- **With**: Concurrent scheduler and dispatcher mirror real OS
- **Benefit**: Demonstrates true parallelism, synchronization challenges

## 🎯 Comparison with Real OS

### SOLARIS (Production OS)
- Hundreds of processes/threads
- 16-256 CPU cores
- Complex locking, futex
- Multiple scheduling classes
- Real I/O, memory management

### This Simulation
- 4 processes, 2 kernel threads
- Single CPU execution (sequential)
- Simple mutex locks
- Single scheduling class
- Simplified I/O (no blocking)

✅ **But** demonstrates all **core concepts** realistically!

## 🚀 What to Try

### 1. Change Time Quantum
In `OSSimulation.java`, change:
```java
kernel = new OSKernel(Scheduler.SchedulingAlgorithm.PRIORITY_BASED, 3);  // Change 3 to 5 or 2
```
**Effect**: More/less context switches

### 2. Change Priorities
In `OSSimulation.java`, modify:
```java
Process p1 = kernel.createProcess("Process-A", 8, 12);  // Change 8 to 10 for higher priority
```
**Effect**: Different process selection order

### 3. Add More Processes
```java
Process p5 = kernel.createProcess("Process-E", 7, 9);  // New process
```
**Effect**: More contention in ready queue

### 4. Try Different Algorithm
```java
kernel = new OSKernel(Scheduler.SchedulingAlgorithm.ROUND_ROBIN, 3);
```
**Effect**: FIFO scheduling instead of priority-based

## 📞 Need Help?

1. **Architecture**: Read `CLASS_DIAGRAM.txt`
2. **SOLARIS Details**: Read `docs/SOLARIS_DOCUMENTATION.md`
3. **Source Code**: Each file has detailed comments
4. **Compilation**: Ensure JDK 25 is set in PATH
5. **Visualization**: Open `dashboard.html` in browser

---

**Quick Command to Remember**:
```bash
cd /Users/vutl2004/Documents/OS && \
export JAVA_HOME=/Users/vutl2004/Downloads/jdk-25.0.1.jdk/Contents/Home && \
export PATH="$JAVA_HOME/bin:$PATH" && \
javac -d bin src/process/*.java src/scheduling/*.java src/dispatcher/*.java src/kernel/*.java src/OSSimulation.java && \
java -cp bin OSSimulation
```

Copy & paste this entire line to compile and run in one go!
