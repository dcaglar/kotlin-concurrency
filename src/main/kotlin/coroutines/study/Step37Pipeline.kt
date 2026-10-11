package coroutines.study

import coroutines.workshop.common.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.runBlocking

/*
  Step 37 - a pipeline: numbers -> squares -> receiver. Three coroutines, two channels.

  Look at: coroutine#2 sends the numbers, coroutine#3 squares them, coroutine#1 gets 1, 4, 9, 16, 25.
  Why:     a stage receives from one channel and sends into the next. Each stage is its own coroutine
           and only knows its two channels. When numbers() closes its channel, the for loop in
           squareAll() ends, so squares() closes its channel too: the close flows down the pipeline.
  Try:     add a third stage that adds 1 to every value.
 */
fun main() {
    runBlocking { parent() }
}

private suspend fun CoroutineScope.parent() {
    receiveSlowly(squares(numbers()))
    logger.info("the pipeline is empty, I am finished")
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun CoroutineScope.numbers(): ReceiveChannel<Int> = produce { sendNumbers(channel) }

@OptIn(ExperimentalCoroutinesApi::class)
private fun CoroutineScope.squares(numbers: ReceiveChannel<Int>): ReceiveChannel<Int> =
    produce { squareAll(numbers, channel) }

private suspend fun squareAll(numbers: ReceiveChannel<Int>, squares: SendChannel<Int>) {
    for (n in numbers) {
        logger.info("I square {}", n)
        squares.send(n * n)
    }
}
