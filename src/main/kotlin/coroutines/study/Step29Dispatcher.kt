package study

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

/*
  Case 6 - same as case 4, one change: launch(Dispatchers.Default).

  Expect: about the same time as case 3 (threads). Log lines show
  [DefaultDispatcher-worker-1], [DefaultDispatcher-worker-2], ...

  Why: the dispatcher decides WHICH threads a coroutine runs on. Dispatchers.Default is a
  pool with one thread per CPU core. runBlocking (cases 4 and 5) is "this one thread only".
  Same code, different dispatcher, real parallelism.
 */
fun main() = runBlocking {
    val ms = measureTimeMillis {
        val results = DoubleArray(CHUNKS)
        val size = TERMS / CHUNKS

        val jobs = List(CHUNKS) { i ->
            launch(Dispatchers.Default) {              // <- the only difference from case 4
                log("chunk $i start")
                results[i] = leibniz(i * size, (i + 1) * size)
                log("chunk $i done")
            }
        }
        jobs.forEach { it.join() }
        log("pi = ${results.sum() * 4}")
    }
    log("coroutines on Dispatchers.Default: $ms ms")
}
