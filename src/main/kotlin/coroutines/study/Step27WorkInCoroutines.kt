package study

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

/*
  Case 4 - same split, but one coroutine per piece, inside runBlocking.

  Expect: SAME time as case 1. Every log line says [main].
  "chunk 0 start / chunk 0 done / chunk 1 start / chunk 1 done ..." strictly one after another.

  Why: runBlocking runs all its children on ONE thread. launch gives you concurrency
  (the ability to interleave) but not parallelism. And because leibniz() never suspends,
  the coroutines don't even interleave - each one hogs the thread until it returns.
 */
fun main() = runBlocking {
    val ms = measureTimeMillis {
        val results = DoubleArray(CHUNKS)
        val size = TERMS / CHUNKS

        val jobs = List(CHUNKS) { i ->
            launch {
                log("chunk $i start")
                results[i] = leibniz(i * size, (i + 1) * size)
                log("chunk $i done")
            }
        }
        jobs.forEach { it.join() }
        log("pi = ${results.sum() * 4}")
    }
    log("coroutines, single thread: $ms ms")
}
