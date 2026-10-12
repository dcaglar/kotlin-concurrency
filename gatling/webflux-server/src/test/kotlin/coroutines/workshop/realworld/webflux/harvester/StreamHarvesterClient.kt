package coroutines.workshop.realworld.webflux.harvester

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.core.ConsoleAppender
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlow
import java.io.File
import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.TreeMap
import java.util.TreeSet
import kotlin.math.roundToLong

// Start the video streaming events server first (see docs/streamingplatform.md), then run this main.
// Optional args: [baseUrl] [reportFile]
// Credentials can be overridden with the STREAM_USERNAME / STREAM_PASSWORD environment variables.
fun main(args: Array<String>) = runBlocking {
    logToStderr()

    val client = WebClient.builder()
        .baseUrl(args.getOrNull(0) ?: "http://localhost:8080")
        .defaultHeaders {
            it.setBasicAuth(
                System.getenv("STREAM_USERNAME") ?: "sytac",
                System.getenv("STREAM_PASSWORD") ?: "4p9g-Dv7T-u8fe-iz6y-SRW2",
            )
        }
        .build()

    val sources = PLATFORMS.associateWith { platform ->
        client.get()
            .uri("/{path}", platform.lowercase())
            .accept(MediaType.TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlow<ServerSentEvent<String>>() // raw data: we parse it ourselves so malformed messages can be skipped
    }

    val report = harvest(sources)

    val json = REPORT_WRITER.writeValueAsString(report)
    val reportFile = File(args.getOrNull(1) ?: "target/harvest-report.json")
    reportFile.parentFile?.mkdirs()
    reportFile.writeText(json)
    println(json)
    log("report written to ${reportFile.absolutePath}")
}

val PLATFORMS = listOf("Sytflix", "Sytazon", "Sysney")

const val STREAM_STARTED = "stream-started"
const val STREAM_FINISHED = "stream-finished"

enum class StopReason { TIMEOUT, STOP_USER_LIMIT_REACHED, ALL_STREAMS_ENDED }

/**
 * Consumes all [sources] concurrently. Stops at whichever comes first:
 *  - [timeoutMillis] has passed
 *  - the [stopCount]th event of a user named [stopFirstName] (on any stream) has been received
 * Cancelling the merged flow closes all the HTTP connections.
 *
 * Aggregation is partitioned by user id: each of the [partitionCount] partitions has its own channel, worker
 * coroutine and [HarvestAggregator], so all state of a user lives in one partition and needs no locking.
 * The partitions are combined into one report at the end.
 */
suspend fun harvest(
    sources: Map<String, Flow<ServerSentEvent<String>>>,
    timeoutMillis: Long = 20_000,
    stopFirstName: String = "Sytac",
    stopCount: Int = 3,
    today: LocalDate = LocalDate.now(),
    partitionCount: Int = 3,
): HarvestReport = coroutineScope {
    val aggregators = List(partitionCount) { HarvestAggregator(today) }
    val partitions = List(partitionCount) { Channel<StreamEvent>(capacity = 64) }
    val workers = partitions.mapIndexed { i, partition ->
        launch(Dispatchers.Default) {
            for (event in partition) aggregators[i].add(event) // ends when the partition is closed and drained
        }
    }

    // Only touched from the single router coroutine running firstOrNull's predicate; merge() hands events over to it.
    var skipped = 0
    val stopUserEvents = HashSet<EventKey>() // distinct, so a repeated event does not count twice towards the stop
    var stopReason = StopReason.TIMEOUT

    val startNanos = System.nanoTime()
    withTimeoutOrNull(timeoutMillis) {
        val streams = sources.map { (platform, flow) ->
            flow.map { sse -> parseEvent(platform, sse.event(), sse.data()) }
                // one broken stream must not stop the others
                .catch { e -> log("$platform stream failed: $e") }
        }
        val stopEvent = merge(*streams.toTypedArray()).firstOrNull { event ->
            if (event == null) {
                skipped++
                false
            } else {
                partitions[Math.floorMod(event.user.id, partitionCount.toLong()).toInt()].send(event)
                event.user.firstName == stopFirstName && stopUserEvents.add(event.key()) && stopUserEvents.size == stopCount
            }
        }
        stopReason = if (stopEvent != null) StopReason.STOP_USER_LIMIT_REACHED else StopReason.ALL_STREAMS_ENDED
    }
    val consumedMillis = (System.nanoTime() - startNanos) / 1_000_000

    // The streams are closed; let the workers aggregate what is still queued, then combine the partitions.
    partitions.forEach { it.close() }
    workers.joinAll()

    log("stopped: $stopReason after ${consumedMillis}ms, ${aggregators.sumOf { it.totalEvents }} events, " +
        "${aggregators.sumOf { it.duplicates }} duplicates, $skipped skipped")
    HarvestAggregator.report(aggregators, consumedMillis, stopReason, skipped)
}

// ---- parsing -------------------------------------------------------------------------------------------------

private val EVENT_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSS")
private val DATE_OF_BIRTH_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val AMSTERDAM: ZoneId = ZoneId.of("Europe/Amsterdam")

/** `event_date` is in the user's local time zone; Amsterdam is the default. */
private val ZONE_BY_COUNTRY: Map<String, ZoneId> = mapOf(
    "PT" to "UTC", // as in the hint, and verified against the feed: not Europe/Lisbon, which is UTC+1 in summer
    "CA" to "America/Toronto",
    "US" to "America/Los_Angeles",
    "RU" to "Europe/Moscow",
    "ID" to "Asia/Jakarta",
    "CN" to "Asia/Shanghai",
).mapValues { ZoneId.of(it.value) }

private val PARSER = jacksonObjectMapper()
    .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

private data class RawEvent(val show: RawShow, val eventDate: String, val user: RawUser)
private data class RawShow(val showId: String, val title: String, val cast: String? = null, val releaseYear: Int? = null)
private data class RawUser(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: String,
    val country: String? = null,
)

data class User(val id: Long, val firstName: String, val lastName: String, val dateOfBirth: LocalDate)

data class StreamEvent(
    val type: String,
    val platform: String,
    val showId: String,
    val showTitle: String,
    val firstCastMember: String?,
    val releaseYear: Int?,
    val time: ZonedDateTime, // in Amsterdam time
    val user: User,
)

/** Returns null for messages that are not well-formed (the server is known to send some). */
fun parseEvent(platform: String, type: String?, data: String?): StreamEvent? {
    if (type == null || data == null) return null
    return try {
        val raw = PARSER.readValue<RawEvent>(data)
        val userZone = raw.user.country?.let { ZONE_BY_COUNTRY[it] } ?: AMSTERDAM
        StreamEvent(
            type = type,
            platform = platform,
            showId = raw.show.showId,
            showTitle = raw.show.title,
            firstCastMember = raw.show.cast?.split(",")?.first()?.trim()?.takeIf { it.isNotEmpty() },
            releaseYear = raw.show.releaseYear,
            time = LocalDateTime.parse(raw.eventDate, EVENT_DATE_FORMAT).atZone(userZone).withZoneSameInstant(AMSTERDAM),
            user = User(
                id = raw.user.id,
                firstName = raw.user.firstName,
                lastName = raw.user.lastName,
                dateOfBirth = LocalDate.parse(raw.user.dateOfBirth, DATE_OF_BIRTH_FORMAT),
            ),
        )
    } catch (e: JsonProcessingException) {
        null
    } catch (e: DateTimeException) {
        null
    }
}

// ---- aggregation ---------------------------------------------------------------------------------------------

data class HarvestReport(
    val consumedMillis: Long,
    val stopReason: StopReason,
    val totalEvents: Int,
    /** the same event (user, time, platform, type, show) received again: kept once */
    val duplicateEventsIgnored: Int,
    val skippedMalformedMessages: Int,
    /** distinct shows (show id + platform) released in 2020 or later, across all events, users and platforms */
    val showsReleasedIn2020OrLater: Int,
    val sytflixStreamStartedPercentage: Double,
    val users: List<UserReport>,
)

data class UserReport(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val age: Int,
    val successfulStreamingEvents: Int,
    val events: List<EventReport>,
)

data class EventReport(
    val event: String,
    val platform: String,
    val showId: String,
    val showTitle: String,
    val firstCastMember: String?,
    /** Amsterdam time, dd-MM-yyyy HH:mm:ss.SSS */
    val eventTime: String,
)

/**
 * Aggregates events as they arrive, into structures shaped like the report, so producing the report
 * needs no grouping, sorting or counting over all events.
 *
 * Not thread-safe: each instance is fed by a single coroutine (one per partition).
 */
class HarvestAggregator(private val today: LocalDate) {
    private val users = TreeMap<Long, UserAggregate>() // ordered by user id, as in the report
    private val recentShows = HashSet<Pair<String, String>>() // (platform, show id) released in 2020 or later
    private var sytflixEvents = 0
    private var sytflixStarted = 0

    var totalEvents = 0
        private set
    var duplicates = 0
        private set

    /** Idempotent: an event that was already added changes nothing and gives false. */
    fun add(event: StreamEvent): Boolean {
        val isNew = users.getOrPut(event.user.id) { UserAggregate(event.user, Period.between(event.user.dateOfBirth, today).years) }
            .add(event)
        if (!isNew) {
            duplicates++
            return false
        }
        totalEvents++
        if ((event.releaseYear ?: 0) >= 2020) recentShows += event.platform to event.showId
        if (event.platform == "Sytflix") {
            sytflixEvents++
            if (event.type == STREAM_STARTED) sytflixStarted++
        }
        return true
    }

    fun report(consumedMillis: Long, stopReason: StopReason, skipped: Int) =
        report(listOf(this), consumedMillis, stopReason, skipped)

    companion object {
        /** Combines partitions. A user lives in exactly one partition, so users are simply put together. */
        fun report(partitions: List<HarvestAggregator>, consumedMillis: Long, stopReason: StopReason, skipped: Int): HarvestReport {
            val sytflixEvents = partitions.sumOf { it.sytflixEvents }
            val sytflixStarted = partitions.sumOf { it.sytflixStarted }
            return HarvestReport(
                consumedMillis = consumedMillis,
                stopReason = stopReason,
                totalEvents = partitions.sumOf { it.totalEvents },
                duplicateEventsIgnored = partitions.sumOf { it.duplicates },
                skippedMalformedMessages = skipped,
                // the same show can be seen in several partitions: union, not a sum of sizes
                showsReleasedIn2020OrLater = partitions.flatMapTo(HashSet()) { it.recentShows }.size,
                sytflixStreamStartedPercentage =
                    if (sytflixEvents == 0) 0.0
                    else (sytflixStarted * 10_000.0 / sytflixEvents).roundToLong() / 100.0, // 2 decimals
                // each partition is ordered by id; ordering the combined user summaries (not events) is cheap
                users = partitions.flatMap { it.users.values }.sortedBy { it.id }.map { it.report() },
            )
        }
    }
}

/**
 * One user's events, in time order at every moment.
 *
 * Events come from three connections, so they can arrive out of time order (e.g. a `stream-finished` before
 * its `stream-started`); the TreeSet puts each one in its place. Its comparator is also what makes two events
 * the same event: receiving one again changes nothing (idempotent).
 */
private class UserAggregate(private val user: User, private val age: Int) {
    val id get() = user.id

    private val events = TreeSet(SAME_USER_EVENT_ORDER)

    /** False when the same event was already there. */
    fun add(event: StreamEvent): Boolean = events.add(event)

    fun report() = UserReport(
        id = user.id,
        firstName = user.firstName,
        lastName = user.lastName,
        age = age,
        // counted when the report is made: by then no late event can land between a start and a finish
        successfulStreamingEvents = events.zipWithNext().count { (first, second) -> isSuccessfulStream(first, second) },
        events = events.map {
            EventReport(it.type, it.platform, it.showId, it.showTitle, it.firstCastMember, it.time.format(EVENT_DATE_FORMAT))
        },
    )
}

/** Time first; the rest only separates different events of one user at the same moment. */
private val SAME_USER_EVENT_ORDER: Comparator<StreamEvent> =
    compareBy<StreamEvent>({ it.time.toInstant() }, { it.platform }, { it.type }, { it.showId })

/** Identity of an event across users, for the stop rule: the same fields the per-user TreeSet compares. */
private data class EventKey(val userId: Long, val time: Instant, val platform: String, val type: String, val showId: String)

private fun StreamEvent.key() = EventKey(user.id, time.toInstant(), platform, type, showId)

/** A `stream-started` immediately followed (in time) by a `stream-finished` of the same show on the same platform. */
private fun isSuccessfulStream(first: StreamEvent, second: StreamEvent) =
    first.type == STREAM_STARTED && second.type == STREAM_FINISHED &&
        first.showId == second.showId && first.platform == second.platform

// ---- output --------------------------------------------------------------------------------------------------

private val REPORT_WRITER = jacksonObjectMapper().writerWithDefaultPrettyPrinter()

// stderr, so stdout only contains the JSON report
private fun log(msg: String) = System.err.println("${java.time.Instant.now()} $msg")

/** Logback's default console appender writes to stdout; move it to stderr and drop the DEBUG noise. */
private fun logToStderr() {
    val root = (LoggerFactory.getILoggerFactory() as LoggerContext).getLogger(Logger.ROOT_LOGGER_NAME)
    root.level = Level.INFO
    (root.getAppender("console") as? ConsoleAppender<*>)?.apply {
        stop()
        target = "System.err"
        start()
    }
}
