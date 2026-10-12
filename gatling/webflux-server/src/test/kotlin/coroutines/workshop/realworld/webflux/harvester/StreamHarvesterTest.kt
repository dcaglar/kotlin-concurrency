package coroutines.workshop.realworld.webflux.harvester

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.http.codec.ServerSentEvent
import java.time.LocalDate

class StreamHarvesterTest {

    private val today = LocalDate.of(2023, 3, 1)

    // ---- parsing ----

    @Test
    fun `converts event_date from the user's time zone to Amsterdam`() {
        // 03:20 in Los Angeles (UTC-8) = 11:20 UTC = 12:20 in Amsterdam (CET, UTC+1)
        val event = parseEvent("Sysney", "show-liked", json(country = "US", eventDate = "27-02-2023 03:20:17.111"))!!
        assertEquals("27-02-2023 12:20:17.111", report(event).users[0].events[0].eventTime)
    }

    @Test
    fun `treats Portuguese times as UTC, also in summer`() {
        // 12:00 UTC = 14:00 in Amsterdam (CEST, UTC+2)
        val event = parseEvent("Sysney", "show-liked", json(country = "PT", eventDate = "12-10-2026 12:00:00.000"))!!
        assertEquals("12-10-2026 14:00:00.000", report(event).users[0].events[0].eventTime)
    }

    @Test
    fun `uses Amsterdam when the country has no specific time zone`() {
        val event = parseEvent("Sysney", "show-liked", json(country = "TH", eventDate = "27-02-2023 03:20:17.111"))!!
        assertEquals("27-02-2023 03:20:17.111", report(event).users[0].events[0].eventTime)
    }

    @Test
    fun `takes the first cast member, or null when there is no cast`() {
        assertEquals("Kuby Farner", parseEvent("Sysney", "show-liked", json(cast = "Kuby Farner, Rakota Cotus"))!!.firstCastMember)
        assertNull(parseEvent("Sysney", "show-liked", json(cast = ""))!!.firstCastMember)
        assertNull(parseEvent("Sysney", "show-liked", json(cast = null))!!.firstCastMember)
    }

    @Test
    fun `skips malformed messages`() {
        assertNull(parseEvent("Sysney", "show-liked", "{\"show\": {\"show_id\": \"s1\""))  // truncated JSON
        assertNull(parseEvent("Sysney", "show-liked", json().replace("\"title\"", "\"titel\""))) // missing field
        assertNull(parseEvent("Sysney", "show-liked", json(eventDate = "2023-02-27T03:20:17")))  // bad date
        assertNull(parseEvent("Sysney", null, json()))
        assertNull(parseEvent("Sysney", "show-liked", null))
    }

    // ---- aggregation ----

    @Test
    fun `computes age, release-year count and Sytflix started percentage`() {
        val events = listOf(
            parseEvent("Sytflix", STREAM_STARTED, json(showId = "s1", releaseYear = 2021))!!,
            parseEvent("Sytflix", STREAM_FINISHED, json(showId = "s1", releaseYear = 2021))!!, // same show: counted once
            parseEvent("Sytflix", "show-liked", json(showId = "s2", releaseYear = 2019))!!,
            parseEvent("Sysney", STREAM_STARTED, json(showId = "s1", releaseYear = 2020))!!,  // other platform: other show
        )
        val report = aggregate(events)

        assertEquals(52, report.users[0].age) // born 31/10/1970
        assertEquals(2, report.showsReleasedIn2020OrLater)
        assertEquals(33.33, report.sytflixStreamStartedPercentage) // 1 of 3 Sytflix events
        assertEquals(4, report.totalEvents)
    }

    @Test
    fun `combining partitions gives the same report as a single aggregator`() {
        val events = (1L..9L).flatMap { userId ->
            listOf(
                parseEvent("Sytflix", STREAM_STARTED, json(userId = userId, showId = "s$userId", releaseYear = 2021, eventDate = "01-01-2000 00:01:00.000"))!!,
                parseEvent("Sytflix", STREAM_FINISHED, json(userId = userId, showId = "s$userId", releaseYear = 2021, eventDate = "01-01-2000 00:02:00.000"))!!,
                parseEvent("Sysney", "show-liked", json(userId = userId, showId = "shared", releaseYear = 2022))!!, // same show for everyone
            )
        }
        val single = HarvestAggregator(today).apply { events.forEach(::add) }
        val partitions = List(3) { HarvestAggregator(today) }
        events.forEach { partitions[(it.user.id % 3).toInt()].add(it) }

        val combined = HarvestAggregator.report(partitions, 0, StopReason.TIMEOUT, 0)
        assertEquals(single.report(0, StopReason.TIMEOUT, 0), combined)
        assertEquals(10, combined.showsReleasedIn2020OrLater) // s1..s9 + "shared" counted once
        assertEquals((1L..9L).toList(), combined.users.map { it.id })
    }

