package study

import kotlin.system.measureTimeMillis

/*
  Case 1 - plain sequential code. No threads, no coroutines.
  This is the baseline. Write the time down.
 */
fun main() {
    val ms = measureTimeMillis {
        val pi = leibniz(0, TERMS) * 4
        log("pi = $pi")
    }
    log("sequential: $ms ms")
}
