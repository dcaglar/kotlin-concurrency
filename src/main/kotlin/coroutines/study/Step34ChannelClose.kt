package coroutines.study

import coroutines.workshop.common.logger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/*
  Step 34 - new: close() and for (n in channel). The receiver does not have to know how many come.

  Look at: the same handoff as step 33. After "I got 5" the receiver says it is finished.
  Why:     close() says "nothing more will come". The for loop in receiveSlowly() (open it in Common.kt)
           takes values until the channel is closed AND empty, then it ends by itself.
           No count to keep in sync like the repeat(NUMBERS) of step 33.
  Try:     remove channel.close(). The receiver waits for a 6th number forever.
 */
fun main() {
    runBlocking { parent() }
}

private suspend fun parent() = coroutineScope {
    val channel = Channel<Int>()
    launch { sendAndClose(channel) }
    receiveSlowly(channel)
    logger.info("the channel is closed, I am finished")
}

private suspend fun sendAndClose(channel: SendChannel<Int>) {
    sendNumbers(channel)
    channel.close()
}
