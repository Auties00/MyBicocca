package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.domain.model.career.CareerId
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth

/**
 * Unified calendar over five sources: EasyStaff lessons, Esse3 booked exams, Moodle
 * assignment deadlines, planning-portal appointments and Affluences library seat
 * reservations.
 *
 * The `observe*` methods are hot, Room-backed flows — they emit on local writes, never touch
 * the network, and merge all sources into chronologically sorted lists. Lessons and exams are
 * read from the calendar's own store; the remaining sources are merged in at observe time
 * from the stores their owning features maintain. The `refresh*` methods hit the network,
 * write through to Room and throw on failure, leaving callers to translate the exception
 * into a sync status.
 */
interface CalendarRepository {
    /** Events of [yearMonth], merged across all sources and sorted by date and start time. */
    fun observeMonth(careerId: CareerId, yearMonth: YearMonth): Flow<Loadable<List<CalendarEvent>>>

    /** Events of a single [date], merged across all sources and sorted by start time. */
    fun observeDay(careerId: CareerId, date: LocalDate): Flow<Loadable<List<CalendarEvent>>>

    /** The next [limit] events on or after [from], merged across all sources. */
    fun observeUpcoming(careerId: CareerId, from: LocalDate, limit: Int): Flow<Loadable<List<CalendarEvent>>>

    /**
     * True once the month has completed at least one successful sync, regardless of how
     * stale it is now. Drives the first-load indicator: an unhydrated month shows a
     * loading state instead of an empty grid.
     */
    fun observeMonthHydrated(careerId: CareerId, yearMonth: YearMonth): Flow<Boolean>

    /**
     * True when every network source covering the month is within its TTL — refreshing
     * would be a no-op, so callers can skip the sync indicator entirely.
     */
    suspend fun isMonthFresh(careerId: CareerId, yearMonth: YearMonth): Boolean

    /**
     * Syncs every source covering [yearMonth] and persists the results. Month-scoped sources
     * fetch just that month, while career-global ones (exam bookings, appointments, library
     * seats) refresh their whole data set; per-source TTLs make the call cheap when data is
     * fresh unless [force] bypasses them. Throws when a required source — lessons, exams or
     * deadlines — fails; appointment and library failures are swallowed as best-effort.
     */
    suspend fun refreshMonth(careerId: CareerId, yearMonth: YearMonth, force: Boolean = false)

    /**
     * Fire-and-forget refresh of the months adjacent to [yearMonth], so month swipes land on
     * warm data. Failures are silent.
     */
    suspend fun prefetchAdjacent(careerId: CareerId, yearMonth: YearMonth)
}
