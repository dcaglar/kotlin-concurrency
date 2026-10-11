package coroutines.study

import kotlin.concurrent.thread

/*
  Step 11 - two threads count to 3. PREEMPTIVE.

  Look at: the lines of A and B are mixed. Run it again: the order changes.
  Why:     with threads the operating system decides who runs. It can give a thread a core and
           take it away at any moment. count() does nothing for that (open it in Common.kt).
  Try:     run it three times and compare the order. Then run step 12.
 */
fun main() {
    thread(name = "thread-A") { count("A") }
    thread(name = "thread-B") { count("B") }
}
