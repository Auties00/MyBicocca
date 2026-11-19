package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergen
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergenPreferences
import it.attendance100.mybicocca.domain.model.mensa.MensaCourse
import it.attendance100.mybicocca.domain.model.mensa.MensaDayMealType
import it.attendance100.mybicocca.domain.model.mensa.MensaDaySchedule
import it.attendance100.mybicocca.domain.model.mensa.MensaDietaryBadge
import it.attendance100.mybicocca.domain.model.mensa.MensaDish
import it.attendance100.mybicocca.domain.model.mensa.MensaDishDetail
import it.attendance100.mybicocca.domain.model.mensa.MensaEligibility
import it.attendance100.mybicocca.domain.model.mensa.MensaGdaItem
import it.attendance100.mybicocca.domain.model.mensa.MensaIngredient
import it.attendance100.mybicocca.domain.model.mensa.MensaMealRule
import it.attendance100.mybicocca.domain.model.mensa.MensaMealRuleId
import it.attendance100.mybicocca.domain.model.mensa.MensaMealType
import it.attendance100.mybicocca.domain.model.mensa.MensaMenuSummary
import it.attendance100.mybicocca.domain.model.mensa.MensaNutritionRow
import it.attendance100.mybicocca.domain.model.mensa.MensaSession
import it.attendance100.mybicocca.domain.model.settings.AppTheme
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import java.time.LocalDate

/** Width and height of a full-page preview, close to a phone's modal sheet. */
internal const val MENSA_PREVIEW_WIDTH = 400
internal const val MENSA_PREVIEW_HEIGHT = 780

/** One palette in one light or dark mode, for previews that show a component in every look. */
internal data class MensaPreviewTheme(val appTheme: AppTheme, val dark: Boolean) {
    val label: String get() = "${appTheme.displayName} · ${if (dark) "Dark" else "Light"}"
}

/** Feeds the three fixed palettes, each light and dark, into a `@PreviewParameter` preview. */
internal class MensaPreviewThemeProvider : PreviewParameterProvider<MensaPreviewTheme> {
    private val themes = listOf(AppTheme.Default, AppTheme.Oceano, AppTheme.Bosco)
        .flatMap { listOf(MensaPreviewTheme(it, dark = false), MensaPreviewTheme(it, dark = true)) }

    override val values: Sequence<MensaPreviewTheme> = themes.asSequence()

    override fun getDisplayName(index: Int): String = themes[index].label
}

/** The theme plus the surface a modal sheet draws its pages on. */
@Composable
internal fun MensaPreviewHost(
    theme: MensaPreviewTheme,
    content: @Composable () -> Unit,
) {
    BicoccaTheme(dark = theme.dark, appTheme = theme.appTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            content = content,
        )
    }
}

internal object MensaPreviewData {
    val today: LocalDate = LocalDate.of(2026, 6, 18)

    val gluten = MensaAllergen(1, "Glutine", "Glutine", null)
    val glutenSelected = MensaAllergenPreferences(allergenIds = setOf(1))
    val milk = MensaAllergen(7, "Latte", "Latte", null)
    val allergens = listOf("Glutine", "Crostacei", "Uova", "Pesce", "Arachidi", "Soia", "Latte", "Frutta a guscio")
        .mapIndexed { index, name -> MensaAllergen(index + 1, name, name, null) }

    private fun dish(
        id: Int,
        name: String,
        courseId: Int,
        courseName: String,
        calories: String = "Medio",
        contains: List<MensaAllergen> = emptyList(),
        traces: List<MensaAllergen> = emptyList(),
        badges: List<MensaDietaryBadge> = emptyList(),
    ) = MensaDish(
        id = id, name = name, shortName = name, courseId = courseId, courseName = courseName,
        mealTypeId = 3, mealTypeName = "Pranzo", imageUrl = null, thumbUrl = null,
        majorAllergens = contains, traceAllergens = traces,
        caloricLabel = calories,
        caloricColorHex = if (calories == "Leggero") "#4CAF50" else "#FF9800",
        dietaryBadges = badges,
    )

    private val vegan = MensaDietaryBadge("vegano", "Vegano", null)

