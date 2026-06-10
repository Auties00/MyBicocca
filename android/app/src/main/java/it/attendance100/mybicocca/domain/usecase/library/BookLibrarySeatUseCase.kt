package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.model.library.Library
import it.attendance100.mybicocca.domain.model.library.LibrarySeat
import it.attendance100.mybicocca.domain.model.library.LibraryZone
import it.attendance100.mybicocca.domain.repository.LibraryRepository
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

/**
 * Submits the Biblioteca booking wizard: books the chosen seat slot for the logged-in account
 * and returns the new reservation's identifier, when the server issues one. The booking is
 * confirmed immediately — no e-mail step — and the cached reservation list re-syncs.
 */
class BookLibrarySeatUseCase @Inject constructor(
    private val repository: LibraryRepository,
) {
    suspend operator fun invoke(
        library: Library,
        zone: LibraryZone,
        seat: LibrarySeat,
        date: LocalDate,
        startTime: LocalTime,
        durationMinutes: Int,
        email: String,
        note: String?,
    ): String? =
        repository.bookSeat(library, zone, seat, date, startTime, durationMinutes, email, note)
}
