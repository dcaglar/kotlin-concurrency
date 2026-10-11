package study

import kotlinx.coroutines.*

/*
  Case 0 - what launch and yield actually DO (no timing, just order of output).

  Read the output top to bottom and compare with the comments.
 */
fun main() = runBlocking {

    log("1) launch schedules the child, it does NOT run yet")
    launch { log("   child runs") }
    log("   parent continues first")
    yield()                       // parent gives up the thread -> child gets to run
    log("   parent resumes after yield")

    log("2) two children, no yield: A runs to the end before B starts")
    val a = launch { repeat(3) { log("   A$it") } }
    val b = launch { repeat(3) { log("   B$it") } }
    joinAll(a, b)

    log("3) two children WITH yield: they take turns")
    val c = launch { repeat(3) { log("   C$it"); yield() } }
    val d = launch { repeat(3) { log("   D$it"); yield() } }
    joinAll(c, d)

    log("4) everything ran on ONE thread: runBlocking's thread")
}
