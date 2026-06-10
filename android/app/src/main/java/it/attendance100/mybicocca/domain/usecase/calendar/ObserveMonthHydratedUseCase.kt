package it.attendance100.mybicocca.domain.usecase.calendar

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth
import javax.inject.Inject

/**
 * Streams whether a month has ever completed a first successful sync. The calendar tab keeps
 * its initial-loading indicator pinned until the visible month hydrates, so a never-synced
 * month shows a loading state instead of an empty grid.
 */
class ObserveMonthHydratedUseCase @Inject constructor(
    private val repository: CalendarRepository,
) {
    operator fun invoke(careerId: CareerId, yearMonth: YearMonth): Flow<Boolean> =
        repository.observeMonthHydrated(careerId, yearMonth)
}
