package coroutines.study

import coroutines.workshop.common.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

/*
  Step 38 - fan-out: one sender, three receivers on the same channel.

  Look at: "I got n" comes from coroutine#3, #4 and #5. Every number is got ONCE, by one of them.
           5 numbers in a bit more than 2 seconds. With one receiver (step 36) it was 5 seconds.
  Why:     receive() takes a value OUT of the channel. Whoever is free takes the next one.
           This is how you share work between workers: a channel is a work queue.
  Try:     launch 5 receivers. Then 10. Does it get faster than 1 second? Why not?
 */
fun main() {
    val ms = measureTimeMillis {
        runBlocking { startReceivers() }
    }
    logger.info("it took {} ms", ms)
}

private fun CoroutineScope.startReceivers() {
    val channel = numbers()
    repeat(3) {
        launch { receiveSlowly(channel) }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun CoroutineScope.numbers(): ReceiveChannel<Int> = produce { sendNumbers(channel) }
