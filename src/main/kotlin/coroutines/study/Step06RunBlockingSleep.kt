package coroutines.study

import coroutines.workshop.common.logger
import kotlinx.coroutines.runBlocking

/*
  Step 6 - new: runBlocking { }. Your first coroutine. Back to ONE thread.

  Look at: [main], then [main @coroutine#1], then [main] again.
           [main @coroutine#1] means: coroutine#1 is running on the thread main.
  Why:     runBlocking starts a coroutine on the current thread and blocks that thread
           until the coroutine is finished, hence the name.
  Try:     remove runBlocking { } and call sayHelloWithSleep() directly. It still works:
           a plain function does not need a coroutine. Step 7 shows a function that does.
 */
fun main() {
    logger.info("before runBlocking")
    runBlocking { sayHelloWithSleep() }
    logger.info("after runBlocking")
}
