package study

import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

/*
  Case 3 - split into CHUNKS pieces, one OS thread per piece.

  Expect: roughly (case 1 time) / (number of cores). Log lines show different thread names
  and "start" lines all appear before "done" lines: they really run in parallel.

  Notice: no runBlocking anywhere. leibniz() is a plain function, so plain threads can call it.
 */
fun main() {
    val ms = measureTimeMillis {
        val results = DoubleArray(CHUNKS)          // each thread writes ONLY its own slot -> no race
        val size = TERMS / CHUNKS

        val threads = List(CHUNKS) { i ->
            thread(name = "worker-$i") {
                log("chunk $i start")
                results[i] = leibniz(i * size, (i + 1) * size)
                log("chunk $i done")
            }
        }
        threads.forEach { it.join() }              // wait for all of them
        log("pi = ${results.sum() * 4}")
    }
    log("threads: $ms ms")
}
