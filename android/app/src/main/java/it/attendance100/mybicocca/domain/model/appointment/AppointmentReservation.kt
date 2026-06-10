package it.attendance100.mybicocca.domain.model.appointment

import java.time.LocalDateTime

/**
 * A Portale Planning desk reservation created from this device. Together, [code] + [email]
 * authorize management — the portal is anonymous and has no authenticated "my reservations"
 * listing. Shown on the registry "Appuntamenti" sub-screen.
 *
 * @property code Reservation code issued by the portal; pairs with [email] to manage or cancel.
 * @property email Email address submitted as the form's primary field.
 * @property entryId Portal entry id, needed to download the reservation PDF.
 * @property serviceId Booked service identifier.
 * @property serviceName Booked service name.
 * @property serviceGroup Portal group the booked service belongs to ("Carriere Studenti …",
 *   "Didattica …"). Kept so the booking can show its macro-section without re-fetching the
 *   service catalog.
 * @property areaName Location name of the appointment.
 * @property areaAddress Street address of the appointment.
 * @property start Appointment start, local to the campus.
 * @property end Appointment end, local to the campus.
 * @property qrCodeDataUrl Check-in QR as a base64 data URL, when the portal issued one.
 * @property webConferenceUrl Meeting link for virtual appointments, when any.
 */
data class AppointmentReservation(
    val code: String,
    val email: String,
    val entryId: Int?,
    val serviceId: Int,
    val serviceName: String,
    val serviceGroup: String?,
    val areaName: String?,
    val areaAddress: String?,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val qrCodeDataUrl: String?,
    val webConferenceUrl: String?,
)
