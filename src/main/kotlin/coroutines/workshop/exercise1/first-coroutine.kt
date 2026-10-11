package coroutines.workshop.exercise1

import coroutines.workshop.common.logger
import kotlinx.coroutines.Dispatchers
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

 //  runwithThread()
        /*
        1:40:36.815 [main] INFO  exercise - Just started
11:40:36.818 [main] INFO  exercise - world
11:40:36.818 [Thread-0] INFO  exercise - Hello
         */

  // runwithThreadAndSleep()
    /*
    //when we add sleep order change hellow world.
17:38:46.420 [main]                                        INFO  exercise - Just started
17:38:46.422 [Thread-0]                                    INFO  exercise - Hello , new thread runs first
17:38:51.428 [main]                                        INFO  exercise - world main thread wait 5 second
     */

   // runwithCoroutineAndSleep()
    /*
17:42:34.987 [main]                                        INFO  exercise - Just started
17:42:40.014 [main @coroutine#1]                           INFO  exercise - world
17:42:40.024 [main @coroutine#2]                           INFO  exercise - Hello
notice how chil coroutine wait also for 5 second, because there is only one thread, 2 coroutine use one thread only
     */


  //  runwithCoroutineOnDefaultDispatcherAndSleep()
    /*
    17:46:17.210 [main]                                        INFO  exercise - Just started
17:46:17.234 [DefaultDispatcher-worker-2 @coroutine#2]     INFO  exercise - Hello
17:46:22.239 [DefaultDispatcher-worker-1 @coroutine#1]     INFO  exercise - world
notice coruitine did not wait for 5 second. becasuse dispatcjer default allow to create multiple user, equvalent to runwithThreadAndSleep
     */

   runwithCoroutineAndDelay()
/*
11:42:21.257 [main] INFO  exercise - Just started
11:42:21.283 [main @coroutine#2] INFO  exercise - Hello
11:42:22.287 [main @coroutine#1] INFO  exercise - world

notice it looks same but not exactly because 2 coorotune run ion siingle thread, but because delay is not blocking, thread can pick up other coroutine

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
        Thread.sleep(5000)
        logger.info("world")
    }
}

fun  runwithCoroutineOnDefaultDispatcherAndSleep(){
    runBlocking(Dispatchers.Default) {
        launch {
            logger.info("Hello")
        }
        Thread.sleep(5000)
        logger.info("world")
    }
}



fun  runwithCoroutineAndDelay(){
    runBlocking {
        launch {
            logger.info("Hello")
        }
         delay(5000.milliseconds)
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
    Thread.sleep(5000L)
    logger.info("world")

}