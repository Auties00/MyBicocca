package it.attendance100.mybicocca.data.local.appointment

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// A desk reservation created from this device. The Portale Planning portal is anonymous
// (keyed by code + email), so this table is the only way to list bookings later. Not scoped
// per account: the booking email is free-form and independent of the signed-in user.
@Entity(tableName = "appointment_reservation")
data class AppointmentReservationEntity(
    @PrimaryKey val code: String,
    val email: String,
    @ColumnInfo("entry_id") val entryId: Int?,
    @ColumnInfo("service_id") val serviceId: Int,
    @ColumnInfo("service_name") val serviceName: String,
    @ColumnInfo("service_group") val serviceGroup: String?,
    @ColumnInfo("area_name") val areaName: String?,
    @ColumnInfo("area_address") val areaAddress: String?,
    @ColumnInfo("start_epoch_seconds") val startEpochSeconds: Long,
    @ColumnInfo("end_epoch_seconds") val endEpochSeconds: Long,
    @ColumnInfo("qr_code_data_url") val qrCodeDataUrl: String?,
    @ColumnInfo("web_conference_url") val webConferenceUrl: String?,
    @ColumnInfo("created_at") val createdAt: Long,
)
