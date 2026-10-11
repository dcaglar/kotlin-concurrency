package coroutines.study

import coroutines.workshop.common.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

/*
  Step 41 - a channel is HOT: the sender starts at once, and every value can be received only once.

  Look at: "I send 1" comes right after "I made the channel", 1 second BEFORE anybody receives.
           The first receiver gets 1 to 5. The second receiver gets nothing.
  Why:     produce starts its coroutine immediately, like launch. It does not wait for a receiver.
           Receiving takes the values out of the channel, so after the first receiver nothing is left.
           Step 43 does the same with a flow and gets the opposite: a flow is cold.
  Try:     make the channel with produce(capacity = NUMBERS). What does the sender do during the delay?
 */
fun main() {
    runBlocking { receiveTwice() }
}

private suspend fun CoroutineScope.receiveTwice() {
    val channel = numbers()
    logger.info("I made the channel, nobody receives for 1 second")
    delay(WAIT_MS)
    receiveSlowly(channel)
    logger.info("the first receiver is finished")
    receiveSlowly(channel)
    logger.info("the second receiver is finished, it got nothing")
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun CoroutineScope.numbers(): ReceiveChannel<Int> = produce { sendNumbers(channel) }
