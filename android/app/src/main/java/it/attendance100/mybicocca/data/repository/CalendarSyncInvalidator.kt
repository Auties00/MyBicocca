package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.domain.model.calendar.EventSource
import it.attendance100.mybicocca.domain.model.career.CareerId
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bus that lets feature repositories nudge the calendar when they change data it mirrors
 * (e.g. an exam booked or cancelled from the Appelli sheet), without a circular dependency
 * on the calendar repository. The calendar listens and re-syncs the affected source right
 * away instead of waiting out the TTL.
 */
@Singleton
class CalendarSyncInvalidator @Inject constructor() {

    /** One nudge: which career's data changed and which calendar source mirrors it. */
    data class Invalidation(val careerId: CareerId, val source: EventSource)

    private val _invalidations = MutableSharedFlow<Invalidation>(extraBufferCapacity = 16)

    /** Stream the calendar collects for its lifetime; emissions are not replayed. */
    val invalidations: SharedFlow<Invalidation> = _invalidations

    /** Best-effort, non-suspending publish, safe to call from any context. */
    fun invalidate(careerId: CareerId, source: EventSource) {
        _invalidations.tryEmit(Invalidation(careerId, source))
    }
}
