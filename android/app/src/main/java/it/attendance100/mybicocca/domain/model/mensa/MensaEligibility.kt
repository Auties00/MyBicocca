package it.attendance100.mybicocca.domain.model.mensa

import java.time.LocalDate

/** Combination of courses (or lunch-box items) the canteen accepts for a fixed price. */
enum class MensaMealRuleId {
    FULL_MENU,
    REDUCED_MENU,
    SALAD_MENU,
    SINGLE_DISH_MENU,
    LUNCH_BOX_SANDWICH,
    LUNCH_BOX_PIZZA,
}

/** A meal combination and its price, in euro cents. The UI maps [id] to localized wording. */
data class MensaMealRule(
    val id: MensaMealRuleId,
    val priceCents: Int,
)

/** Meal combinations and prices valid for the university canteens, as of [lastVerified]. */
data class MensaEligibility(
    val lastVerified: LocalDate,
    val rules: List<MensaMealRule>,
)
