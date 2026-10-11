package coroutines.study

import coroutines.workshop.common.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/*
  Step 40 - step 32 without a Mutex: the counter belongs to ONE coroutine, the others send it messages.

  Look at: the counter is 1_000_000, every run. The senders run on many threads, as in step 31. No lock.
  Why:     in step 31 the counter was shared, so two threads could change it at the same time.
           Here the counter is a local variable of addAll(): no other coroutine can even see it.
           The 1_000 senders do not add, they send "add 1". One coroutine does all the adding,
           one message after the other. "Do not communicate by sharing memory;
           share memory by communicating."
  Try:     time it and compare with step 32. Then try Channel<Int>(Channel.BUFFERED).
 */
fun main() {
    val counter = runBlocking { countWithChannel() }
    logger.info("the counter is {}, expected is {}", counter, COROUTINES * ADDS)
}

private suspend fun countWithChannel(): Int = coroutineScope {
    val adds = Channel<Int>()
    val counter = async { addAll(adds) }
    coroutineScope {
        repeat(COROUTINES) {
            launch(Dispatchers.Default) { sendManyAdds(adds) }
        }
    }
    adds.close()
    counter.await()
}

/** The only coroutine that touches the counter. */
private suspend fun addAll(adds: ReceiveChannel<Int>): Int {
    var counter = 0
    for (add in adds) {
        counter += add
    }
    return counter
}

private suspend fun sendManyAdds(adds: SendChannel<Int>) {
    repeat(ADDS) { adds.send(1) }
}
