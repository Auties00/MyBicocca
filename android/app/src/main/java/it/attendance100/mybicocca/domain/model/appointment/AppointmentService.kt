package it.attendance100.mybicocca.domain.model.appointment

/**
 * A bookable information-desk service ("sportello") of the university's Portale Planning
 * booking portal. Listed as the entry point of the registry "Appuntamenti" booking flow.
 *
 * @property id Portal service identifier.
 * @property name Display name of the desk.
 * @property group Macro-section the service belongs to (e.g. "Carriere Studenti …").
 * @property descriptionHtml Rich-text description supplied by the portal, when any.
 * @property durationSeconds Default appointment duration.
 */
data class AppointmentService(
    val id: Int,
    val name: String,
    val group: String?,
    val descriptionHtml: String?,
    val durationSeconds: Int,
)
