# Study steps: from a boring program to coroutines

Run each `main` from IntelliJ, in order. Every step adds ONE new thing to the step before.
The sections follow the topics of the workshop (README.md at the top of the project).

## How to read the output

Every log line starts with WHO is running:

| Start of the line | Meaning |
|---|---|
| `[main]` | the thread main, no coroutine |
| `[worker]` | another thread, no coroutine |
| `[main @coroutine#2]` | coroutine#2, running on the thread main |
| `[DefaultDispatcher-worker-3 @coroutine#5]` | coroutine#5, running on a thread of `Dispatchers.Default` |

The coroutine part needs the VM option `-Dkotlinx.coroutines.debug`
(Run -> Edit Configurations -> Edit configuration templates -> Kotlin).
Look at the timestamps too: a gap of 1 second is somebody waiting.

## How to read the code

- Every file has a comment at the top: **Look at** (what to check in the output), **Why**, and **Try** (one small change to make).
- A `thread { }`, `runBlocking { }`, `launch { }` or `async { }` block only CALLS a function. The work is in the function.
- The standard functions are in `Common.kt`. Every wait in them is 1 second.

| Function | Does | Stands for |
|---|---|---|
| `sayHelloWithSleep()` | sleeps 1 s, logs hello | blocking I/O: a JDBC query, reading a file |
| `sayHelloWithDelay()` | delays 1 s, logs hello | non-blocking I/O: a call with a suspend HTTP client |
| `findAnswerWithSleep()` / `findAnswerWithDelay()` | waits 1 s, returns 42 | the same, with a result |
| `tickWithDelay()` / `tickWithSleep()` | logs a tick every second | a long job |
| `count(name)` / `countWithYield(name)` | counts to 3, busy before every number | short computing |
| `computePi()` / `computeChunk(i)` | computes pi | long computing |
| `sendNumbers(channel)` / `receiveSlowly(channel)` | sends 1 to 5 fast / receives one per second | a fast producer and a slow consumer |

## 1. Threads, the old way

| Step | File | New | Look at |
|---|---|---|---|
| 1 | Step01Boring | nothing | `[main]` on every line |
| 2 | Step02OneThread | `thread { }` | main is finished before the worker says hello |
| 3 | Step03OneThreadJoin | `join()` | main waits for the worker |
| 4 | Step04ThreadResultTooEarly | a value from a thread | the answer is 0: read too early |
| 5 | Step05ThreadResultJoin | + `join()` | the answer is 42 |

## 2. Your first coroutine: `runBlocking`, `suspend`, `launch`

| Step | File | New | Look at |
|---|---|---|---|
| 6 | Step06RunBlockingSleep | `runBlocking { }` | `[main @coroutine#1]`: a coroutine on the thread main |
| 7 | Step07RunBlockingDelay | `suspend`, `delay` | only a coroutine can call a suspend function |
| 8 | Step08Launch | `launch { }` | the child runs AFTER the parent |

## 3. How lightweight are coroutines?

| Step | File | New | Look at |
|---|---|---|---|
| 9 | Step09ManyThreadsSleep | 100_000 threads | crash: unable to create native thread |
| 10 | Step10ManyCoroutinesDelay | 100_000 coroutines | about 1 second, all on the thread main |

## 4. Cooperative vs. preemptive

| Step | File | New | Look at |
|---|---|---|---|
| 11 | Step11TwoThreads | two threads count | mixed, the order changes every run (preemptive) |
| 12 | Step12TwoChildren | two coroutines count | A 1 2 3, then B 1 2 3, every run (cooperative) |
| 13 | Step13BlockingCoroutines | a coroutine that blocks | 3 coroutines with sleep: 3 seconds |
| 14 | Step14Yield | `yield()` | the child runs between the two lines of the parent |
| 15 | Step15TwoChildrenYield | two coroutines + yield | A 1, B 1, A 2, B 2, ... |

## 5. Cancellation and timeouts

| Step | File | New | Look at |
|---|---|---|---|
| 16 | Step16Job | `Job` | active before join, completed after |
| 17 | Step17Cancel | `cancel()` | tick 1, 2, 3, cancelled, no tick 4 |
| 18 | Step18CancelTooLate | cancel a blocking coroutine | all 5 ticks: the cancel comes too late |
| 19 | Step19WithTimeout | `withTimeout` | tick 1, 2, 3, then the time is over |

