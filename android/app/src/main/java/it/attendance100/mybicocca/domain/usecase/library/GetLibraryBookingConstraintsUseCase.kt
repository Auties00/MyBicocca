package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.model.library.LibraryBookingConstraints
import it.attendance100.mybicocca.domain.repository.LibraryRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Loads what the chosen zone accepts for booking around a reference day — open days, start
 * times, durations — to drive the date/duration/time pickers of the Biblioteca booking wizard.
 */
class GetLibraryBookingConstraintsUseCase @Inject constructor(
    private val repository: LibraryRepository,
) {
    suspend operator fun invoke(
        libraryId: String,
        zoneId: Int,
        date: LocalDate,
    ): LibraryBookingConstraints = repository.getBookingConstraints(libraryId, zoneId, date)
}
