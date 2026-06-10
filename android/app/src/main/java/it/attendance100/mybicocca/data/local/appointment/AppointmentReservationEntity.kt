package it.attendance100.mybicocca.data.local.appointment

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A desk reservation created from this device. The Portale Planning portal is anonymous (a
 * booking is keyed by code + email), so this table is the only way to list bookings later.
 * Deliberately NOT scoped per account: the booking email is free-form and independent of the
 * signed-in user, so rows are device-local and survive account switches.
 *
 * @property code Primary key — the reservation code issued by the portal.
 * @property email Email submitted as the booking's primary field; pairs with [code] to manage
 *   or cancel the reservation.
 * @property entryId Portal entry id, needed for the reservation PDF.
 * @property startEpochSeconds Appointment start as epoch seconds (campus-local wall time).
 * @property endEpochSeconds Appointment end as epoch seconds.
 * @property qrCodeDataUrl Check-in QR as a base64 data URL, when the portal issued one.
 * @property webConferenceUrl Meeting link for virtual appointments, when any.
 * @property createdAt Insertion timestamp in epoch milliseconds.
 */
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
