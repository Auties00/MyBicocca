package it.attendance100.mybicocca.domain.usecase.calendar

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.repository.CalendarRepository
import java.time.YearMonth
import javax.inject.Inject

/**
 * Warms the previous and next months in the background once the calendar tab settles on a
 * month, so month swipes land on already-cached data. Fire-and-forget: failures are silent.
 */
class PrefetchAdjacentMonthsUseCase @Inject constructor(
    private val repository: CalendarRepository,
) {
    suspend operator fun invoke(careerId: CareerId, yearMonth: YearMonth) =
        repository.prefetchAdjacent(careerId, yearMonth)
}
