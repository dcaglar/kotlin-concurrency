package coroutines.study

import coroutines.workshop.common.logger
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield

// The standard functions of the study steps. Every thread and every coroutine in the steps runs one of these.

// ---------- waiting: every wait is 1 second ----------

const val WAIT_MS = 10_000L       // every sleep and every delay
const val PATIENCE_MS = 35000L   // how long a parent waits before it gives up: 3 ticks fit in, the 4th does not
const val MANY = 100_000         // "a lot" of threads or coroutines

/** Waits 1 second with sleep, then logs. Plain function: main, a thread or a coroutine can call it. */
fun sayHelloWithSleep() {
    Thread.sleep(WAIT_MS)
    logger.info("hello, after 1 second of sleep")
}

/** Waits 1 second with delay, then logs. Suspend function: only a coroutine can call it. */
suspend fun sayHelloWithDelay() {
    delay(WAIT_MS)
    logger.info("hello, after 1 second of delay")
}

/** Needs 1 second (sleep) to find the answer. */
fun findAnswerWithSleep(): Int {
    Thread.sleep(WAIT_MS)
    logger.info("I found the answer 42")
    return 42
}

/** Needs 1 second (delay) to find the answer. */
suspend fun findAnswerWithDelay(): Int {
    delay(WAIT_MS)
    logger.info("I found the answer 42")
    return 42
}

/** Logs a tick every second (delay). Never stops by itself. */
suspend fun tickWithDelay() {
    var tick = 0
    while (true) {
        delay(WAIT_MS)
        logger.info("tick {}", ++tick)
    }
}

/** Logs a tick every second (sleep). Stops after 5 ticks. */
fun tickWithSleep() {
    repeat(5) {
        Thread.sleep(WAIT_MS)
        logger.info("tick {}", it + 1)
    }
}

// ---------- counting: busy, not waiting ----------

/** Counts to 3. It is busy for a moment before every number. It never gives the thread away. */
fun count(name: String) {
    repeat(3) {
        beBusy()
        logger.info("{} counts {}", name, it + 1)
    }
}

/** Same as count(), but gives the thread away after every number. */
suspend fun countWithYield(name: String) {
    repeat(3) {
        beBusy()
        logger.info("{} counts {}", name, it + 1)
        yield()
    }
}

/** Keeps the thread busy for a moment: it computes, it does not wait. */
fun beBusy() {
    leibniz(0, 50_000_000)
}

// ---------- computing: approximate pi with the Leibniz series ----------
// pi/4 = 1 - 1/3 + 1/5 - 1/7 + ...   Pure CPU work, no waiting.

const val TERMS = 400_000_000L   // lower this if a run takes too long on your machine
const val CHUNKS = 8             // number of pieces we split the work into

/** Plain function. Keeps whatever thread calls it busy until done. */
fun leibniz(start: Long, end: Long): Double {
    var result = 0.0
    for (k in start until end) {
        result += (if (k % 2 == 0L) 1.0 else -1.0) / (2 * k + 1)
    }
    return result
}

/** The whole job in one piece. */
fun computePi() {
    val pi = leibniz(0, TERMS) * 4
    logger.info("pi = {}", pi)
}

/** One piece of the job: piece number i of CHUNKS. Writes ONLY its own slot of results. */
fun computeChunk(i: Int, results: DoubleArray) {
    val size = TERMS / CHUNKS
    logger.info("chunk {} starts", i)
    results[i] = leibniz(i * size, (i + 1) * size)
    logger.info("chunk {} is done", i)
}

// ---------- shared counter ----------

const val COROUTINES = 1_000     // so many coroutines add to the same counter
const val ADDS = 1_000           // so many times each

// ---------- channels: one coroutine sends, another receives ----------

const val NUMBERS = 5            // so many numbers go through a channel

/** Sends 1 to 5, as fast as it can. The gap between "I send" and "I sent" is the time send() waited. */
suspend fun sendNumbers(channel: SendChannel<Int>) {
    for (n in 1..NUMBERS) {
        logger.info("I send {}", n)
        channel.send(n)
        logger.info("I sent {}", n)
    }
}

/** Receives until the channel is closed. It needs 1 second (delay) for every number it gets. */
suspend fun receiveSlowly(channel: ReceiveChannel<Int>) {
    for (n in channel) {
        logger.info("I got {}", n)
        delay(WAIT_MS)
    }
}
