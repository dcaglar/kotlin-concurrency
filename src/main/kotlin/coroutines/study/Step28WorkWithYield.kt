package study

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import kotlin.system.measureTimeMillis

/*
  Case 5 - same as case 4, but the work function calls yield() now and then.

  Expect: still [main] everywhere, still about the SAME time as case 1 (maybe slightly slower).
  But the log order changes: all "start" lines appear first, then all "done" lines.
  The coroutines now take turns on the one thread instead of running back to back.

  Lesson: yield() makes coroutines cooperate. It does not make them faster.
 */

/** Same maths, but suspends every million terms to let other coroutines run. */
suspend fun leibnizCooperative(start: Long, end: Long): Double {
    var result = 0.0
    for (k in start until end) {
        result += (if (k % 2 == 0L) 1.0 else -1.0) / (2 * k + 1)
        if (k % 1_000_000 == 0L) yield()
    }
    return result
}

fun main() = runBlocking {
    val ms = measureTimeMillis {
        val results = DoubleArray(CHUNKS)
        val size = TERMS / CHUNKS

        val jobs = List(CHUNKS) { i ->
            launch {
                log("chunk $i start")
                results[i] = leibnizCooperative(i * size, (i + 1) * size)
                log("chunk $i done")
            }
        }
        jobs.forEach { it.join() }
        log("pi = ${results.sum() * 4}")
    }
    log("coroutines + yield, single thread: $ms ms")
}
