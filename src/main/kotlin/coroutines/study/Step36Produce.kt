package coroutines.study

import coroutines.workshop.common.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.runBlocking

/*
  Step 36 - new: produce { }. Step 34 in less code: launch + channel + close() in one call.

  Look at: the same output as step 34. The sender is still coroutine#2.
  Why:     produce starts a coroutine with its own channel and returns the receiving end.
           When the block is finished, the channel is closed for you: no close() to forget.
           numbers() returns a ReceiveChannel, so the caller can only receive, never send.
           (produce is marked experimental, hence the @OptIn.)
  Try:     throw an exception in numbers() after the 3rd number. What does the receiver see?
 */
fun main() {
    runBlocking { parent() }
}

private suspend fun CoroutineScope.parent() {
    receiveSlowly(numbers())
    logger.info("the channel is closed, I am finished")
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun CoroutineScope.numbers(): ReceiveChannel<Int> = produce { sendNumbers(channel) }
