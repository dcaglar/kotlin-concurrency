package coroutines.study

import coroutines.workshop.common.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/*
  Step 18 - step 17 with tickWithSleep(). The cancel comes too late.

  Look at: all 5 ticks appear. The parent wanted to cancel after tick 3, but it logs after tick 5.
           The child is not cancelled: it was already finished.
  Why:     the child blocks the one thread with sleep (step 13). The parent needs that thread
           to call cancel(), and only gets it when the child is done.
           Cancelling is cooperative too: a coroutine that never gives the thread away cannot be stopped.
  Try:     use tickWithDelay(). You are back in step 17.
 */
fun main() {
    runBlocking { parent() }
}

private suspend fun CoroutineScope.parent() {
    val job = launch { tickWithSleep() }
    delay(PATIENCE_MS)
    logger.info("I cancel the child")
    job.cancel()
    job.join()
    logger.info("the child is cancelled = {}", job.isCancelled)
}
