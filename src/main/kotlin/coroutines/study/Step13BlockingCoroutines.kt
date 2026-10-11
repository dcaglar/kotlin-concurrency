package coroutines.study

import coroutines.workshop.common.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

/*
  Step 13 - a coroutine that misbehaves: it blocks. Three coroutines run sayHelloWithSleep().

  Look at: one hello per second, 3 seconds in total.
  Why:     sleep blocks the THREAD. The coroutine keeps the thread while it sleeps,
           so the next coroutine can only start when the sleep is over.
           Never use Thread.sleep, or any other blocking call, in a coroutine.
  Try:     use sayHelloWithDelay(). Now all three hellos come after 1 second.
 */
fun main() {
    val ms = measureTimeMillis {
        runBlocking { startCoroutines() }
    }
    logger.info("3 coroutines took {} ms", ms)
}

private fun CoroutineScope.startCoroutines() {
    repeat(3) {
        launch { sayHelloWithSleep() }
    }
}
