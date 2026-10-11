package coroutines.study

import coroutines.workshop.common.logger
import kotlin.concurrent.thread

/*
  Step 2 - new: thread { }. The program has TWO threads now.

  Look at: the three [main] lines come at the same moment. The [worker] hello comes 1 second later.
  Why:     main starts the worker and continues. It does not wait. When the worker says hello,
           the main thread is already gone. The JVM stays open until the last thread is finished.
  Try:     thread(name = "worker", isDaemon = true) { ... }. Where is the hello?
           (The JVM does not wait for daemon threads.)
 */
fun main() {
    logger.info("I start the worker")
    thread(name = "worker") { sayHelloWithSleep() }
    logger.info("I do not wait for the worker")
    logger.info("I am finished")
}
