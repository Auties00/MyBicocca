package it.attendance100.mybicocca.domain.model.appointment

import java.time.LocalDateTime

// A reservation created from this device. Together, code + email authorize management
// (the portal has no authenticated "my reservations" listing).
data class AppointmentReservation(
    val code: String,
    val email: String,
    val entryId: Int?,
    val serviceId: Int,
    val serviceName: String,
    // The portal group the booked service belongs to ("Carriere Studenti …", "Didattica …").
    // Kept so the booking can show its macro-section without re-fetching the service catalog.
    val serviceGroup: String?,
    val areaName: String?,
    val areaAddress: String?,
    val start: LocalDateTime,
    val end: LocalDateTime,
    // Check-in QR as a base64 data URL, when the portal issued one.
    val qrCodeDataUrl: String?,
    val webConferenceUrl: String?,
)
