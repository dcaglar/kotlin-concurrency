package coroutines.study

import coroutines.workshop.common.logger
import kotlin.concurrent.thread

/*
  Step 2 - new: thread { }. The program now has TWO threads.

  Expect: lines from [main] and one line from [worker].
  Run it a few times: "main continues" and "hello from worker" can swap places.
  Two threads run at the same time, so nobody decides who logs first.

  sayHello() is the same kind of function as in step 1. Only the caller is different.
 */
fun main() {
    logger.info("main starts a second thread")
    val worker = thread(name = "worker") { sayHello() }
    logger.info("main continues, it does not wait")
    worker.join()                                  // now main waits until worker is finished
    logger.info("worker is finished")
}

private fun sayHello() {
    Thread.sleep(1000L)
    logger.info("hello from worker")
}
