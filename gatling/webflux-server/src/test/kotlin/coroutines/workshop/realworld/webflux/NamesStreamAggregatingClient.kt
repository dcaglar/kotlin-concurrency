package coroutines.workshop.realworld.webflux

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlow
import java.time.Instant

// Start WebfluxApplication first, then run this main.
// Consumes the stream for 20 seconds. Receiving and aggregating run concurrently:
// while event N is being aggregated, event N+1 is already being received.
fun main(args: Array<String>) = runBlocking {
    val client = WebClient.create(args.firstOrNull() ?: "http://localhost:8080")
    val countsByName = mutableMapOf<String, Int>() // only touched by the aggregating coroutine
    var aggregated = 0

    withTimeoutOrNull(20_000) {
        client.get()
            .uri("/names_stream")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlow<Tick>()
            .onEach { log("received    seq=${it.seq}") }
            // Everything above runs in its own coroutine and hands events over through a channel,
            // so receiving does not wait for the (slow) aggregation below.
            // Without buffer(), event N+1 would not be read until event N was fully aggregated.
            .buffer(capacity = 10)
            .collect { tick ->
                log("aggregating seq=${tick.seq} ...")
                delay(1500) // simulated slow aggregation work, slower than the 1s event rate
                countsByName[tick.name] = (countsByName[tick.name] ?: 0) + 1
                aggregated++
                log("aggregated  seq=${tick.seq} -> $countsByName")
            }
    }

    log("20 seconds passed, connection closed. aggregated $aggregated events: $countsByName")
}

private fun log(msg: String) = println("${Instant.now()} $msg")
