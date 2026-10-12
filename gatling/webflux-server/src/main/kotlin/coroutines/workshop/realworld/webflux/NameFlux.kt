package coroutines.workshop.realworld.webflux

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

@RestController
class NameFlux {

    @GetMapping("/names", produces = [ MediaType.APPLICATION_JSON_VALUE ])
    suspend fun names(): List<String> {
        delay(500)
        return listOf("Elle", "Mike", "John")
    }

    @GetMapping("/names_flux", produces = [ MediaType.APPLICATION_JSON_VALUE ])
    fun namesFlux() =
        Flux.fromArray(arrayOf("Elle", "Mike", "John"))
            .delaySequence(Duration.of(500, ChronoUnit.MILLIS))
            .collectList() // see https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-codecs-jackson

    // Server-Sent Events: each emitted value is written and flushed as its own `data:` event.
    // The flow never completes; it is cancelled when the client disconnects.
    @GetMapping("/names_stream", produces = [ MediaType.TEXT_EVENT_STREAM_VALUE ])
    fun namesStream(): Flow<ServerSentEvent<Tick>> = flow {
        val names = listOf("Elle", "Mike", "John")
        var seq = 0L
        while (true) {
            val tick = Tick(seq, names[(seq % names.size).toInt()], Instant.now())
            emit(
                ServerSentEvent.builder(tick)
                    .id(seq.toString()) // a reconnecting EventSource sends this back as Last-Event-ID
                    .event("tick")      // event type; browsers can listen with addEventListener("tick", ...)
                    .build()
            )
            seq++
            delay(1000)
        }
    }
}

data class Tick(val seq: Long, val name: String, val time: Instant)