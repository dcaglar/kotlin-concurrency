package coroutines.study

import coroutines.workshop.common.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

/*
  Step 21 - new: async { } and await(). A coroutine that returns a value.

  Look at: coroutine#2 finds the answer, coroutine#1 (the parent) logs it.
  Why:     async is launch with a result. It returns a Deferred: a Job (step 16) that also holds a value.
           await() waits until the value is there, and returns it.
           Compare with steps 4 and 5: no shared variable, no join(), and you cannot read too early.
  Try:     log answer.isCompleted before and after await().
 */
fun main() {
    runBlocking { parent() }
}

private suspend fun CoroutineScope.parent() {
    val answer: Deferred<Int> = async { findAnswerWithDelay() }
    logger.info("I wait for the answer")
    logger.info("the answer is {}", answer.await())
}