## 6. Explicit concurrent execution of coroutines

| Step | File | New | Look at |
|---|---|---|---|
| 20 | Step20OneAfterAnother | two suspend calls | one after the other: 2 seconds |
| 21 | Step21AsyncAwait | `async`, `await()`, `Deferred` | a coroutine returns a value |
| 22 | Step22TwoAsync | two `async` | both at the same time: 1 second |
| 23 | Step23CoroutineScope | `coroutineScope { }` | a suspend function that starts coroutines itself |

## 7. Computing instead of waiting (compute pi, measure the time)

The same six programs, run on 1 core and on all cores. Run `./computing-cores.sh` in this folder:
it runs every step both ways and prints the two times side by side (needs Docker or OrbStack).
It takes step numbers too, so you never edit the script: `./computing-cores.sh 25 29`, `./computing-cores.sh 10-15 -o`
(full output), `./computing-cores.sh 29 -c 2 -1` (only the container run, with 2 cores). `-h` lists the options.

| Step | File | Starts | 1 core | 8 cores |
|---|---|---|---|---|
| 24 | Step24BoringWork | nothing, one thread | 664 ms | 549 ms |
| 25 | Step25ManyThreadsWork | 8 threads | 651 ms | 240 ms |
| 26 | Step26RunBlockingWork | 1 coroutine | 690 ms | 560 ms |
| 27 | Step27WorkInCoroutines | 8 coroutines on one thread | 719 ms | 573 ms |
| 28 | Step28WorkWithYield | 8 coroutines on one thread + yield | 867 ms | 727 ms |
| 29 | Step29Dispatcher | 8 coroutines on `Dispatchers.Default` | 718 ms | 266 ms |

On 1 core nothing makes computing faster. On 8 cores only steps 25 and 29 get faster: they use more than one thread.

## 8. Shared mutable state and synchronization

| Step | File | New | Look at |
|---|---|---|---|
| 30 | Step30SharedCounter | 1_000 coroutines, one counter, one thread | 1_000_000, correct |
| 31 | Step31SharedCounterRace | the same on many threads | less than 1_000_000, different every run |
| 32 | Step32SharedCounterMutex | `Mutex` | 1_000_000 again, but slower |

## 9. Channels: communicating instead of sharing

Steps 30 to 32 share one counter and lock it. A channel does it the other way: one coroutine sends values,
another receives them, and nothing is shared. Everything runs on the thread main, except step 40.

| Step | File | New | Look at |
|---|---|---|---|
| 33 | Step33Channel | `Channel()`, `send`, `receive` | the sender waits at `send` until the receiver takes the value: a handoff |
| 34 | Step34ChannelClose | `close()`, `for (n in channel)` | the receiver stops by itself when the channel is closed |
| 35 | Step35ChannelBuffer | `Channel(capacity = 2)` | the sender runs 2 ahead, finished after 2 s instead of 4 s: backpressure |
| 36 | Step36Produce | `produce { }` | step 34 without the channel and the `close()`: produce does both |
| 37 | Step37Pipeline | a pipeline: numbers -> squares | 1, 4, 9, 16, 25: three coroutines, two channels |
| 38 | Step38FanOut | 3 receivers, one channel | every number is got once, by one of them: a work queue, about 2 s |
| 39 | Step39FanIn | 3 senders, one channel | A, B and C mixed into one stream |
| 40 | Step40SharedCounterChannel | step 32 with a channel instead of a `Mutex` | 1_000_000, correct, no lock: one coroutine owns the counter |
| 41 | Step41ChannelIsHot | a channel is hot | it sends before anybody receives; the second receiver gets nothing |

## 10. Generators (flows)

| Step | File | New | Look at |
|---|---|---|---|
| 42 | Step42Flow | `flow { }`, `emit`, `collect` | send 1, got 1, send 2, got 2, ... |
| 43 | Step43FlowIsCold | a flow is cold | nothing is sent before collect; sent again for every collect (compare step 41) |
