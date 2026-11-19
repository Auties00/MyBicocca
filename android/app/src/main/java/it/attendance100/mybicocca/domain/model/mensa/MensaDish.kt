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
)
