package coroutines.study

import coroutines.workshop.common.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/*
  Step 17 - new: cancel(). The parent stops a child that would never stop by itself.

  Look at: tick 1, tick 2, tick 3, then the parent cancels. There is no tick 4.
  Why:     tickWithDelay() runs forever (open it in Common.kt). cancel() asks the coroutine to stop.
           The coroutine notices it at its next delay, and stops there.
  Try:     remove job.cancel(). The program never ends: stop it with the red button.
 */
fun main() {
    runBlocking { parent() }
}

private suspend fun CoroutineScope.parent() {
    val job = launch { tickWithDelay() }
    delay(PATIENCE_MS)
    logger.info("I cancel the child")
    job.cancel()
    job.join()
    logger.info("the child is cancelled = {}", job.isCancelled)
}
