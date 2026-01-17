export JAVA_HOME=/Users/vutl2004/Downloads/jdk-25.0.1.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
java -cp bin OSSimulation | grep -E "SCHEDULER|DISPATCH|EXECUTE|CONTEXT|TERMINATED|\[T="# Process and Thread Execution in SOLARIS: Multi-core Systems

## Executive Summary

SOLARIS (Oracle Solaris) is an enterprise-grade UNIX operating system known for its advanced process scheduling, threading capabilities, and excellent multiprocessor scalability. This document describes in detail how SOLARIS manages multiple processes, multiple threads per process, and their execution across multicore systems.

---

## 1. SOLARIS Process Model

### 1.1 Process Structure
In SOLARIS, a process is a container for resources and execution contexts. Key components:

- **Process ID (PID)**: Unique identifier for the process
- **Process Address Space**: Virtual memory space (text, data, BSS, heap, stack)
- **Process Control Block (PCB)**: Kernel structure holding process metadata
- **Process Credentials**: UID, GID, groups for security
- **Signal Handlers**: User-defined actions for signals
- **File Descriptors**: Open files and I/O resources
- **Resource Limits**: CPU time, memory, file size limits

### 1.2 Process States in SOLARIS

```
NEW → READY ↔ RUNNING → STOPPED → ZOMBIE
       ↕        ↕
    BLOCKED ←───┘
```

- **NEW**: Process created, not yet in dispatcher queue
- **READY**: Runnable, waiting for CPU allocation
- **RUNNING**: Currently executing on a CPU core
- **BLOCKED**: Waiting for I/O, memory, or synchronization primitive
- **STOPPED**: Suspended by debugger or SIGSTOP signal
- **ZOMBIE**: Process terminated but parent hasn't reaped it

### 1.3 Process Hierarchy

SOLARIS maintains a process tree:
- **init (PID 1)**: First process, parent of all others
- **Parent Process**: Creates child processes via `fork()`
- **Process Groups**: Related processes for job control
- **Sessions**: Collection of process groups from same login session

---

## 2. SOLARIS Thread Model

### 2.1 Kernel-Level Threads (LWP - Lightweight Processes)

SOLARIS is **thread-aware** at kernel level. Key features:

- **1:1 Threading Model**: Each user-level thread maps to a kernel-level LWP
- **LWP Data Structure**: Holds thread-specific CPU context
  - Registers, program counter, stack pointer
  - Thread state (STOPPED, ONPROC, RUNQ, SLEEP, etc.)
  - Priority and scheduling class
  - Signals pending for this thread

### 2.2 User-Level Threads (Libthread/Libc)

SOLARIS provides both:

1. **POSIX Threads (pthreads)**:
   - Standard Portable Operating System Interface
   - Each pthread thread → underlying LWP
   - Explicit thread creation via `pthread_create()`

2. **Legacy Solaris Threads**:
   - Earlier API, largely superseded by pthreads
   - `thr_create()`, `thr_join()`, etc.

### 2.3 Thread States

Each thread has its own state independent of process:

```
NEW → RUNNABLE ↔ RUNNING → TERMINATED
        ↕
     BLOCKED
```

- **NEW**: Thread created, not yet scheduled
- **RUNNABLE**: Ready to execute, in dispatcher queue
- **RUNNING**: Currently executing on a CPU core
- **BLOCKED**: Waiting on mutex, condition variable, I/O
- **TERMINATED**: Thread exited via `pthread_exit()`

### 2.4 Thread-Specific Data (TSD)

Each thread can have private data via:
- **Thread Local Storage (TLS)**: Variables private to each thread
- `pthread_key_create()` / `pthread_setspecific()`
- No race conditions for thread-local variables

---

## 3. Multicore Execution Model

### 3.1 CPU Scheduling in Multicore Systems

SOLARIS scheduler design for multicore:

#### **Processor Sets (psets)**
- Assign groups of CPUs to specific processes/workloads
- Isolation and guaranteed resources
- Example: Database on cores 0-3, Web server on cores 4-7

