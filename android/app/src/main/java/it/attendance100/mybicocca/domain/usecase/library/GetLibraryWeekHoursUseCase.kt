package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.model.library.LibraryWeekHours
import it.attendance100.mybicocca.domain.repository.LibraryRepository
import javax.inject.Inject

/**
 * Fetches a library's opening hours for the week at the given offset from the current one;
 * backs the timetable section of the Biblioteca library detail.
 */
class GetLibraryWeekHoursUseCase @Inject constructor(
    private val repository: LibraryRepository,
) {
    suspend operator fun invoke(libraryId: String, weekOffset: Int = 0): LibraryWeekHours =
        repository.getWeekHours(libraryId, weekOffset)
}
