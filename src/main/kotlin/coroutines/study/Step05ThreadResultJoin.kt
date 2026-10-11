package coroutines.study

import coroutines.workshop.common.logger
import kotlin.concurrent.thread

/*
  Step 5 - step 4 + join(). main waits, then reads the value.

  Look at: first the [worker] finds 42, then [main] says the answer is 42.
  Why:     join() makes main wait until the worker has written the variable.
           It works, but YOU have to remember the join(). Step 21 does the same with coroutines.
  Try:     remove worker.join(). You are back in step 4.
 */
private var answer = 0

fun main() {
    val worker = thread(name = "worker") { storeAnswer() }
    worker.join()
    logger.info("the answer is {}", answer)
}

private fun storeAnswer() {
    answer = findAnswerWithSleep()
}