#### **CPU Binding**
- Pin a thread to specific CPU core
- Reduces cache misses and context switch overhead
- API: `processor_bind()`, `pthread_setaffinity_np()`

#### **Load Balancing**
- Dispatcher monitors all CPU queues
- Migrates runnable threads to least-loaded CPUs
- Automatic unless CPU affinity is set

### 3.2 Dispatcher (Scheduler) in SOLARIS

The SOLARIS dispatcher is sophisticated:

```
Multiple Runnable Queues (one per priority level)
       ↓
    Dispatcher
       ↓
   ┌──────┬──────┬──────┬──────┐
   │ CPU0 │ CPU1 │ CPU2 │ CPU3 │  (multicore)
   └──────┴──────┴──────┴──────┘
```

#### **Priority-Based Preemptive Scheduling**

- **Priority Levels**: 0-159 (higher = more important)
- **Scheduling Classes**:
  1. **Real-Time (RT)**: Highest priority, hard deadline support
  2. **System (SYS)**: Kernel threads
  3. **Fair Share (FSS)**: CPU allocation proportional to shares
  4. **Interactive (IA)**: Desktop/interactive (bonus for I/O wait)
  5. **Time-Sharing (TS)**: Default, dynamic priorities

- **Time Quantum**: Varies by class
  - Real-Time: May be infinite (non-preemptive)
  - Interactive: 100-200 ms (small for responsiveness)
  - Time-Sharing: 100 ms (balanced)

#### **Dispatcher Algorithm**

1. Find highest-priority ready thread
2. If same priority, Round-Robin within that level
3. Execute for time quantum or until I/O wait
4. If preempted, move to end of same-priority queue
5. Repeat on next quantum or context switch

### 3.3 Context Switching

When a context switch occurs:

```
Current Thread State Saved
    ↓
CPU Context:
  • Registers → Memory (including stack pointer)
  • Program counter → Saved
  • Memory management (TLB, page tables)
    ↓
Next Thread State Loaded
    ↓
Resume execution
```

**Overhead**: ~1-10 microseconds per context switch (highly optimized in modern SOLARIS)

---

## 4. Example: Multi-process, Multi-threaded Application in SOLARIS

Let's imagine a Web Server with Multiple Threads:

### 4.1 Process Structure

```
Web Server Process (PID 2048)
│
├─► Kernel LWP 1 (Listener Thread)
│   ├─ Binds to TCP port 80
│   ├─ Accept incoming connections
│   ├─ Create worker threads for each request
│   └─ Running on CPU 0 (affinity set)
│
├─► Kernel LWP 2 (Worker Thread 1)
│   ├─ Handle HTTP request #1
│   ├─ Read from socket → Blocked (I/O wait)
│   ├─ Wake up on data arrival
│   └─ Running on CPU 1
│
├─► Kernel LWP 3 (Worker Thread 2)
│   ├─ Handle HTTP request #2
│   ├─ Parse HTTP headers → CPU-intensive
│   ├─ Fetch from database → Blocked (I/O wait)
│   └─ Running on CPU 2
│
└─► Kernel LWP 4 (Cleanup Thread)
    ├─ Monitor resource usage
    ├─ Reap terminated threads
    ├─ Low priority, runs when CPU idle
    └─ Running on CPU 3
```

### 4.2 Timeline of Execution (4-core System)

```
Time 0ms:
  CPU0: Listener (LWP 1) accepts connection
  CPU1: Worker 1 (LWP 2) processing request
  CPU2: Worker 2 (LWP 3) processing request  
  CPU3: Idle

Time 5ms:
  CPU0: Listener ready to accept next connection
  CPU1: Worker 1 blocked on socket read (waiting for data)
        → Dispatcher moves another ready thread to CPU1
  CPU2: Worker 2 executing query
  CPU3: Cleanup thread starts (was in ready queue)

Time 10ms:
  CPU0: Listener accepts new connection
  CPU1: New worker thread starts processing request #3
  CPU2: Worker 2 blocked on I/O
        → Cleanup thread moves to CPU2
  CPU3: Worker 1 unblocks (data arrived), ready queue

Time 15ms:
  CPU0: Listener
  CPU1: Worker 3 processing
  CPU2: Cleanup thread
  CPU3: Worker 1 executes (highest priority, I/O-wait bonus)

... Continues dynamically across 4 cores ...
```

