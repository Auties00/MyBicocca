package it.attendance100.mybicocca.ui.screen.registry.state

enum class RegistryCategory {
    Exams,
    Taxes,
    Teaching,
    Documents,
    Agenda,
}

val RegistryCategory.label: String
    get() = when (this) {
        RegistryCategory.Exams -> "Esami"
        RegistryCategory.Taxes -> "Tasse"
        RegistryCategory.Teaching -> "Didattica"
        RegistryCategory.Documents -> "Documenti"
        RegistryCategory.Agenda -> "Agenda"
    }
