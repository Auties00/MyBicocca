package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.model.library.LibraryWeekHours
import it.attendance100.mybicocca.domain.repository.LibraryRepository
import javax.inject.Inject

class GetLibraryWeekHoursUseCase @Inject constructor(
    private val repository: LibraryRepository,
) {
    suspend operator fun invoke(libraryId: String, weekOffset: Int = 0): LibraryWeekHours =
        repository.getWeekHours(libraryId, weekOffset)
}
