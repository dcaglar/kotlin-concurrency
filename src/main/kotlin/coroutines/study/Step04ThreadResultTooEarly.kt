package coroutines.study

import coroutines.workshop.common.logger
import kotlin.concurrent.thread

/*
  Step 4 - main needs a value from the worker, and reads it too early.

  Look at: [main] says the answer is 0. One second later the [worker] finds 42.
  Why:     a thread cannot return a value. It writes the value into a variable.
           main does not wait (step 2), so it reads the variable before the worker has written it.
           No error, no warning: just a wrong value.
  Try:     put Thread.sleep(2_000) before the log line of main. It works. Why is it still a bad fix?
 */
private var answer = 0

fun main() {
    thread(name = "worker") { storeAnswer() }
    logger.info("the answer is {}", answer)
}

private fun storeAnswer() {
    answer = findAnswerWithSleep()
}
