package coroutines.study

import coroutines.workshop.common.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield

/*
  Step 14 - new: yield(). A coroutine gives the thread away.

  Look at: the child (coroutine#2) runs BETWEEN the two lines of the parent (coroutine#1).
  Why:     yield() says: "let somebody else use the thread, I continue later".
           The child was waiting for the thread (step 8), so it runs now.
           parent() is a suspend function now, because it can pause in the middle.
  Try:     remove yield(). When does the child run?
 */
fun main() {
    runBlocking { parent() }
}

private suspend fun CoroutineScope.parent() {
    launch { child() }
    logger.info("I give the thread away")
    yield()
    logger.info("I have the thread back")
}

private fun child() {
    logger.info("I am the child, I run now")
}
