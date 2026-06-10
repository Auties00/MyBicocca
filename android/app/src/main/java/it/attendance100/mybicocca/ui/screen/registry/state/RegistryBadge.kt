package it.attendance100.mybicocca.ui.screen.registry.state

/** Semantic tone of a status pill, mapped to the registry status palette in the registry theme. */
enum class RegistryBadgeTone { Ok, Attention, Alert, Info, Neutral }

/**
 * Status pill shown at the trailing end of a service row: the Italian copy rendered inside
 * the pill (e.g. "In ritardo", "3 nuovi") and the palette tone it is tinted with.
 */
data class RegistryBadge(
    val label: String,
    val tone: RegistryBadgeTone,
)
