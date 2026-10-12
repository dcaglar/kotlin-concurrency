package coroutines.workshop.realworld.webflux

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlow
import java.time.Instant

// Start WebfluxApplication first, then run this main.
// Optional args: [baseUrl] [timeoutMillis]
fun main(args: Array<String>) = runBlocking {
    val client = WebClient.create(args.getOrNull(0) ?: "http://localhost:8080")
    val timeoutMillis = args.getOrNull(1)?.toLong() ?: 20_000

    val counts = countNames(client, timeoutMillis, stopName = "Elle", stopCount = 3)
    log("result: $counts")
}

/**
 * Consumes /names_stream and counts how often each name is seen.
 * Stops (and closes the connection) at whichever comes first:
 *  - [timeoutMillis] has passed
 *  - [stopName] has been seen [stopCount] times
 */
suspend fun countNames(
    client: WebClient,
    timeoutMillis: Long,
    stopName: String,
    stopCount: Int,
): Map<String, Int> {
    val counts = mutableMapOf<String, Int>()

    // firstOrNull cancels the stream as soon as the predicate returns true;
    // withTimeoutOrNull cancels it (and returns null) if that hasn't happened in time.
    val stoppedAt = withTimeoutOrNull(timeoutMillis) {
        client.get()
            .uri("/names_stream")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlow<Tick>()
            .firstOrNull { tick ->
                val count = (counts[tick.name] ?: 0) + 1
                counts[tick.name] = count
                log("received seq=${tick.seq} ${tick.name} -> $counts")
                tick.name == stopName && count == stopCount
            }
    }

    if (stoppedAt != null) log("saw $stopName $stopCount times (seq=${stoppedAt.seq}), connection closed early")
    else log("${timeoutMillis}ms passed, connection closed")

    return counts.toMap()
}

private fun log(msg: String) = println("${Instant.now()} $msg")
