package coroutines.study

import coroutines.workshop.common.logger
import kotlinx.coroutines.runBlocking

/*
  Step 7 - step 6 with delay instead of sleep. New: a suspend function.

  Look at: the same output as step 6. With ONE coroutine you see no difference between sleep and delay.
  Why:     sayHelloWithDelay() is a suspend function (open it in Common.kt). Only a coroutine can call
           a suspend function. runBlocking is the door from a plain main() into coroutines.
  Try:     remove runBlocking { } and call sayHelloWithDelay() directly. Read the compiler error.
 */
fun main() {
    logger.info("before runBlocking")
    runBlocking { sayHelloWithDelay() }
    logger.info("after runBlocking")
}
