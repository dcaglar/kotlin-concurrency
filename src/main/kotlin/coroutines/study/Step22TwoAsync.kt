package coroutines.study

import coroutines.workshop.common.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

/*
  Step 22 - step 20 with async. Two answers at the same time.

  Look at: coroutine#2 and coroutine#3 find their answers at the same moment. 1 second in total.
  Why:     both coroutines are started before anybody waits. They wait for their answer at the same time.
  Try:     write  val a = async { findAnswerWithDelay() }.await()  . It takes 2 seconds again. Why?
 */
fun main() {
    val ms = measureTimeMillis {
        runBlocking { addTwoAnswers() }
    }
    logger.info("it took {} ms", ms)
}

private suspend fun CoroutineScope.addTwoAnswers() {
    val a = async { findAnswerWithDelay() }
    val b = async { findAnswerWithDelay() }
    logger.info("the sum is {}", a.await() + b.await())
}
