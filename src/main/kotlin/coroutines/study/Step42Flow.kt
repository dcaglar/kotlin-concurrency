package coroutines.study

import coroutines.workshop.common.logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

/*
  Step 42 - new: Flow. A function that gives you values one by one, while it is still working.

  Look at: "I send 1", "I got 1", "I send 2", "I got 2", ... one pair per second. All in coroutine#1.
  Why:     a suspend function returns ONE value at the end (step 21). A flow sends many values,
           each one as soon as it is ready (emit). collect { } takes them one by one.
           The sender and the receiver take turns in the same coroutine: this is a generator.
           Compare step 33: with a channel, the sender and the receiver are two coroutines.
  Try:     send 5 numbers instead of 3.
 */
fun main() {
    runBlocking { receiveNumbers() }
}

private fun numbers(): Flow<Int> = flow {
    for (n in 1..3) {
        delay(WAIT_MS)
        logger.info("I send {}", n)
        emit(n)
    }
}

private suspend fun receiveNumbers() {
    numbers().collect { logger.info("I got {}", it) }
}
