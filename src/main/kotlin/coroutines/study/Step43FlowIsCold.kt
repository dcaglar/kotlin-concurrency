package coroutines.study

import coroutines.workshop.common.logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

/*
  Step 43 - a flow does nothing until somebody collects it. It is COLD.

  Look at: "I made the flow" comes first, and no number is sent yet.
           Then the numbers are sent TWICE, once for every collect.
  Why:     numbers() only describes the work. The work starts at collect, and starts again
           for every collect. A flow is a recipe, not a box of values.
           Compare step 41: a channel is HOT, it sends without anybody receiving, and only once.
  Try:     remove the second collect. How many times are the numbers sent now?
 */
fun main() {
    runBlocking { receiveNumbersTwice() }
}

private fun numbers(): Flow<Int> = flow {
    for (n in 1..3) {
        delay(WAIT_MS)
        logger.info("I send {}", n)
        emit(n)
    }
}

private suspend fun receiveNumbersTwice() {
    val flow = numbers()
    logger.info("I made the flow")
    flow.collect { logger.info("first collect got {}", it) }
    flow.collect { logger.info("second collect got {}", it) }
}
