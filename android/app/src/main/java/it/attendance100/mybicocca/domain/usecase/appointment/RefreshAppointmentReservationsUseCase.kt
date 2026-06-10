package it.attendance100.mybicocca.domain.usecase.appointment

import it.attendance100.mybicocca.domain.repository.AppointmentRepository
import javax.inject.Inject

/**
 * Re-validates the locally-saved reservations against Portale Planning when the
 * "Appuntamenti" bookings list opens or is pulled to refresh. Deletes from the local store the
 * bookings that are gone on the portal and the long-past ones.
 */
class RefreshAppointmentReservationsUseCase @Inject constructor(
    private val repository: AppointmentRepository,
) {
    suspend operator fun invoke() =
        repository.refreshReservations()
}
