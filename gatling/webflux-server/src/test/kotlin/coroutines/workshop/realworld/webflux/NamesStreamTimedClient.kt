package coroutines.workshop.realworld.webflux

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlow
import java.time.Instant

// Start WebfluxApplication first, then run this main.
// Consumes the infinite stream for 20 seconds, then cancels it, which closes the connection.
fun main(args: Array<String>) = runBlocking {
    val client = WebClient.create(args.firstOrNull() ?: "http://localhost:8080")

    println("${Instant.now()} connecting")
    val result = withTimeoutOrNull(20_000) {
        client.get()
            .uri("/names_stream")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlow<Tick>()
            .collect { println("${Instant.now()} received: $it") }
    }

    // null means the timeout cancelled the collection (the stream itself never completes)
    if (result == null) println("${Instant.now()} 20 seconds passed, connection closed")
}
