package it.attendance100.mybicocca.domain.model.mensa

/**
 * The user's allergen selection, kept on the device only. A dish whose declared allergens
 * overlap [allergenIds] is flagged in the menus, and hidden when [hideMatchingDishes] is on.
 */
data class MensaAllergenPreferences(
    val allergenIds: Set<Int> = emptySet(),
    val hideMatchingDishes: Boolean = false,
) {
    val isActive: Boolean get() = allergenIds.isNotEmpty()
}

/** How a dish relates to the allergens the user selected. */
enum class MensaAllergenMatch { NONE, TRACE, CONTAINS }

/** Compares the dish's major (contained) and trace allergens with the user's selection. */
fun MensaDish.allergenMatch(preferences: MensaAllergenPreferences): MensaAllergenMatch = when {
    !preferences.isActive -> MensaAllergenMatch.NONE
    majorAllergens.any { it.id in preferences.allergenIds } -> MensaAllergenMatch.CONTAINS
    traceAllergens.any { it.id in preferences.allergenIds } -> MensaAllergenMatch.TRACE
    else -> MensaAllergenMatch.NONE
}
