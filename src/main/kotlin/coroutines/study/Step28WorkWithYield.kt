package coroutines.study

import coroutines.workshop.common.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import kotlin.system.measureTimeMillis

/*
  Step 28 - step 27, but the work gives the thread away now and then.

  Look at: all "starts" lines come first, then all "is done" lines. Still everything on [main].
           The time is the same as in step 24, or a bit slower.
  Why:     this is step 15 with real work. The coroutines take turns on the one thread.
           yield() makes coroutines cooperate. It does not make them faster.
  Try:     yield after every 1_000 terms instead of every 1_000_000. What happens to the time?
 */
fun main() {
    val results = DoubleArray(CHUNKS)
    val ms = measureTimeMillis {
        runBlocking { startChunks(results) }
    }
    logger.info("pi = {}", results.sum() * 4)
    logger.info("it took {} ms", ms)
}

private fun CoroutineScope.startChunks(results: DoubleArray) {
    repeat(CHUNKS) { i ->
        launch { computeChunkWithYield(i, results) }
    }
}

/** Same as computeChunk(), but with leibnizWithYield(). */
private suspend fun computeChunkWithYield(i: Int, results: DoubleArray) {
    val size = TERMS / CHUNKS
    logger.info("chunk {} starts", i)
    results[i] = leibnizWithYield(i * size, (i + 1) * size)
    logger.info("chunk {} is done", i)
}

/** Same as leibniz(), but gives the thread away after every million terms. */
private suspend fun leibnizWithYield(start: Long, end: Long): Double {
    var result = 0.0
    for (k in start until end) {
        result += (if (k % 2 == 0L) 1.0 else -1.0) / (2 * k + 1)
        if (k % 1_000_000 == 0L) yield()
    }
    return result
}
