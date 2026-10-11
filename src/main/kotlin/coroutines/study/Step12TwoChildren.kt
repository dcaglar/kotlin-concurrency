package coroutines.study

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/*
  Step 12 - two coroutines count to 3. COOPERATIVE.

  Look at: A counts 1 2 3, then B counts 1 2 3. Run it again: always the same order.
  Why:     a coroutine keeps its thread until it gives it up by itself. count() never does.
           Nobody takes the thread away from A, so B has to wait (compare step 11).
  Try:     swap the two launch lines.
 */
fun main() {
    runBlocking { parent() }
}

private fun CoroutineScope.parent() {
    launch { count("A") }
    launch { count("B") }
}
