package coroutines.study

import coroutines.workshop.common.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/*
  Step 33 - new: Channel, send(), receive(). One coroutine hands values to another.

  Look at: "I send" lines from coroutine#2, "I got" lines from coroutine#3. From 2 on, every "I sent n"
           comes 1 second after its "I send n": the sender waits at send() until the receiver is ready.
  Why:     steps 30 to 32 SHARED a counter and needed a lock. A channel does not share anything:
           a value goes from one coroutine to the other. Channel() has no room for a value, so
           send() suspends until somebody calls receive(). It is a handoff (a rendezvous), not a queue.
  Try:     receive one number less: repeat(NUMBERS - 1). The program never ends. Where is it stuck?
 */
fun main() {
    runBlocking { parent() }
}

private fun CoroutineScope.parent() {
    val channel = Channel<Int>()
    launch { sendNumbers(channel) }
    launch { receiveAll(channel) }
}

private suspend fun receiveAll(channel: ReceiveChannel<Int>) {
    repeat(NUMBERS) {
        val n = channel.receive()
        logger.info("I got {}", n)
        delay(WAIT_MS)
    }
}