    @Test
    fun `counts successful streaming events as in the scenario examples`() {
        assertEquals(1, successfulStreams(e(STREAM_STARTED, "s_01", "Sytflix", 1), e(STREAM_FINISHED, "s_01", "Sytflix", 2)))
        assertEquals(0, successfulStreams(e(STREAM_STARTED, "s_01", "Sytflix", 1), e(STREAM_FINISHED, "s_01", "Sytazon", 2)))
        assertEquals(0, successfulStreams(e(STREAM_STARTED, "s_01", "Sytflix", 1), e(STREAM_FINISHED, "s_02", "Sytflix", 2)))
        assertEquals(0, successfulStreams(e(STREAM_STARTED, "s_01", "Sytflix", 1), e("show-liked", "s_01", "Sytflix", 2), e(STREAM_FINISHED, "s_01", "Sytflix", 3)))
        assertEquals(0, successfulStreams(e(STREAM_FINISHED, "s_01", "Sytflix", 1), e(STREAM_STARTED, "s_01", "Sytflix", 2)))
    }

    @Test
    fun `counts successful streaming events when events arrive out of time order`() {
        // finished arrives before its started
        assertEquals(1, successfulStreams(e(STREAM_FINISHED, "s_01", "Sytflix", 2), e(STREAM_STARTED, "s_01", "Sytflix", 1)))
        // a late event lands between a started/finished pair and breaks it
        assertEquals(0, successfulStreams(e(STREAM_STARTED, "s_01", "Sytflix", 1), e(STREAM_FINISHED, "s_01", "Sytflix", 3), e("show-liked", "s_01", "Sytflix", 2)))
        // a late started completes a pair in the middle
        assertEquals(2, successfulStreams(
            e(STREAM_STARTED, "s_01", "Sytflix", 1), e(STREAM_FINISHED, "s_01", "Sytflix", 2),
            e(STREAM_FINISHED, "s_02", "Sytflix", 4), e(STREAM_STARTED, "s_02", "Sytflix", 3),
        ))
    }

    @Test
    fun `adding the same event again changes nothing`() {
        val started = e(STREAM_STARTED, "s_01", "Sytflix", 1)
        val finished = e(STREAM_FINISHED, "s_01", "Sytflix", 2)
        val aggregator = HarvestAggregator(today)

        assertTrue(aggregator.add(started))
        assertFalse(aggregator.add(e(STREAM_STARTED, "s_01", "Sytflix", 1))) // equal event, parsed again
        assertTrue(aggregator.add(finished))
        assertFalse(aggregator.add(finished))

        val report = aggregator.report(0, StopReason.TIMEOUT, 0)
        assertEquals(2, report.totalEvents)
        assertEquals(2, report.duplicateEventsIgnored)
        assertEquals(2, report.users.single().events.size)
        assertEquals(1, report.users.single().successfulStreamingEvents)
        assertEquals(50.0, report.sytflixStreamStartedPercentage) // counters not inflated by the duplicates
    }

    @Test
    fun `same moment but a different action or show is not a duplicate`() {
        val events = listOf(
            e(STREAM_STARTED, "s_01", "Sytflix", 1),
            e("show-liked", "s_01", "Sytflix", 1),
            e(STREAM_STARTED, "s_02", "Sytflix", 1),
            e(STREAM_STARTED, "s_01", "Sytazon", 1),
        )
        assertEquals(4, aggregate(events).users.single().events.size)
    }

    @Test
    fun `orders a user's events by time, not by arrival`() {
        val user = aggregate(listOf(e(STREAM_FINISHED, "s1", "Sytflix", 2), e(STREAM_STARTED, "s1", "Sytazon", 1))).users.single()
        assertEquals(listOf("Sytazon", "Sytflix"), user.events.map { it.platform })
    }

    @Test
    fun `lists users ordered by id`() {
        val events = listOf(30L, 10L, 20L).map { parseEvent("Sytflix", "show-liked", json(userId = it))!! }
        assertEquals(listOf(10L, 20L, 30L), aggregate(events).users.map { it.id })
    }

    // ---- harvesting ----

