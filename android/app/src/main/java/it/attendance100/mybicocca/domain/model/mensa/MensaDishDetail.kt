package it.attendance100.mybicocca.domain.model.mensa

/** Detailed recipe, ingredients, allergens, and nutritional breakdown for a single dish. */
data class MensaDishDetail(
    val id: Int,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val netQuantityGrams: Double?,
    val ingredients: List<MensaIngredient>,
    val majorAllergens: List<MensaAllergen>,
    val traceAllergens: List<MensaAllergen>,
    val displayNutrition: Boolean,
    val nutritionSource: String?,
    val nutritionRows: List<MensaNutritionRow>,
    val gdaItems: List<MensaGdaItem>,
    val dietaryBadges: List<MensaDietaryBadge>,
)

/** Single recipe ingredient and its quantity. */
data class MensaIngredient(
    val id: Int,
    val name: String,
    val quantity: String?,
)

/** Nutrient measurement per portion and per 100 g. */
data class MensaNutritionRow(
    val name: String,
    val unit: String,
    val portionValue: Double?,
    val portionKjValue: Double?,
    val per100gValue: Double?,
    val per100gKjValue: Double?,
    val field: String?,
)

/** Reference Intake (GDA) percentage entry for a nutrient. */
data class MensaGdaItem(
    val name: String,
    val formattedValue: String,
    val percentage: Double,
    val field: String?,
)
