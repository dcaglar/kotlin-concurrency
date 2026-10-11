package coroutines.study

import coroutines.workshop.common.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/*
  Step 8 - new: launch { }. A second coroutine, a child of the first.

  Look at: coroutine#1 (the parent) logs both its lines first. Then coroutine#2 (the child) says hello.
           The last line comes after the hello, and nobody called join().
  Why:     launch only SCHEDULES the child. The parent is using the one thread, so the child has to wait.
           runBlocking waits for its children by itself (compare step 2 and 3).
           CoroutineScope.parent(): a function that launches a coroutine needs the scope of its caller.
  Try:     launch a second child. How long does the program take?
 */
fun main() {
    runBlocking { parent() }
    logger.info("runBlocking is finished")
}

private fun CoroutineScope.parent() {
    logger.info("I launch a child")
    launch { sayHelloWithDelay() }
    logger.info("I am finished, the child did not run yet")
}
