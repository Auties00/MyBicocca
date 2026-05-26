package it.attendance100.mybicocca.ui.screen.registry.state

enum class RegistryTab {
    Exams,
    Taxes,
    Questionnaires,
    Bookings,
    Internships,
}

val RegistryTab.label: String
    get() = when (this) {
        RegistryTab.Exams -> "Esami"
        RegistryTab.Taxes -> "Tasse"
        RegistryTab.Questionnaires -> "Questionari"
        RegistryTab.Bookings -> "Prenotazioni"
        RegistryTab.Internships -> "Stage"
    }
