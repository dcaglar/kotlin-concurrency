package coroutines.study

import coroutines.workshop.common.logger
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

/*
  Step 19 - new: withTimeout. "You get 3.5 seconds, then I cancel you."

  Look at: tick 1, tick 2, tick 3, then the timeout. There is no tick 4.
  Why:     withTimeout does step 17 for you: it waits, and cancels the work when the time is over.
           It tells you with a TimeoutCancellationException.
  Try:     use withTimeoutOrNull instead. It returns null, there is no exception to catch.
 */
fun main() {
    runBlocking { parent() }
}

private suspend fun parent() {
    try {
        withTimeout(PATIENCE_MS) { tickWithDelay() }
    } catch (e: TimeoutCancellationException) {
        logger.info("the time is over, the ticking was cancelled")
    }
}
