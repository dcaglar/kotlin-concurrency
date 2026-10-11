package study

// One problem for every case: approximate pi with the Leibniz series.
// pi/4 = 1 - 1/3 + 1/5 - 1/7 + ...   Pure CPU work, no waiting.

const val TERMS = 400_000_000L   // lower this if a run takes too long on your machine
const val CHUNKS = 8             // number of pieces we split the work into

/** Plain function. Blocks whatever thread calls it until done. */
fun leibniz(start: Long, end: Long): Double {
    var result = 0.0
    for (k in start until end) {
        result += (if (k % 2 == 0L) 1.0 else -1.0) / (2 * k + 1)
    }
    return result
}

/** Prints the message together with the thread it runs on. */
fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")
