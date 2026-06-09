package it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.state

import it.attendance100.mybicocca.domain.model.appointment.AppointmentService

// A display group of desk services. The portal's raw groups are too granular for a
// directory (one group per faculty area), so the UI folds them into macro sections.
data class AppointmentDirectorySection(
    val name: String,
    val caption: String,
    val services: List<AppointmentService>,
)
