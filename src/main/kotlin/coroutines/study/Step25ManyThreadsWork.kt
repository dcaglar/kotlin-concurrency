package coroutines.study

import coroutines.workshop.common.logger
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

/*
  Step 25 - the work in 8 pieces, one thread per piece.

  Look at: all "starts" lines come before the "is done" lines, each from another thread.
           The time is about T / (number of cores).
  Why:     every thread gets its own core: they really compute at the same time.
  Try:     set CHUNKS in Common.kt to 2, then to 64. When does it stop getting faster?
 */
fun main() {
    val results = DoubleArray(CHUNKS)
    val ms = measureTimeMillis {
        val threads = List(CHUNKS) { i ->
            thread(name = "worker-$i") { computeChunk(i, results) }
        }
        threads.forEach { it.join() }
    }
    logger.info("pi = {}", results.sum() * 4)
    logger.info("it took {} ms", ms)
}