### 4.3 Memory Layout (Process Address Space)

Each process has independent virtual address space:

```
┌─────────────────────────┐  ← High address
│   Stack (grows down)    │
│   ┌──────────────────┐  │    ┌─────────────────┐
│   │ Thread 1 stack   │  │    │ Independent per │
│   ├──────────────────┤  │    │ thread!         │
│   │ Thread 2 stack   │  │    └─────────────────┘
│   ├──────────────────┤  │
│   │ Thread 3 stack   │  │
│   └──────────────────┘  │
│                         │
│   (gap - guard pages)   │
│                         │
│   Heap (grows up)       │
│   ┌──────────────────┐  │    ┌─────────────────┐
│   │ malloc'd memory  │  │    │ Shared among    │
│   │ (shared threads) │  │    │ all threads!    │
│   └──────────────────┘  │    └─────────────────┘
│                         │
│ Uninitialized data (BSS)│
│ Initialized data        │
│ Text (code) - Read-only │
└─────────────────────────┘  ← Low address
```

---

## 5. Synchronization Mechanisms in SOLARIS

### 5.1 Mutexes (Mutual Exclusion Locks)

```
Thread 1                        Thread 2
    │                               │
    ├─ pthread_mutex_lock()         │
    │  ✓ Acquired!                  │
    │  [Critical Section]           │
    │  ├─ Modify shared resource    ├─ pthread_mutex_lock()
    │  │  (database record)         │  ✗ Blocked! Wait for lock
    │  └─ Complete operation        │
    │  pthread_mutex_unlock()       │
    │  Lock released!               ├─ ✓ Acquired!
    │                               │  [Critical Section]
    │                               │
```

### 5.2 Condition Variables

```
Producer Thread                 Consumer Thread
    │                               │
    ├─ [Produce data]              │
    ├─ pthread_mutex_lock()        │
    ├─ [Insert into queue]         ├─ pthread_mutex_lock()
    ├─ [Queue not empty now]       ├─ while (queue empty)
    ├─ pthread_cond_signal()       │    pthread_cond_wait()
    │  (Wake up waiting threads)   │    [Blocked, releasing lock]
    ├─ pthread_mutex_unlock()      │
    │                              ├─ [Woken up!]
    │                              ├─ Queue no longer empty
    │                              ├─ Reacquire lock
    │                              ├─ [Consume data]
    │                              ├─ pthread_mutex_unlock()
```

### 5.3 Synchronization Primitives Available

- **Mutexes**: PTHREAD_MUTEX_NORMAL, PTHREAD_MUTEX_ERRORCHECK, PTHREAD_MUTEX_RECURSIVE
- **Condition Variables**: Signal/broadcast to sleeping threads
- **Semaphores**: POSIX semaphores for resource counting
- **Reader-Writer Locks**: Allow concurrent readers, exclusive writers
- **Barriers**: Synchronize multiple threads at checkpoint

---

## 6. Process Creation and Thread Creation

### 6.1 Creating Processes

```c
pid_t pid = fork();
if (pid == 0) {
    // Child process
    execve("/bin/command", args, env);  // Replace image
    exit(0);
} else if (pid > 0) {
    // Parent process
    waitpid(pid, &status, 0);  // Wait for child
} else {
    // Error
}
```

- **fork()**: Creates child process (copy-on-write optimization)
- **execve()**: Replace process image with new program
- **exit()**: Terminate process and free resources
- **waitpid()**: Parent reaps child

### 6.2 Creating Threads

```c
pthread_t thread_id;
pthread_attr_t attr;

pthread_attr_init(&attr);
pthread_attr_setschedpolicy(&attr, SCHED_FIFO);  // Real-time
pthread_attr_setschedparam(&attr, &param);       // Set priority

pthread_create(&thread_id, &attr, thread_function, arg);
pthread_join(thread_id, NULL);  // Wait for thread
```

