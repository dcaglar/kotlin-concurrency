package coroutines.study

import coroutines.workshop.common.logger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/*
  Step 39 - fan-in: three senders, one channel, one receiver.

  Look at: every second the receiver gets one message from A, one from B and one from C.
           Three senders, but ONE stream for the receiver, in the order the messages came in.
  Why:     many coroutines can send into the same channel. The receiver does not know or care who sent.
           Close the channel only when ALL senders are finished: coroutineScope waits for them first.
  Try:     close the channel right after the first sender is finished. What happens to the others?
 */
fun main() {
    runBlocking { parent() }
}

private suspend fun parent() = coroutineScope {
    val channel = Channel<String>()
    launch { sendFromThree(channel) }
    receiveMessages(channel)
    logger.info("all senders are finished, I am finished")
}

private suspend fun sendFromThree(channel: SendChannel<String>) {
    coroutineScope {
        for (name in listOf("A", "B", "C")) {
            launch { sendMessages(name, channel) }
        }
    }
    channel.close()
}

/** Sends a message every second, 3 times. */
private suspend fun sendMessages(name: String, channel: SendChannel<String>) {
    repeat(3) {
        delay(WAIT_MS)
        channel.send("$name ${it + 1}")
    }
}

private suspend fun receiveMessages(channel: ReceiveChannel<String>) {
    for (message in channel) {
        logger.info("I got {}", message)
    }
}
