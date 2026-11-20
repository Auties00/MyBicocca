package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergen
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergenKind
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
import it.attendance100.mybicocca.domain.model.mensa.MensaCartPricing
import it.attendance100.mybicocca.domain.model.mensa.MensaPickupLocation
import it.attendance100.mybicocca.domain.model.mensa.MensaReservation
import it.attendance100.mybicocca.domain.model.mensa.MensaTakeawayDayMealType
import it.attendance100.mybicocca.domain.model.mensa.MensaTakeawayService
import it.attendance100.mybicocca.domain.model.mensa.MensaTimeSlot
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.MensaCartEntry
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

    /** The allergen list as Compass publishes it. */
    val allergens: List<MensaAllergen> = listOf(
        MensaAllergen(2, "Crostacei e prodotti a base di Crostacei", "Crostacei", null),
        MensaAllergen(3, "Uova e prodotti a base di Uova", "Uova", null),
        MensaAllergen(4, "Pesce e prodotti a base di Pesce", "Pesce", null),
        MensaAllergen(5, "Arachidi e prodotti a base di arachidi", "Arachidi", null),
        MensaAllergen(6, "Soia e prodotti a base di Soia", "Soia", null),
        MensaAllergen(7, "Latte e prodotti a base di Latte", "Latte", null),
        MensaAllergen(9, "Sedano e prodotti a base di Sedano", "Sedano", null),
        MensaAllergen(10, "Senape e prodotti a base di Senape", "Senape", null),
        MensaAllergen(11, "Semi di Sesamo e prodotti a base di Sesamo", "Sesamo", null),
        MensaAllergen(13, "Lupini e prodotti a base di Lupini", "Lupini", null),
        MensaAllergen(14, "Molluschi e prodotti a base di Molluschi", "Molluschi", null),
        MensaAllergen(18, "Contiene Carne di Maiale", "Maiale", null, MensaAllergenKind.CONTENT),
        MensaAllergen(19, "Contiene ingredienti surgelati/congelati", "Contiene ingredienti gelo", null, MensaAllergenKind.CONTENT),
        MensaAllergen(24, "Anidride Solforosa e Solfiti >10 mg/l", "Solfiti", null),
        MensaAllergen(37, "Cereali contenenti glutine: <strong>Grano</strong>", "Grano", null),
        MensaAllergen(38, "Frutta a guscio: <strong>Pistacchio</strong>", "Pistacchio", null),
        MensaAllergen(39, "Frutta a guscio: <strong>Noci di Macadamia</strong>", "Macadam.", null),
        MensaAllergen(40, "Frutta a guscio: <strong>Noci Pecan</strong>", "Pecan", null),
        MensaAllergen(41, "Frutta a guscio: <strong>Noci del Brasile</strong>", "N. brasile", null),
        MensaAllergen(42, "Frutta a guscio: <strong>Noce di Acagiù</strong>", "Anacardi", null),
        MensaAllergen(43, "Frutta a guscio: <strong>Noci</strong>", "Noci", null),
        MensaAllergen(44, "Frutta a guscio: <strong>Nocciole</strong>", "Nocciole", null),
        MensaAllergen(45, "Frutta a guscio: <strong>Mandorle</strong>", "Mandorle", null),
        MensaAllergen(46, "Cereali contenenti glutine: <strong>Farro</strong>", "Farro", null),
        MensaAllergen(47, "Cereali contenenti glutine: <strong>Grano Khorasan</strong>", "Khorasan", null),
        MensaAllergen(48, "Cereali contenenti glutine: <strong>Segale</strong>", "Segale", null),
        MensaAllergen(49, "Cereali contenenti glutine: <strong>Orzo</strong>", "Orzo", null),
        MensaAllergen(50, "Cereali contenenti glutine: <strong>Avena</strong>", "Avena", null),
    )
    val gluten = allergens.first { it.id == 37 }
    val glutenSelected = MensaAllergenPreferences(allergenIds = setOf(37))
    val milk = allergens.first { it.id == 7 }

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
            id = 598, name = "Menu Bicocca Senza Glutine", shortName = "Senza Glutine", description = "",
            cateringTypeDescription = "Mensa", activeDays = listOf(today, today.plusDays(1), today.plusDays(4)),
            mealTypes = listOf(MensaMealType(3, "Pranzo", "Pr", 1), MensaMealType(5, "Cena", "Ce", 2)),
        ),
        MensaMenuSummary(
            id = 701, name = "Ristorazione complementare U3", shortName = "U3", description = "Edificio U3",
            cateringTypeDescription = "Bar", activeDays = listOf(today, today.plusDays(1)),
            mealTypes = listOf(MensaMealType(3, "Pranzo", "Pr", 1)),
        ),
        MensaMenuSummary(
            id = 709, name = "Ristorazione complementare U12", shortName = "U12", description = "",
            cateringTypeDescription = "Bar", activeDays = listOf(today, today.plusDays(1)),
            mealTypes = listOf(MensaMealType(3, "Pranzo", "Pr", 1)),
        ),
        MensaMenuSummary(
            id = 712, name = "Ristorazione complementare U6PT", shortName = "U6PT", description = "",
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

/** Booking data for the order and reservation previews, shaped like what Compass returns. */
internal object MensaPreviewOrders {
    private val day = MensaPreviewData.today

    val canteenService = MensaTakeawayService(
        id = 279, name = "Prenotazione ristorazione base", menuId = 400, menuName = "Menù Bicocca", ageRangeId = 5,
        viewPrices = false, walletPayment = false, userRequestsEnabled = true, infoText = null, descriptionText = null,
        maxDaysAhead = 5, isFavorite = false,
        pickupLocations = listOf(
            MensaPickupLocation(141, "Mensa U12", "Ristorazione base"),
            MensaPickupLocation(146, "Mensa U6 -1 (solo pranzo)", "Ristorazione base"),
        ),
        scheduleByDate = mapOf(
            day to listOf(MensaTakeawayDayMealType(3, "Pranzo", listOf(MensaTimeSlot(1116, "Pranzo\n11:45 - 14:30", true, 6, 60, 10.0)))),
        ),
    )

    val barService = MensaTakeawayService(
        id = 435, name = "Prenotazione Lunch box U12", menuId = 709, menuName = "Ristorazione complementare U12", ageRangeId = 5,
        viewPrices = true, walletPayment = true, userRequestsEnabled = true, infoText = null, descriptionText = null,
        maxDaysAhead = 5, isFavorite = false, pickupLocations = emptyList(),
        scheduleByDate = mapOf(
            day to listOf(
                MensaTakeawayDayMealType(
                    3, "Pranzo",
                    listOf("12:00 - 12:30", "12:30 - 13:00", "13:00 - 13:30", "13:30 - 14:00")
                        .mapIndexed { i, label -> MensaTimeSlot(1508 + i, "\n$label", i != 2, i, 20, null) },
                ),
            ),
        ),
    )

    val cart = mapOf(
        MensaPreviewData.plainDish.id to MensaCartEntry(MensaPreviewData.plainDish, 1),
        MensaPreviewData.containsDish.id to MensaCartEntry(MensaPreviewData.containsDish, 2),
    )

    val pricing = MensaCartPricing("€", 15.0, 0.0, 0.0, 15.0, 0.0, "0.42", "KgCO2e", null)

    val reservation = MensaReservation(
        id = 1203341, code = "2026-MA111203341", date = day, rawDate = day.toString(), createdAt = null,
        deliveryAddress = "Mensa U12", menuName = "Menù Bicocca", mealTypeName = "Pranzo",
        canteenName = "Prenotazione ristorazione base", shiftName = "Pranzo (11:45 - 14:30)",
        confirmed = true, deletable = true, active = true, provided = false,
        statusDescription = "Confermata", statusColorHex = "#66BB6A",
        dishes = listOf(MensaPreviewData.plainDish, MensaPreviewData.containsDish),
        barcodeValue = "P-1203341", cfpFinalValue = "0.42", cfpUnit = "KgCO2e",
    )
}
