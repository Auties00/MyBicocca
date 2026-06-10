package it.attendance100.mybicocca.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.domain.model.calendar.EventSource
import it.attendance100.mybicocca.domain.model.career.CareerId
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Covers the cross-feature calendar nudge bus: a published invalidation reaches a live
 * collector carrying the originating career and the mirrored source, the stream replays
 * nothing to a late subscriber (the calendar collects for its lifetime, not from history),
 * and concurrent nudges all surface in order.
 */
class CalendarSyncInvalidatorTest {

    private val invalidator = CalendarSyncInvalidator()

    @Test
    fun `invalidate emits the career and source to a live collector`() = runTest {
        invalidator.invalidations.test {
            invalidator.invalidate(CareerId(7L), EventSource.EXAM)

            val event = awaitItem()
            assertThat(event.careerId).isEqualTo(CareerId(7L))
            assertThat(event.source).isEqualTo(EventSource.EXAM)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invalidations are not replayed to a collector that subscribes afterwards`() = runTest {
        invalidator.invalidate(CareerId(7L), EventSource.LESSON)

        invalidator.invalidations.test {
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `multiple invalidations surface in emission order`() = runTest {
        invalidator.invalidations.test {
            invalidator.invalidate(CareerId(1L), EventSource.EXAM)
            invalidator.invalidate(CareerId(2L), EventSource.APPOINTMENT)
            invalidator.invalidate(CareerId(3L), EventSource.LIBRARY)

            assertThat(awaitItem()).isEqualTo(
                CalendarSyncInvalidator.Invalidation(CareerId(1L), EventSource.EXAM),
            )
            assertThat(awaitItem()).isEqualTo(
                CalendarSyncInvalidator.Invalidation(CareerId(2L), EventSource.APPOINTMENT),
            )
            assertThat(awaitItem()).isEqualTo(
                CalendarSyncInvalidator.Invalidation(CareerId(3L), EventSource.LIBRARY),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }
}
