package coroutines.workshop.exercise1

import coroutines.workshop.common.logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread
import kotlin.time.Duration.Companion.milliseconds

/*
 Coroutines have been created to allow explicit concurrency without much overhead.
 Therefore, several constructs were written to facilitate this purpose.

 runBlocking() and launch() are two of those constructs. The first creates a coroutine
 context and does not return until all coroutines started within it complete. The latter
 launches a coroutine concurrently in a different context but does not block or suspend
 the caller.

 Can you reason about the sequence in which the two strings appear in the console?
 What would happen if runBlocking() would not block? Why?
 */
fun main() {
    logger.info("Just started")

    //runwithCoroutine()
    /*
    11:39:34.893 [main] INFO  exercise - Just started
11:39:34.918 [main @coroutine#1] INFO  exercise - world
11:39:34.920 [main @coroutine#2] INFO  exercise - Hello

Process finished with exit code 0
     */

  // runwithThread()
        /*
        1:40:36.815 [main] INFO  exercise - Just started
11:40:36.818 [main] INFO  exercise - world
11:40:36.818 [Thread-0] INFO  exercise - Hello
         */
  //  runwithThreadAndSleep()
    /*
    //when we add sleep order change hellow world.
    11:41:13.165 [main] INFO  exercise - Just started
11:41:13.167 [Thread-0] INFO  exercise - Hello
11:41:14.172 [main] INFO  exercise - world
     */

   // runwithCoroutineAndSleep()
    /*
    //order same world hello , why? because
    11:41:38.562 [main] INFO  exercise - Just started
11:41:39.591 [main @coroutine#1] INFO  exercise - world
11:41:39.598 [main @coroutine#2] INFO  exercise - Hello

     */

   runwithCoroutineAndDelay()
/*
11:42:21.257 [main] INFO  exercise - Just started
11:42:21.283 [main @coroutine#2] INFO  exercise - Hello
11:42:22.287 [main @coroutine#1] INFO  exercise - world

Process finished with exit code 0
 */
}


fun  runwithCoroutine(){
    runBlocking {
        launch {
            logger.info("Hello")
        }
        logger.info("world")
    }
}

fun  runwithCoroutineAndSleep(){
    runBlocking {
        launch {
            logger.info("Hello")
        }
        Thread.sleep(1000)
        logger.info("world")
    }
}



fun  runwithCoroutineAndDelay(){
    runBlocking {
        launch {
            logger.info("Hello")
        }
         delay(1000.milliseconds)
        logger.info("world")
    }
}




fun runwithThread(){
    thread {
        logger.info("Hello")
    }
    logger.info("world")

}


fun runwithThreadAndSleep(){
    thread {
        logger.info("Hello")
    }
    Thread.sleep(1000L)
    logger.info("world")

}