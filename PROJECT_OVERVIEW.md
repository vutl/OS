# 🖥️ OS Kernel Simulation - Project Overview

## What Is This Project?

This is a **real operating system kernel simulator** written in Java that demonstrates how modern operating systems manage processes and threads on multicore computers.

## What Does It Do?

### 1. **Process Management** 
- Creates 4 processes with different priorities (P9, P8, P5, P3)
- Tracks each process through its lifecycle: `NEW → READY → RUNNING → TERMINATED`
- Each process has a "burst time" (how long it needs to run on the CPU)

### 2. **Process Scheduling**
- The **Scheduler** decides which process should run next based on priority
- Higher priority processes get to run first
- Maintains a "ready queue" of processes waiting for CPU time

### 3. **CPU Dispatching**
- The **Dispatcher** assigns the selected process to the CPU
- Allocates a time slice (3 units) - each process gets max 3 time units before it's interrupted
- Performs **context switching** - saves one process's state, loads the next process's state
- When time runs out, the process goes back to the ready queue if not finished

### 4. **Multi-Threading**
- **Scheduler Thread** (PID 25): Runs continuously, checks which processes are ready, monitors queue status
- **Dispatcher Thread** (PID 26): Runs continuously, executes processes on the CPU, handles context switches
- Both threads run **concurrently** and synchronize with each other to prevent conflicts

### 5. **Interactive Visualization**
- **simulation.html** - Watch the simulation in real-time
  - See which process is running
  - Watch the ready queue grow and shrink
  - Track execution progress with progress bars
  - Monitor both threads working simultaneously

## How It Works (Step by Step)

1. **Start**: Program creates 4 processes and puts them in the ready queue
2. **Scheduler Cycle**: Scheduler picks the highest priority process
3. **Dispatch**: Dispatcher sends that process to the CPU
4. **Execute**: Process runs for up to 3 time units, reducing its remaining time
5. **Context Switch**: When time expires or process finishes:
   - Save current process state
   - Send finished process back to queue (or terminate if done)
   - Load next process from queue
6. **Repeat**: Continue until all processes finish (50 cycles total)

## Key Concepts Demonstrated

| Concept | What It Is | Why It Matters |
|---------|-----------|-----------------|
| **Priority Scheduling** | Run important processes first | Responsiveness, fairness |
| **Time Quantum** | Max time each process gets (3 units) | Prevents one process from monopolizing CPU |
| **Context Switching** | Save/load process state | Enables multiple processes to share one CPU |
| **Multi-threading** | 2 kernel threads working together | Shows how OS components operate concurrently |
| **Preemption** | Interrupting a process before it finishes | Ensures all processes get CPU time |

## Real-World Application

This simulation mirrors how **real operating systems** like Linux, Windows, and SOLARIS actually work:

- **Linux Scheduler**: Uses similar priority-based queues
- **CPU Context Switching**: Happens thousands of times per second on real CPUs
- **Multi-threading**: Every OS has kernel threads managing various subsystems
- **Time Quantum**: Called "time slice" or "scheduling quantum" in real OSes

## The 4 Processes in the Simulation

```
Process-C: Priority 9, Burst Time 6 units    (VIP - runs first)
Process-A: Priority 8, Burst Time 12 units   (High priority)
Process-B: Priority 5, Burst Time 8 units    (Medium priority)
Process-D: Priority 3, Burst Time 10 units   (Low priority)
```

## Simulation Results

Running `simulation.html` in your browser shows:
- **50 time cycles** of kernel execution
- **2 concurrent threads** (Scheduler & Dispatcher) each completing 50 cycles
- All processes **finishing execution** in order of priority + fairness
- **Real-time visualization** of CPU usage and queue status

## Files Explained

| File | Purpose |
|------|---------|
| `src/process/*.java` | Process and Thread classes |
| `src/scheduling/Scheduler.java` | Priority queue, scheduling decisions |
| `src/dispatcher/Dispatcher.java` | CPU allocation, context switching |
| `src/kernel/OSKernel.java` | Coordinator of all components |
| `src/OSSimulation.java` | Main program with 2 concurrent threads |
| `simulation.html` | Interactive browser-based visualization |
| `README.md` | Full technical documentation |
| `docs/SOLARIS_DOCUMENTATION.md` | Real SOLARIS OS details |

## Quick Start

```bash
# Compile
javac -d bin src/process/*.java src/scheduling/*.java \
  src/dispatcher/*.java src/kernel/*.java src/OSSimulation.java

# View in browser
open /Users/vutl2004/Documents/OS/simulation.html

# Click "Start Simulation" to watch it run
```

**That's it!** You now understand how operating systems schedule and execute processes. 🎓