- **pthread_create()**: Create new thread within same process
- **pthread_join()**: Wait for thread to complete
- **Shared resources**: All threads share memory, file descriptors
- **Independent execution**: Each has own stack, registers, priority

---

## 7. Multicore Execution in SOLARIS

### 7.1 Thread Affinity (CPU Binding)

To avoid cache thrashing and context switches:

```c
processorid_t cpu_id = 1;
processor_bind(P_LWPID, P_MYID, cpu_id, NULL);
// Bind current thread to CPU 1
```

Benefits:
- **Cache Locality**: Thread stays on same core, data in L1/L2 cache
- **NUMA Awareness**: Thread stays local to memory bank
- **Reduced Latency**: No inter-core synchronization needed

### 7.2 CPU Hotplug and Dynamic Reconfiguration

SOLARIS supports dynamic CPU changes:

```c
// At runtime, administrator can:
psradm -f 3  // Turn off CPU 3
psradm -n 2  // Turn on CPU 2 (if previously off)
```

Scheduler automatically:
- Moves threads from disabled CPU to active ones
- Rebalances load across remaining CPUs
- Maintains fairness and priorities

### 7.3 Memory Locality Groups (LGroups)

For NUMA systems (multiple memory banks):

```
CPU 0,1 ← Memory Bank 0
CPU 2,3 ← Memory Bank 1
```

SOLARIS scheduler:
- Tries to keep threads and their memory on same lgroup
- Reduces remote memory access latency
- Transparent to application (kernel auto-optimizes)

---

## 8. Performance Considerations

### 8.1 Context Switch Overhead

**Factors**:
- TLB (Translation Lookaside Buffer) flush: ~200 cycles
- Cache invalidation: ~100-1000 cycles
- Register save/restore: ~10 cycles

**Total**: ~1-10 microseconds per switch (varies with hardware)

**Optimization**: Reduce context switches by:
- Proper thread affinity
- Right balance of thread count vs cores
- Avoiding excessive lock contention

### 8.2 Lock Contention

When many threads compete for same mutex:

```
Thread 1: ✓ Acquired lock, working
Thread 2: ✗ Blocked
Thread 3: ✗ Blocked
Thread 4: ✗ Blocked

Spinning in kernel, context switching, cache bouncing
→ CPU cache invalidations
→ Memory bus contention
→ Lower throughput
```

**Solution**: Fine-grained locking, lock-free data structures

### 8.3 Scalability

SOLARIS is known for excellent multicore scalability:
- Linear scaling up to hundreds of CPUs
- Partitioning and processor sets prevent bottlenecks
- Efficient lock implementations (adaptive spinning, futex-like)

---

## 9. Monitoring and Debugging

### 9.1 Tools in SOLARIS

```bash
# Process information
ps -eLf  # List all threads with LWP info
pstack <pid>  # Stack trace of all threads

# CPU and resource info
cpustat  # Per-CPU statistics
iostat -x 1  # I/O statistics per device
mpstat -a 1  # Per-CPU performance metrics

# Thread-specific
thread_stats  # Thread statistics
prstat  # Process/thread resource stats
```

### 9.2 Example Output Analysis

```
ps -eLf | grep http
  UID   PID  PPID  LWP NLWP  STIME TTY TIME CMD
  root  2048 1    1    8    10:00 ?   0:01 /usr/bin/httpd
  root  2048 1    2    8    10:00 ?   0:05 /usr/bin/httpd
  root  2048 1    3    8    10:00 ?   0:03 /usr/bin/httpd
  ...
```

Interpretation:
- Same PID (2048) = same process
- Different LWP numbers (1,2,3) = different threads
- NLWP=8 = total 8 threads in process

---

## 10. Comparison with Simulation

### 10.1 Our Simulation vs Real SOLARIS

