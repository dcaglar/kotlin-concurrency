package coroutines.workshop.example

import coroutines.workshop.common.logger
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {

        suspend fun one(): Int {
            logger.info("Finding the answer to the most important question in universe...")
            delay(1_000)
            logger.info("... found!")
            return 42
        }

        suspend fun two(): Int {
            logger.info("Finding the question...")
    //        throw ArithmeticException()
            delay(1_000)
            logger.info("... asked!")
            return 1
        }

        try {
            coroutineScope {

                val een = async { one() }
                val twee = async { two() }

                logger.info("{}", een.await() + twee.await())

            }
        } catch (e: Exception) {
            logger.info("Coroutines have thrown {}", e::class.java)
        }

    }
}
