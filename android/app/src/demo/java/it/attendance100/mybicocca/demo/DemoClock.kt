package it.attendance100.mybicocca.demo

import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/** The moment the demo build is frozen at, so every screenshot shows the same day. */
object DemoClock {
    val zone: ZoneId = ZoneId.of("Europe/Rome")
    val now: LocalDateTime = LocalDateTime.of(2026, 6, 18, 10, 30)
    val fixed: Clock = Clock.fixed(now.atZone(zone).toInstant(), zone)

    fun instant(at: LocalDateTime): Instant = at.atZone(zone).toInstant()
}
