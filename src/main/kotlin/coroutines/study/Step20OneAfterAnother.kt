package coroutines.study

import coroutines.workshop.common.logger
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

/*
  Step 20 - two answers, one after the other.

  Look at: the two answers are found 1 second after each other. 2 seconds in total.
           Both lines are from coroutine#1: there is only one coroutine.
  Why:     suspend functions run one after the other, like all other code.
           If you want them to run at the same time, you have to say so: step 22.
  Try:     add a third answer.
 */
fun main() {
    val ms = measureTimeMillis {
        runBlocking { addTwoAnswers() }
    }
    logger.info("it took {} ms", ms)
}

private suspend fun addTwoAnswers() {
    val a = findAnswerWithDelay()
    val b = findAnswerWithDelay()
    logger.info("the sum is {}", a + b)
}
