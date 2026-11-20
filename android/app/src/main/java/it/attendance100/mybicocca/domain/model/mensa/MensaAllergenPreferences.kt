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

    /**
     * Whether the user avoids gluten: any of the cereal allergens is selected. The names are read
     * from [allergens] when it is loaded, and the ids Compass has published so far cover the rest.
     */
    fun avoidsGluten(allergens: List<MensaAllergen> = emptyList()): Boolean {
        val glutenIds = allergens.filter { "glutine" in it.name.lowercase() }.map { it.id }.toSet() + KNOWN_GLUTEN_IDS
        return allergenIds.any { it in glutenIds }
    }

    private companion object {
        /** Grano, farro, khorasan, segale, orzo and avena. */
        val KNOWN_GLUTEN_IDS = setOf(37, 46, 47, 48, 49, 50)
    }
}

/** How a dish relates to the allergens the user selected. */
enum class MensaAllergenMatch { NONE, TRACE, CONTAINS }

/** Compares the dish's major (contained) and trace allergens with the user's selection. */
fun MensaDish.allergenMatch(preferences: MensaAllergenPreferences): MensaAllergenMatch = when {
    !preferences.isActive -> MensaAllergenMatch.NONE
    majorAllergens.any { it.id in preferences.allergenIds && !it.isFrozenNotice } -> MensaAllergenMatch.CONTAINS
    traceAllergens.any { it.id in preferences.allergenIds && !it.isFrozenNotice } -> MensaAllergenMatch.TRACE
    else -> MensaAllergenMatch.NONE
}
