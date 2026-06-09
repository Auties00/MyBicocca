package it.attendance100.mybicocca.data.local.library

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// Offline cache of the server's "my reservations" list (the server is the source of truth; this is
// kept only so bookings still show when offline). Keyed by the server reservation id.
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
