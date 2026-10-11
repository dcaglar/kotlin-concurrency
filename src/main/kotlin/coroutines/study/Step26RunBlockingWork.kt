package coroutines.study

import coroutines.workshop.common.logger
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

/*
  Step 26 - step 24 inside runBlocking. The same computePi(), now called by a coroutine.

  Look at: the pi line is on [main @coroutine#1]. The time is the same as in step 24.
  Why:     a coroutine by itself makes nothing faster. It is still one thread doing all the work.
  Try:     compare the time with step 24 a few times. Is there a real difference?
 */
fun main() {
    val ms = measureTimeMillis {
        runBlocking { computePi() }
    }
    logger.info("it took {} ms", ms)
}
