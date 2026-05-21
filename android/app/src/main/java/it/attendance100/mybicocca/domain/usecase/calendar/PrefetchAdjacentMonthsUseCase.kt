package it.attendance100.mybicocca.domain.usecase.calendar

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.repository.CalendarRepository
import java.time.YearMonth
import javax.inject.Inject

class PrefetchAdjacentMonthsUseCase @Inject constructor(
    private val repository: CalendarRepository,
) {
    suspend operator fun invoke(careerId: CareerId, yearMonth: YearMonth) =
        repository.prefetchAdjacent(careerId, yearMonth)
}
