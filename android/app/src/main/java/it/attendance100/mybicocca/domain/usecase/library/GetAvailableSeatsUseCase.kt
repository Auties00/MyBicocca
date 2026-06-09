package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.model.library.LibrarySeat
import it.attendance100.mybicocca.domain.repository.LibraryRepository
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class GetAvailableSeatsUseCase @Inject constructor(
    private val repository: LibraryRepository,
) {
    suspend operator fun invoke(
        libraryId: String,
        zoneId: Int,
        date: LocalDate,
        durationMinutes: Int,
        startTime: LocalTime?,
    ): List<LibrarySeat> =
        repository.getAvailableSeats(libraryId, zoneId, date, durationMinutes, startTime)
}
