package coroutines.study

import coroutines.workshop.common.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/*
  Step 31 - step 30 on Dispatchers.Default: many threads. The counter is WRONG.

  Look at: the counter is less than 1_000_000. Run it again: another number every time.
  Why:     counter++ is three steps: read, add 1, write. Two threads read the same value,
           both add 1, both write: one of the two additions is lost. This is a data race.
  Try:     run it three times and compare the numbers.
 */
private var counter = 0

fun main() {
    runBlocking { startCoroutines() }
    logger.info("the counter is {}, expected is {}", counter, COROUTINES * ADDS)
}

private fun CoroutineScope.startCoroutines() {
    repeat(COROUTINES) {
        launch(Dispatchers.Default) { addManyTimes() }
    }
}

private fun addManyTimes() {
    repeat(ADDS) { counter++ }
}
