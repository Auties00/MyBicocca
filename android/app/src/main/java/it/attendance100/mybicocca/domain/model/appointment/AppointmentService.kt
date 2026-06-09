package it.attendance100.mybicocca.domain.model.appointment

// A bookable information-desk service ("sportello") of the university booking portal.
data class AppointmentService(
    val id: Int,
    val name: String,
    val group: String?,
    val descriptionHtml: String?,
    val durationSeconds: Int,
)