    @Test
    fun `a repeated Sytac event does not count twice towards the stop`() = runBlocking {
        val sources = mapOf(
            "Sytflix" to flow {
                repeat(3) { emit(sse("Sytac", second = 0)) } // the same event three times
                emit(sse("Sytac", second = 1))
                delay(10_000)
            },
        )
        val report = harvest(sources, timeoutMillis = 300, today = today)

        assertEquals(StopReason.TIMEOUT, report.stopReason) // only 2 distinct Sytac events
        assertEquals(2, report.totalEvents)
        assertEquals(2, report.duplicateEventsIgnored)
    }

    @Test
    fun `stops at the third Sytac event across all streams`() = runBlocking {
        val sources = mapOf(
            "Sytflix" to stream(firstNames = listOf("Sytac", "Anna", "Sytac", "Bob", "Sytac", "Never")),
            "Sytazon" to stream(firstNames = listOf("Carl", "Dora")),
        )
        val report = harvest(sources, timeoutMillis = 5_000, today = today)

        assertEquals(StopReason.STOP_USER_LIMIT_REACHED, report.stopReason)
        assertTrue(report.users.flatMap { it.events }.isNotEmpty())
        assertEquals(3, report.users.single { it.firstName == "Sytac" }.events.size)
        assertTrue(report.users.none { it.firstName == "Never" })
        assertTrue(report.consumedMillis < 5_000)
    }

    @Test
    fun `stops after the timeout and skips malformed messages`() = runBlocking {
        val sources = mapOf(
            "Sytflix" to flow {
                emit(ServerSentEvent.builder("not json").event("show-liked").build())
                emit(sse("Anna"))
                delay(10_000)
            },
        )
        val report = harvest(sources, timeoutMillis = 300, today = today)

        assertEquals(StopReason.TIMEOUT, report.stopReason)
        assertEquals(1, report.totalEvents)
        assertEquals(1, report.skippedMalformedMessages)
        assertTrue(report.consumedMillis in 300..2_000, "consumed ${report.consumedMillis}ms")
    }

    @Test
    fun `a failing stream does not stop the others`() = runBlocking {
        val sources = mapOf(
            "Sytflix" to flow<ServerSentEvent<String>> { throw IllegalStateException("connection reset") },
            "Sytazon" to stream(firstNames = listOf("Anna", "Bob")),
        )
        val report = harvest(sources, timeoutMillis = 300, today = today)

        assertEquals(2, report.totalEvents)
        assertNotNull(report.users.find { it.firstName == "Bob" })
    }

    // ---- helpers ----

    private fun report(event: StreamEvent) = aggregate(listOf(event))

    private fun aggregate(events: List<StreamEvent>) =
        HarvestAggregator(today).apply { events.forEach(::add) }.report(0, StopReason.TIMEOUT, 0)

    /** Events are added in the given (arrival) order. */
    private fun successfulStreams(vararg events: StreamEvent) =
        aggregate(events.toList()).users.single().successfulStreamingEvents

    private fun e(type: String, show: String, platform: String, minute: Int) =
        parseEvent(platform, type, json(showId = show, eventDate = "01-01-2000 00:%02d:00.000".format(minute)))!!

    private fun stream(firstNames: List<String>): Flow<ServerSentEvent<String>> = flow {
        firstNames.forEach { emit(sse(it)); delay(10) }
        delay(10_000)
    }

    private var nextUserId = 1L
    private var nextSecond = 0

    /** Each call is a distinct event (its own second); pass [second] to send the same event again. */
    private fun sse(firstName: String, second: Int = nextSecond++) =
        ServerSentEvent.builder(
            json(
                firstName = firstName,
                userId = if (firstName == "Sytac") 999 else nextUserId++,
                eventDate = "27-02-2023 03:20:%02d.000".format(second),
            )
        ).event("show-liked").build()

    private fun json(
        showId: String = "s41",
        cast: String? = "Kuby Farner, Rakota Cotus",
        releaseYear: Int = 2021,
        eventDate: String = "27-02-2023 03:20:17.111",
        userId: Long = 116,
        firstName: String = "Guenna",
        country: String = "TH",
    ) = """
        {
          "show": {
            "show_id": "$showId",
            ${if (cast != null) "\"cast\": \"$cast\"," else ""}
            "title": "Kancy Kancy (Dhorts)",
            "release_year": $releaseYear,
            "platform": "Sysney"
          },
          "event_date": "$eventDate",
          "user": {
            "id": $userId,
            "date_of_birth": "31/10/1970",
            "first_name": "$firstName",
            "last_name": "Beeton",
            "country": "$country"
          }
        }
    """.trimIndent()
}
