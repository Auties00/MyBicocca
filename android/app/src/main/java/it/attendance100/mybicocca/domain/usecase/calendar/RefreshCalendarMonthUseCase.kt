package it.attendance100.mybicocca.domain.usecase.calendar

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.repository.CalendarRepository
import java.time.YearMonth
import javax.inject.Inject

class RefreshCalendarMonthUseCase @Inject constructor(
    private val repository: CalendarRepository,
) {
    suspend operator fun invoke(
        careerId: CareerId,
        yearMonth: YearMonth,
        force: Boolean = false,
    ) = repository.refreshMonth(careerId, yearMonth, force)
}
