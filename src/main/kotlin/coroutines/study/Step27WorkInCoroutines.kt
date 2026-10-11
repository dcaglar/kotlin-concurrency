package coroutines.study

import coroutines.workshop.common.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

/*
  Step 27 - step 25 with coroutines: one coroutine per piece, all on ONE thread.

  Look at: chunk 0 starts, chunk 0 is done, chunk 1 starts, chunk 1 is done, ... All on [main].
           The time is the same as in step 24.
  Why:     this is step 12 with real work. One thread, and computeChunk() never gives it away.
           8 coroutines on one thread are not faster than 1.
  Try:     compare the order of the lines with step 25.
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
        launch { computeChunk(i, results) }
    }
}
