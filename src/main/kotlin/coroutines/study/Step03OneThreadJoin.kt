package coroutines.study

import coroutines.workshop.common.logger
import kotlin.concurrent.thread

/*
  Step 3 - new: join(). main waits for the worker.

  Look at: the last [main] line comes AFTER the hello of the worker, 1 second after the others.
  Why:     join() blocks the main thread until the worker is finished.
  Try:     start a second worker and join both. How long does the program take? Why not 2 seconds?
 */
fun main() {
    logger.info("I start the worker")
    val worker = thread(name = "worker") { sayHelloWithSleep() }
    logger.info("I wait for the worker")
    worker.join()
    logger.info("the worker is finished")
}
