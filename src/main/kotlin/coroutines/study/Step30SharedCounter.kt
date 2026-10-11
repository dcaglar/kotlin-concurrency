package coroutines.study

import coroutines.workshop.common.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/*
  Step 30 - shared mutable state: 1_000 coroutines add to ONE counter, on ONE thread.

  Look at: the counter is 1_000_000, as expected. Every run.
  Why:     one thread: only one coroutine runs at a time, and addManyTimes() never gives the thread away.
           Nobody can come in between.
  Try:     run it three times. Then run step 31.
 */
private var counter = 0

fun main() {
    runBlocking { startCoroutines() }
    logger.info("the counter is {}, expected is {}", counter, COROUTINES * ADDS)
}

private fun CoroutineScope.startCoroutines() {
    repeat(COROUTINES) {
        launch { addManyTimes() }
    }
}

private fun addManyTimes() {
    repeat(ADDS) { counter++ }
}
