package coroutines.study

import coroutines.workshop.common.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/*
  Step 32 - new: Mutex. Step 31 with synchronization. The counter is correct again.

  Look at: the counter is 1_000_000 again, on many threads. But the program is slower than step 31.
  Why:     only one coroutine at a time can be inside withLock. The others wait for their turn.
           A Mutex is the lock for coroutines: waiting for it suspends, it does not block the thread.
  Try:     use an AtomicInteger for the counter instead of the Mutex: counter.incrementAndGet().
 */
private var counter = 0
private val mutex = Mutex()

fun main() {
    runBlocking { startCoroutines() }
    logger.info("the counter is {}, expected is {}", counter, COROUTINES * ADDS)
}

private fun CoroutineScope.startCoroutines() {
    repeat(COROUTINES) {
        launch(Dispatchers.Default) { addManyTimes() }
    }
}

private suspend fun addManyTimes() {
    repeat(ADDS) {
        mutex.withLock { addOne() }
    }
}

private fun addOne() {
    counter++
}
