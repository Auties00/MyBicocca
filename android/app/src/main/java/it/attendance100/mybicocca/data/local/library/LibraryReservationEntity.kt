package it.attendance100.mybicocca.data.local.library

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Offline cache row for one entry of the Affluences "my reservations" list. The server is the
 * source of truth — the table exists only so bookings still show offline. Keyed by the server
 * reservation id and not account-scoped: it mirrors the single linked Affluences e-mail session
 * and is wiped on library logout.
 *
 * @property startEpochSeconds Reservation start, as epoch seconds of the UTC instant.
 * @property endEpochSeconds Reservation end, as epoch seconds of the UTC instant.
 * @property reservationCode Short human-readable code shown to the user; null when absent.
 * @property cancellationToken Token authorizing cancellation calls; null when the server omits it.
 * @property state Name of the domain reservation-state enum entry.
 */
@Entity(tableName = "library_reservation")
data class LibraryReservationEntity(
    @PrimaryKey @ColumnInfo("reservation_id") val reservationId: Int,
    @ColumnInfo("library_name") val libraryName: String,
    @ColumnInfo("library_secondary_name") val librarySecondaryName: String?,
    @ColumnInfo("seat_name") val seatName: String,
    @ColumnInfo("start_epoch_seconds") val startEpochSeconds: Long,
    @ColumnInfo("end_epoch_seconds") val endEpochSeconds: Long,
    val note: String?,
    @ColumnInfo("reservation_code") val reservationCode: String?,
    @ColumnInfo("cancellation_token") val cancellationToken: String?,
    val state: String,
)
