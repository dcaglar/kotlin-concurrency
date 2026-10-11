package coroutines.study

import coroutines.workshop.common.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

/*
  Step 23 - new: coroutineScope { }. A suspend function that starts coroutines by itself.

  Look at: the same output as step 22, 1 second in total.
  Why:     logSum() is a plain suspend function: its caller does not have to give it a scope.
           coroutineScope opens a scope, waits until all coroutines in it are finished,
           and returns the result. Nothing is still running when logSum() returns.
  Try:     add a log line directly after coroutineScope. Does it come before or after the answers?
 */
fun main() {
    val ms = measureTimeMillis {
        runBlocking { logSum() }
    }
    logger.info("it took {} ms", ms)
}

private suspend fun logSum() {
    val sum = coroutineScope { addTwoAnswers() }
    logger.info("the sum is {}", sum)
}

private suspend fun CoroutineScope.addTwoAnswers(): Int {
    val a = async { findAnswerWithDelay() }
    val b = async { findAnswerWithDelay() }
    return a.await() + b.await()
}
