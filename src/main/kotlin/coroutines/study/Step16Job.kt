package coroutines.study

import coroutines.workshop.common.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/*
  Step 16 - new: Job. launch gives you a handle for the coroutine it started.

  Look at: before join the job is active and not completed. After join it is the other way round.
  Why:     a Job is for a coroutine what the Thread object is for a thread (step 3):
           you can ask for its state, wait for it with join(), and cancel it (step 17).
  Try:     remove job.join(). What does the second line say?
 */
fun main() {
    runBlocking { parent() }
}

private suspend fun CoroutineScope.parent() {
    val job: Job = launch { sayHelloWithDelay() }
    logger.info("before join: active = {}, completed = {}", job.isActive, job.isCompleted)
    job.join()
    logger.info("after join:  active = {}, completed = {}", job.isActive, job.isCompleted)
}
