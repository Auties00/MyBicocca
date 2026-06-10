package it.attendance100.mybicocca.domain.usecase.calendar

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.repository.CalendarRepository
import java.time.YearMonth
import javax.inject.Inject

/**
 * Syncs every calendar source covering a month and persists the results. The calendar tab
 * runs it TTL-gated when the user lands on a month, and with [invoke]'s force flag from
 * pull-to-refresh; it throws when a required source fails so the screen can surface the
 * failure as a sync status.
 */
class RefreshCalendarMonthUseCase @Inject constructor(
    private val repository: CalendarRepository,
) {
    suspend operator fun invoke(
        careerId: CareerId,
        yearMonth: YearMonth,
        force: Boolean = false,
    ) = repository.refreshMonth(careerId, yearMonth, force)
}
