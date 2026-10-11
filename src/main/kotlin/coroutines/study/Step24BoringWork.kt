package coroutines.study

import coroutines.workshop.common.logger
import kotlin.system.measureTimeMillis

/*
  Step 24 - computing instead of waiting: computePi(). One thread, as in step 1.

  Look at: [main] everywhere. Write the time down: it is the baseline T for steps 25 to 29.
  Why:     computePi() does not wait for anything. It keeps a core busy all the time.
  Try:     run ./computing-cores.sh in this folder. It runs steps 24 to 29 on 1 core and on all cores.
 */
fun main() {
    val ms = measureTimeMillis { computePi() }
    logger.info("it took {} ms", ms)
}
