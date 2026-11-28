package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.core.time.AppClock
import it.attendance100.mybicocca.domain.model.library.LibraryBookingDraft
import it.attendance100.mybicocca.domain.model.library.LibraryPendingValidation
import it.attendance100.mybicocca.domain.repository.LibraryRepository
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Single enforcement point for persisted pending-validation and booking-draft validity.
 * Both expire after 60 minutes, and a draft whose slot is no longer in the future is invalid.
 * Invalid values are cleared from storage before returning null.
 */
class GetLibraryPendingStateUseCase @Inject constructor(
    private val repository: LibraryRepository,
) {
    suspend fun pendingValidation(): LibraryPendingValidation? {
        val pending = repository.pendingValidation() ?: return null
        val now = Instant.now(AppClock.clock)
        if (!now.isBefore(pending.createdAt.plus(TTL))) {
            repository.clearPendingValidation()
            return null
        }
        return pending
    }

    suspend fun bookingDraft(): LibraryBookingDraft? {
        val draft = repository.bookingDraft() ?: return null
        val now = Instant.now(AppClock.clock)
        val nowLocal = LocalDateTime.now(AppClock.clock)
        val slotStart = LocalDateTime.of(draft.date, draft.startTime)
        if (!now.isBefore(draft.savedAt.plus(TTL)) || !slotStart.isAfter(nowLocal)) {
            repository.clearBookingDraft()
            return null
        }
        return draft
    }

    private companion object {
        val TTL: Duration = Duration.ofMinutes(60)
    }
}
