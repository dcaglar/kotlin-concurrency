package study

import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

/*
  Case 2 - same as case 1, but wrapped in runBlocking.

  Expect: SAME time as case 1, same thread ("main").
  runBlocking by itself creates a coroutine scope on the current thread - nothing more.
  It only becomes useful once you launch things inside it.
 */
fun main() = runBlocking {
    val ms = measureTimeMillis {
        val pi = leibniz(0, TERMS) * 4
        log("pi = $pi")
    }
    log("runBlocking only: $ms ms")
}
