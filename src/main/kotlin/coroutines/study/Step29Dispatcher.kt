package coroutines.study

import coroutines.workshop.common.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

/*
  Step 29 - new: a dispatcher. Step 27 with one change: launch(Dispatchers.Default).

  Look at: the chunk lines come from [DefaultDispatcher-worker-1 @coroutine#2], worker-2, worker-3, ...
           The time is about the same as with threads in step 25.
  Why:     the dispatcher decides WHICH threads a coroutine runs on. Dispatchers.Default is a
           pool with one thread per core. Without a dispatcher, launch uses the thread of runBlocking.
  Try:     use Dispatchers.IO. It works, but it is made for blocking work (sleep), not for computing.
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
        launch(Dispatchers.Default) { computeChunk(i, results) }
    }
}
