package coroutines.study

import coroutines.workshop.common.logger

/*
  Step 1 - a boring program. One thread, one line after the other.

  Look at: every line starts with [main]. The hello comes 1 second after the first line.
  Why:     [main] is the thread the JVM starts your program on. It does everything, the waiting too.
  Try:     call sayHelloWithSleep() twice. How long does the program take now?
 */
fun main() {
    logger.info("first line")
    sayHelloWithSleep()
    logger.info("last line")
}
