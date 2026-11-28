package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.model.library.LibraryWeekdayCrowding
import it.attendance100.mybicocca.domain.repository.LibraryCrowdingHistoryRepository
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import javax.inject.Inject

/** Streams cached crowding history by weekday for one library. */
class ObserveLibraryCrowdingHistoryUseCase @Inject constructor(
    private val historyRepository: LibraryCrowdingHistoryRepository,
) {
    operator fun invoke(libraryId: String): Flow<Map<DayOfWeek, LibraryWeekdayCrowding>> =
        historyRepository.observeHistory(libraryId)
}
