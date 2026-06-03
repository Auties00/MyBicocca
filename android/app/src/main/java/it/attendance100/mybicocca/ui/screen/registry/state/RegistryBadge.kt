package it.attendance100.mybicocca.ui.screen.registry.state

// Status pill shown at the end of a service row. The tone maps to the registry status
// palette (see ui/screen/registry/theme/RegistryStatusColors.kt); the label is the
// Italian copy rendered inside the pill (e.g. "In ritardo", "3 nuovi").
enum class RegistryBadgeTone { Ok, Attention, Alert, Neutral }

data class RegistryBadge(
    val label: String,
    val tone: RegistryBadgeTone,
)
