package study

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

/*
  Case 7 - exercise 2 in miniature: work that WAITS instead of computing.

  Part A: N threads that each sleep 1 second.
  Part B: N coroutines that each delay 1 second.

  Expect: both take ~1 s at N = 5_000, but watch memory/thread count. Raise N to 100_000:
  part A dies with "unable to create native thread", part B still takes ~1 s on one thread.

  Why: sleep() blocks an OS thread. delay() suspends the coroutine and gives the thread back,
  so 100k waiting coroutines cost 100k small objects, not 100k threads.
 */
const val N = 5_000

fun main() {
    val a = measureTimeMillis {
        val threads = List(N) { thread { Thread.sleep(1_000) } }
        threads.forEach { it.join() }
    }
    log("A) $N threads sleeping 1s:    $a ms")

    val b = measureTimeMillis {
        runBlocking {
            repeat(N) { launch { delay(1_000) } }
        }   // runBlocking waits for all N children
    }
    log("B) $N coroutines delaying 1s: $b ms")
}