| Aspect | Simulation | Real SOLARIS |
|--------|-----------|--------------|
| Scheduling | Priority-based queue | Multiple classes, dynamic priorities |
| Context Switching | Simulated steps | Hardware-assisted (CPU registers) |
| Threads | 2 kernel-like threads | Hundreds to thousands |
| Synchronization | Simple mutex | Multiple primitives (mutex, cond var, etc.) |
| Multicore | Single CPU model | True parallel execution on all cores |
| Fairness | Time quantum | Per-scheduling-class fairness |
| Preemption | On quantum expiration | Immediate on higher-priority event |

### 10.2 Simulation Demonstrates Core Concepts

✓ Process creation and state management
✓ Scheduler and dispatcher separation
✓ Priority-based selection
✓ Preemptive multitasking
✓ Thread cooperation
✓ Concurrent execution model

---

## 11. Real-World SOLARIS Example

### Scenario: Java Application Server on 16-core SOLARIS

```
Java Process (PID 5000)
├─ Main thread (Dispatcher)
│  ├─ LWP assigned to CPU 0
│  ├─ Monitor health, manage other threads
│  └─ Priority: Normal (TS class, priority 40)
│
├─ Request Listener threads (Acceptor pattern)
│  ├─ LWP 1-2 → CPUs 1-2
│  ├─ Accept incoming HTTP connections
│  ├─ Assign to worker thread pool
│  └─ Priority: Higher (TS class, priority 45) - I/O wait bonus
│
├─ Worker Thread Pool (16 threads)
│  ├─ LWP 3-18 → CPUs 3-14 (with load balancing)
│  ├─ Process business logic
│  ├─ Database queries (blocking I/O)
│  ├─ Web service calls (blocking I/O)
│  └─ Priority: Normal (TS class, 40-50)
│
├─ GC Thread (Garbage Collector)
│  ├─ LWP 19 → CPU 15
│  ├─ Reclaim unused memory
│  ├─ May pause other threads periodically
│  └─ Priority: Lower (TS class, priority 30)
│
└─ Timer/Background tasks
   ├─ LWP 20 → CPU 15 (shared with GC)
   ├─ Periodic maintenance
   └─ Priority: Low (TS class, priority 25)
```

**Execution Trace (100ms window)**:

```
Time 0ms:
  CPU 0: Main dispatcher checking queues
  CPU 1-2: Listeners waiting for connections
  CPU 3-14: Workers processing requests (14 worker threads on 12 CPUs)
  CPU 15: GC thread sleeping (unused memory low)

Time 20ms:
  CPU 0: Main dispatcher, normal priority
  CPU 1: Listener thread (high priority, just accepted 3 connections)
  CPU 2: Listener thread
  CPU 3-9: Workers executing (6 threads)
  CPU 10-12: Workers blocked on database I/O
  CPU 13-14: Workers blocked on network I/O
  CPU 15: Timer thread wakes up, runs cleanup task

Time 50ms:
  CPU 0-14: Mostly worker threads (load balanced)
  CPU 15: GC starts (low-priority, runs when CPU available)
  → GC pauses application briefly, scans heap

Time 100ms:
  CPU 0: Main dispatcher
  CPU 1-2: Listeners (intermittent activity)
  CPU 3-14: Workers again (new requests processed)
  CPU 15: GC continues incrementally
```

**Multicore Benefits Realized**:
- 16 CPUs → up to 16 concurrent threads
- No context switch between workers (enough CPUs)
- Listeners on dedicated CPUs → minimal latency for new connections
- GC on separate CPU → minimal pause time
- Automatic load balancing if some workers block

---

## Conclusion

SOLARIS provides a mature, production-grade OS for complex multithreaded, multiprocess applications on multicore systems. Its strengths:

1. **Kernel-level threading**: Efficient 1:1 mapping between user threads and kernel LWPs
2. **Flexible scheduling**: Multiple scheduling classes for different application needs
3. **Multicore optimization**: CPU affinity, lgroups, load balancing
4. **Scalability**: Proven on systems with hundreds of CPUs
5. **Synchronization primitives**: Rich set for building concurrent systems

The simulation in this project demonstrates the fundamental scheduling and dispatching concepts that SOLARIS (and modern operating systems) use to manage multiple processes and threads efficiently on multicore hardware.

---

**Generated**: January 17, 2026
**Document Version**: 1.0