    val plainDish = dish(1, "CREMA DI CAROTE E ORIGANO", 1, "Primi Piatti", "Leggero", badges = listOf(vegan))
    val containsDish = dish(2, "PASTA AL POMODORO", 1, "Primi Piatti", "Leggero", contains = listOf(gluten))
    val traceDish = dish(3, "RISOTTO ALLA MONZESE", 1, "Primi Piatti", traces = listOf(milk))

    val dishes = listOf(
        plainDish,
        containsDish,
        traceDish,
        dish(4, "COSCE DI POLLO ALLA PAPRIKA", 2, "Secondi Piatti"),
        dish(5, "FILETTO DI MERLUZZO DORATO", 2, "Secondi Piatti", contains = listOf(gluten)),
        dish(6, "PATATE AL ROSMARINO", 4, "Contorni", badges = listOf(vegan)),
        dish(7, "FRUTTA FRESCA 150G", 6, "Dessert", "Leggero", badges = listOf(vegan)),
        dish(8, "ACQUA NATURALE 0,5 L", 9, "Bevande", "Leggero"),
    )

    val menus = listOf(
        MensaMenuSummary(
            id = 400, name = "Menù Bicocca", shortName = "Menù Bicocca", description = "Mense U6 e U12",
            cateringTypeDescription = "Mensa", activeDays = listOf(today, today.plusDays(1), today.plusDays(4)),
            mealTypes = listOf(MensaMealType(3, "Pranzo", "Pr", 1), MensaMealType(5, "Cena", "Ce", 2)),
        ),
        MensaMenuSummary(
            id = 701, name = "Ristorazione complementare U3", shortName = "U3", description = "Edificio U3",
            cateringTypeDescription = "Bar", activeDays = listOf(today, today.plusDays(1)),
            mealTypes = listOf(MensaMealType(3, "Pranzo", "Pr", 1)),
        ),
    )

    val schedule: Loadable<Map<LocalDate, MensaDaySchedule>> = Loadable.Loaded(
        listOf(today, today.plusDays(1), today.plusDays(4)).associateWith { date ->
            MensaDaySchedule(
                menuId = 400, date = date, name = "Menù Bicocca", active = true,
                mealTypes = listOf(
                    MensaDayMealType(
                        id = 3, name = "Pranzo", order = 1,
                        courses = dishes.groupBy { it.courseId }.map { (id, list) ->
                            MensaCourse(id ?: 0, list.first().courseName.orEmpty(), "", id ?: 0, list)
                        },
                    ),
                    MensaDayMealType(id = 5, name = "Cena", order = 2, courses = emptyList()),
                ),
            )
        },
    )

    val session = MensaSession(
        email = "giulia.demo@example.com", userName = "Giulia Demo", barcode = "U-10042",
        walletBalance = 12.4, phone = "+39 333 000 0000",
    )

    val eligibility = MensaEligibility(
        lastVerified = LocalDate.of(2026, 11, 18),
        rules = listOf(
            MensaMealRule(MensaMealRuleId.FULL_MENU, 660),
            MensaMealRule(MensaMealRuleId.REDUCED_MENU, 550),
            MensaMealRule(MensaMealRuleId.LUNCH_BOX_PIZZA, 800),
        ),
    )

    val detail = MensaDishDetail(
        id = 2, name = "PASTA AL POMODORO", description = "Pasta di semola con sugo di pomodoro e basilico.",
        imageUrl = null, netQuantityGrams = 320.0,
        ingredients = listOf(MensaIngredient(1, "Pasta di semola", "90 g"), MensaIngredient(2, "Pomodoro", "120 g")),
        majorAllergens = listOf(gluten), traceAllergens = listOf(milk),
        displayNutrition = true, nutritionSource = "Valori calcolati",
        nutritionRows = listOf(
            MensaNutritionRow("Energia", "kcal", 420.0, 1757.0, 131.0, 549.0, null),
            MensaNutritionRow("Proteine", "g", 14.0, null, 4.4, null, null),
            MensaNutritionRow("Carboidrati", "g", 68.0, null, 21.0, null, null),
            MensaNutritionRow("Grassi", "g", 9.0, null, 2.8, null, null),
        ),
        gdaItems = listOf(MensaGdaItem("Energia", "420 kcal", 21.0, null), MensaGdaItem("Grassi", "9 g", 13.0, null)),
        dietaryBadges = listOf(vegan),
    )
}
