package coroutines.study

import coroutines.workshop.common.logger
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

/*
  Step 9 - 100_000 threads. Each one runs sayHelloWithSleep().

  Look at: the program CRASHES: "OutOfMemoryError: unable to create native thread".
           The threads that did start still say hello. Count them: about 4_000 on macOS.
  Why:     every thread is a real thread of the operating system with its own memory (about 1 MB).
           A sleeping thread does nothing, but it still exists.
  Try:     set MANY in Common.kt to 2_000. Now it works: all hellos after 1 second.
 */
fun main() {
    val ms = measureTimeMillis {
        val threads = List(MANY) { thread { sayHelloWithSleep() } }
        threads.forEach { it.join() }
    }
    logger.info("{} threads took {} ms", MANY, ms)
}
