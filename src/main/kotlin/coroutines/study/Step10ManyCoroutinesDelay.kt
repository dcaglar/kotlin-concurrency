package coroutines.study

import coroutines.workshop.common.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

/*
  Step 10 - 100_000 coroutines. Each one runs sayHelloWithDelay().

  Look at: no crash. 100_000 hellos, all on the thread main, in a bit more than 1 second.
           The coroutine numbers go up to coroutine#100001.
  Why:     delay gives the thread back. While one coroutine waits, the thread starts the next one.
           A waiting coroutine is a small object, not a thread (compare step 9).
  Try:     set MANY in Common.kt to 1_000_000.
 */
fun main() {
    val ms = measureTimeMillis {
        runBlocking { startCoroutines() }
    }
    logger.info("{} coroutines took {} ms", MANY, ms)
}

private fun CoroutineScope.startCoroutines() {
    repeat(MANY) {
        launch { sayHelloWithDelay() }
    }
}
