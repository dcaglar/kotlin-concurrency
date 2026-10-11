# Study cases: threads vs coroutines (exercises 1-3 in one problem)

Drop this `study` folder into the workshop project at
`src/main/kotlin/study/` (either zip's project works - it already has
`kotlinx-coroutines-core` in the pom). Run each `main` from IntelliJ.

Every case solves the same problem: sum 400M terms of the Leibniz series.

| Case | File | What changes | Expect |
|---|---|---|---|
| 0 | Case0OrderDemo | just launch/yield ordering | read the log order |
| 1 | Case1Sequential | baseline | T ms |
| 2 | Case2RunBlockingOnly | wrap in runBlocking | same T |
| 3 | Case3Threads | 8 OS threads | ~T / cores |
| 4 | Case4CoroutinesSingleThread | 8 launch in runBlocking | same T, all on [main], run back to back |
| 5 | Case5CoroutinesYield | + yield() in the loop | same T, all on [main], interleaved |
| 6 | Case6CoroutinesParallel | launch(Dispatchers.Default) | ~T / cores |
| 7 | Case7WaitingThreadsVsCoroutines | sleep vs delay (exercise 2) | both ~1 s; raise N to break threads |

Order to run: 0, 1, 2, 3, 4, 5, 6, 7.

Experiments after that:
- Case 4: replace `results[i] = ...` with a shared `var total` and `total += ...`. Still correct? (yes - one thread)
- Case 6: do the same. Correct now? (no - data race)
- Case 6: swap `Dispatchers.Default` for `Dispatchers.IO`. Works, but why is it the wrong tool?
- Case 7: put `Thread.sleep(1_000)` inside the `launch` in part B. What happens to the time?
