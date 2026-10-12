package coroutines.workshop.realworld.webflux

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlow

// Start WebfluxApplication first, then run this main.
fun main(args: Array<String>) = runBlocking {
    val client = WebClient.create(args.firstOrNull() ?: "http://localhost:8080")

    client.get()
        .uri("/names_stream")
        .accept(MediaType.TEXT_EVENT_STREAM)
        .retrieve()
        .bodyToFlow<Tick>()       // each SSE `data:` event is decoded into a Tick as it arrives
        .collect { println("received: $it") }  // the stream is infinite: this never returns, stop the program to disconnect
}
