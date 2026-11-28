package it.attendance100.mybicocca.domain.model.mensa

/** A dish entry shown in a daily canteen menu or reservation dish list. */
data class MensaDish(
    val id: Int,
    val name: String,
    val shortName: String,
    val courseId: Int?,
    val courseName: String?,
    val mealTypeId: Int?,
    val mealTypeName: String?,
    val imageUrl: String?,
    val thumbUrl: String?,
    val majorAllergens: List<MensaAllergen>,
    val traceAllergens: List<MensaAllergen>,
    val caloricLabel: String?,
    val caloricColorHex: String?,
    val dietaryBadges: List<MensaDietaryBadge>,
)

/** Dietary or certification tag attached to a dish (e.g. *Biologico*, *Vegano*, *DOP*). */
data class MensaDietaryBadge(
    val key: String,
    val label: String,
    val iconUrl: String?,
)

/** EU / Compass allergen present directly or in traces. */
data class MensaAllergen(
    val id: Int,
    val name: String,
    val shortName: String,
    val iconUrl: String?,
    val kind: MensaAllergenKind = MensaAllergenKind.ALLERGEN,
) {
    /**
     * "Contains frozen ingredients" is declared in the allergen list but is a notice about the food,
     * not something to avoid, so it is never flagged and never offered as a preference.
     */
    val isFrozenNotice: Boolean
        get() = kind == MensaAllergenKind.CONTENT &&
            (name.contains("surgel", ignoreCase = true) || name.contains("congel", ignoreCase = true))
}

/** Compass files allergens (`allergene`) and other declared contents such as pork (`contenuto`) in one list. */
enum class MensaAllergenKind { ALLERGEN, CONTENT }
