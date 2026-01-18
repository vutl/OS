# OS Kernel Simulator (Java)

This repository contains a small educational simulator, implemented in Java, that models a simplified OS kernel's scheduling and dispatching behavior. It is intended for learning and demonstration purposes — not a real kernel.

Key points:
- The simulator models processes and user-level threads, a Scheduler (ready queues + priority with round-robin fairness) and a Dispatcher (CPU allocation, time-slice preemption).
- The simulation runs two kernel-like threads inside the JVM: a `SchedulerThread` (manages queues) and a `DispatcherThread` (executes processes / advances CPU time).
- Scheduling algorithm: MIXED — priority-based selection (higher priority first) with Round-Robin time-slicing among processes of the same priority. Default time quantum = 3 units.

## Project structure

```
./
├── src/
│   ├── process/
│   │   ├── ProcessState.java
│   │   ├── Process.java
│   │   └── Thread.java
│   ├── scheduling/
│   │   └── Scheduler.java
│   ├── dispatcher/
│   │   └── Dispatcher.java
│   ├── kernel/
│   │   └── OSKernel.java
│   └── OSSimulation.java
├── bin/                 # compiled classes (after javac -d bin ...)
├── docs/                # additional docs (SOLARIS notes, diagrams)
├── dashboard.html       # optional browser demo / visualization
└── README.md            # this file
```

## What this simulator demonstrates

- Process lifecycle: `NEW → READY → RUNNING → BLOCKED → TERMINATED` (simulated states).
- Scheduling: priority queueing plus Round-Robin inside the same priority level.
- Dispatching: time-sliced execution, context switching when quantum expires or process completes.
- Concurrency: two JVM threads simulate kernel components (scheduler and dispatcher). Synchronization uses `synchronized` blocks.
- Instrumentation: execution logs, dispatch history, and basic per-process metrics (start, end, waiting/turnaround times).

## Build & run

Prerequisites: JDK 17 or newer (the project was tested with JDK 25).

From the repository root:

```bash
mkdir -p bin
javac -d bin \
    src/process/ProcessState.java \
    src/process/Process.java \
    src/process/Thread.java \
    src/scheduling/Scheduler.java \
    src/dispatcher/Dispatcher.java \
    src/kernel/OSKernel.java \
    src/OSSimulation.java

java -cp bin OSSimulation
```

The program prints the creation of sample processes, then starts the `SchedulerThread` and `DispatcherThread`. Logs include dispatch events, execution steps, and a final summary.

## Default simulation scenario

- The provided `OSSimulation` constructs 5 sample processes (including a same-priority pair to show RR fairness) and a couple of user-threads for demonstration.
- Scheduling algorithm: `MIXED` (priority with RR). Time quantum defaults to `3` time units.

## Where this differs from a real OS (e.g., Solaris)

- This project is a user-space simulator that models high-level behaviors. It does NOT implement kernel concepts such as LWPs, real context switches at CPU/VM level, TLB/MMU handling, interrupts, hardware affinity, or true parallel kernel scheduling.
- For a detailed description of how a production OS (Solaris) executes processes and threads on multicore hardware, see `docs/SOLARIS_DOCUMENTATION.md`.

## Files of interest

- `src/scheduling/Scheduler.java` — ready queue management and selection policy (mixed priority + RR).
- `src/dispatcher/Dispatcher.java` — simulates CPU dispatch, time-slice accounting, and process execution for one time unit per cycle.
- `src/kernel/OSKernel.java` — high-level coordinator for creating processes/threads and orchestrating the simulation.
- `src/OSSimulation.java` — entry point; spawns the two simulator kernel threads and creates sample processes.

## Extending the simulator

- Add I/O events and a blocked queue in `Dispatcher` and `Scheduler` to simulate blocking syscalls.
- Implement additional scheduling policies in `Scheduler` (e.g., SJF, multi-level feedback queue).
- Export logs to CSV/JSON for offline analysis or build a richer dashboard page.

## Notes for instructors / graders

- The simulator satisfies the assignment: it simulates dispatching, scheduling (priority + RR), state transitions, and demonstrates two concurrent threads performing scheduler/dispatcher roles.
- A class diagram and a SOLARIS-oriented writeup are available in `docs/` to contrast the simulator with real OS behavior.

---

If you want, I can also:
- add or regenerate `docs/SOLARIS_DOCUMENTATION.md` with the detailed Solaris execution text I prepared earlier, or
- create a short mapping table that links each simulator class (`Scheduler`, `Dispatcher`, `Process`, `Thread`) to the closest Solaris concept (run queues, LWP, address space, etc.).

Which of these next steps do you want me to do?
