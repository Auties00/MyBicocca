package it.attendance100.mybicocca.ui.screen.registry.state

enum class RegistryCategory {
    All,
    Exams,
    Teaching,
    Administration,
    Agenda,
    Internships,
}

val RegistryCategory.label: String
    get() = when (this) {
        RegistryCategory.All -> "Tutti"
        RegistryCategory.Exams -> "Esami"
        RegistryCategory.Teaching -> "Didattica"
        RegistryCategory.Administration -> "Amministrazione"
        RegistryCategory.Agenda -> "Agenda"
        RegistryCategory.Internships -> "Stage"
    }
