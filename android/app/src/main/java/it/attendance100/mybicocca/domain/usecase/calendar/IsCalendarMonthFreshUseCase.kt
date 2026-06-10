package it.attendance100.mybicocca.domain.usecase.calendar

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.repository.CalendarRepository
import java.time.YearMonth
import javax.inject.Inject

/**
 * Tells whether every calendar source covering a month is still within its TTL. The calendar
 * tab checks it before refreshing so switching to an already-fresh month does not flash the
 * sync indicator.
 */
class IsCalendarMonthFreshUseCase @Inject constructor(
    private val repository: CalendarRepository,
) {
    suspend operator fun invoke(careerId: CareerId, yearMonth: YearMonth): Boolean =
        repository.isMonthFresh(careerId, yearMonth)
}
