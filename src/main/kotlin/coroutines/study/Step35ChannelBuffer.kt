package coroutines.study

import coroutines.workshop.common.logger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/*
  Step 35 - step 34 with Channel(capacity = 2). The channel has room for 2 values now.

  Look at: "I sent 1", "I sent 2" and "I sent 3" come at once: 1 goes to the receiver, 2 and 3 wait in
           the buffer. The sender is finished after 2 seconds, in step 34 it was after 4 seconds.
           The receiver still gets one number per second.
  Why:     send() only suspends when the buffer is full. The sender can run 2 values ahead of the
           receiver, then it has to wait again. That waiting is backpressure: a fast sender
           is slowed down to the speed of the receiver, so values cannot pile up without limit.
  Try:     use Channel.UNLIMITED. When is the sender finished now? What would happen with a million values?
 */
fun main() {
    runBlocking { parent() }
}

private suspend fun parent() = coroutineScope {
    val channel = Channel<Int>(capacity = 2)
    launch { sendAndClose(channel) }
    receiveSlowly(channel)
    logger.info("the channel is closed, I am finished")
}

private suspend fun sendAndClose(channel: SendChannel<Int>) {
    sendNumbers(channel)
    logger.info("I am finished sending")
    channel.close()
}
