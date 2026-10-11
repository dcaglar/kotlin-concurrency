package coroutines.study

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/*
  Step 15 - step 12 with countWithYield(). The coroutines take turns.

  Look at: A counts 1, B counts 1, A counts 2, B counts 2, ... Still ONE thread.
  Why:     countWithYield() gives the thread away after every number (open it in Common.kt).
           This is concurrency: two tasks make progress together. It is not parallelism.
  Try:     use count("B") for the second coroutine. What is the order now, and why?
 */
fun main() {
    runBlocking { parent() }
}

private fun CoroutineScope.parent() {
    launch { countWithYield("A") }
    launch { countWithYield("B") }
}
