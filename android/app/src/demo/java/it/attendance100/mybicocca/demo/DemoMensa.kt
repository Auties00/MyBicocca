package it.attendance100.mybicocca.demo

import it.attendance100.mybicocca.domain.model.mensa.MensaAllergen
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergenKind
import it.attendance100.mybicocca.domain.model.mensa.MensaCourse
import it.attendance100.mybicocca.domain.model.mensa.MensaDayMealType
import it.attendance100.mybicocca.domain.model.mensa.MensaDaySchedule
import it.attendance100.mybicocca.domain.model.mensa.MensaDietaryBadge
import it.attendance100.mybicocca.domain.model.mensa.MensaDish
import it.attendance100.mybicocca.domain.model.mensa.MensaMealType
import it.attendance100.mybicocca.domain.model.mensa.MensaMenuSummary
import it.attendance100.mybicocca.domain.model.mensa.MensaSession
import java.time.DayOfWeek
import java.time.LocalDate

/** An invented canteen week and account, shared by every Mensa screen of the demo build. */
object DemoMensa {

    private val today: LocalDate = DemoClock.now.toLocalDate()

    val session = MensaSession(
        email = "giulia.demo@example.com",
        userName = "Giulia Demo",
        barcode = "U-10042",
        walletBalance = 12.4,
        phone = "+39 333 000 0000",
    )

    // The list Compass publishes, ids and names as they are.
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

    // The dishes below name allergens the short way; gluten is wheat, nuts are walnuts.
    private fun allergen(name: String): MensaAllergen {
        val short = when (name) {
            "Glutine" -> "Grano"
            "Frutta a guscio" -> "Noci"
            else -> name
        }
        return allergens.first { it.shortName == short }
    }

    private val vegan = MensaDietaryBadge("vegano", "Vegano", null)
    private val organic = MensaDietaryBadge("bio", "Biologico", null)

    private fun dish(
        id: Int,
        name: String,
        courseId: Int,
        courseName: String,
        mealTypeId: Int = 3,
        calories: String = "Medio",
        contains: List<String> = emptyList(),
        traces: List<String> = emptyList(),
        badges: List<MensaDietaryBadge> = emptyList(),
    ) = MensaDish(
        id = id,
        name = name,
        shortName = name,
        courseId = courseId,
        courseName = courseName,
        mealTypeId = mealTypeId,
        mealTypeName = if (mealTypeId == 3) "Pranzo" else "Cena",
        imageUrl = null,
        thumbUrl = null,
        majorAllergens = contains.map(::allergen),
        traceAllergens = traces.map(::allergen),
        caloricLabel = calories,
        caloricColorHex = when (calories) {
            "Leggero" -> "#4CAF50"
            "Calorico" -> "#F44336"
            else -> "#FF9800"
        },
        dietaryBadges = badges,
    )

    private val mainDishes = listOf(
        dish(38612, "PASTA AL POMODORO", 1, "Primi Piatti", calories = "Leggero", contains = listOf("Glutine"), badges = listOf(vegan)),
        dish(61283, "RISOTTO ALLA ZUCCA", 1, "Primi Piatti", contains = listOf("Latte"), traces = listOf("Sedano")),
        dish(38577, "VELLUTATA CON CAROTE E ZENZERO", 1, "Primi Piatti", calories = "Leggero", badges = listOf(vegan, organic)),
        dish(101105, "PASTA PORTOFINO CON BASILICO", 1, "Primi Piatti", contains = listOf("Glutine", "Frutta a guscio", "Latte")),
        dish(61290, "PASTA E FAGIOLI", 1, "Primi Piatti", contains = listOf("Glutine", "Sedano")),
        dish(42239, "COSCE DI POLLO ALLA PAPRIKA", 2, "Secondi Piatti"),
        dish(91057, "FILETTO DI MERLUZZO DORATO", 2, "Secondi Piatti", contains = listOf("Pesce", "Glutine")),
        dish(101632, "ARROSTO DI MAIALE ALLA BIRRA E SENAPE", 2, "Secondi Piatti", calories = "Calorico", contains = listOf("Solfiti")),
        dish(69427, "FRITTATA AI CARCIOFI", 2, "Secondi Piatti", contains = listOf("Uova")),
        dish(101027, "PATATE RUSTICHE", 4, "Contorni", badges = listOf(vegan)),
        dish(69415, "SPINACI AL VAPORE", 4, "Contorni", calories = "Leggero", badges = listOf(vegan)),
        dish(81675, "CAVOLFIORE GRATINATO", 4, "Contorni", contains = listOf("Latte", "Glutine")),
        dish(69399, "FRUTTA FRESCA 150G", 6, "Dessert", calories = "Leggero", badges = listOf(vegan)),
        dish(38624, "YOGURT BIO", 6, "Dessert", contains = listOf("Latte"), badges = listOf(organic)),
        dish(38782, "PANE INTEGRALE BIO", 7, "Pane e Prodotti da forno", contains = listOf("Glutine"), badges = listOf(organic)),
        dish(38622, "GRISSINI", 7, "Pane e Prodotti da forno", contains = listOf("Glutine")),
        dish(38626, "ACQUA NATURALE 0,5 L", 9, "Bevande", calories = "Leggero"),
        dish(101015, "POKE RISO VENERE, SALMONE, ZUCCHINE JULIENNE, POMODORINI", 14, "Piatto unico", contains = listOf("Pesce", "Soia", "Sesamo")),
        dish(91031, "INSALATA, CETRIOLO, POMODORO, FETA DOP", 17, "Insalatone", calories = "Leggero", contains = listOf("Latte")),
        dish(74197, "INSALATA, POMODORO, OLIVE, MOZZARELLA", 17, "Insalatone", calories = "Leggero", badges = listOf(vegan)),
    )

    private val u3Dishes = listOf(
        dish(88465, "PIZZA MARGHERITA", 12, "Pizze", contains = listOf("Glutine", "Latte")),
        dish(88478, "PIZZA VEGETARIANA", 12, "Pizze", contains = listOf("Glutine")),
        dish(88481, "PIZZA 4 STAGIONI", 12, "Pizze", calories = "Calorico", contains = listOf("Glutine", "Latte")),
        dish(88484, "PANINO AL PROSCIUTTO COTTO", 13, "Toast, panini e tramezzini", contains = listOf("Glutine")),
        dish(88498, "PANINO ALLA CAPRESE", 13, "Toast, panini e tramezzini", contains = listOf("Glutine", "Latte")),
        dish(88495, "PIADINA CRUDO ROBIOLA E INSALATA", 13, "Toast, panini e tramezzini", contains = listOf("Glutine", "Latte")),
        dish(69399, "FRUTTA FRESCA 150G", 6, "Dessert", calories = "Leggero", badges = listOf(vegan)),
        dish(38626, "ACQUA NATURALE 0,5 L", 9, "Bevande", calories = "Leggero"),
    )

    val menus: List<MensaMenuSummary> = listOf(
        MensaMenuSummary(
            id = 400, name = "Menù Bicocca", shortName = "Menù Bicocca", description = "Mense U6 e U12",
            cateringTypeDescription = "Mensa", activeDays = serviceDays(),
            mealTypes = listOf(MensaMealType(3, "Pranzo", "Pr", 1), MensaMealType(5, "Cena", "Ce", 2)),
        ),
        MensaMenuSummary(
            id = 598, name = "Menu Bicocca Senza Glutine", shortName = "Senza Glutine", description = "",
            cateringTypeDescription = "Mensa", activeDays = serviceDays(),
            mealTypes = listOf(MensaMealType(3, "Pranzo", "Pr", 1), MensaMealType(5, "Cena", "Ce", 2)),
        ),
        MensaMenuSummary(
            id = 701, name = "Ristorazione complementare U3", shortName = "U3", description = "Edificio U3",
            cateringTypeDescription = "Bar", activeDays = serviceDays(),
            mealTypes = listOf(MensaMealType(3, "Pranzo", "Pr", 1)),
        ),
        MensaMenuSummary(
            id = 709, name = "Ristorazione complementare U12", shortName = "U12", description = "",
            cateringTypeDescription = "Bar", activeDays = serviceDays(),
            mealTypes = listOf(MensaMealType(3, "Pranzo", "Pr", 1)),
        ),
        MensaMenuSummary(
            id = 712, name = "Ristorazione complementare U6PT", shortName = "U6PT", description = "",
            cateringTypeDescription = "Bar", activeDays = serviceDays(),
            mealTypes = listOf(MensaMealType(3, "Pranzo", "Pr", 1)),
        ),
    )

    /** The five weekdays starting at the demo day. */
    fun serviceDays(): List<LocalDate> =
        generateSequence(today) { it.plusDays(1) }
            .filter { it.dayOfWeek != DayOfWeek.SATURDAY && it.dayOfWeek != DayOfWeek.SUNDAY }
            .take(5)
            .toList()

    fun schedule(menuId: Int): Map<LocalDate, MensaDaySchedule> {
        val menu = menus.first { it.id == menuId }
        val pool = if (menuId in setOf(701, 709, 712)) u3Dishes else mainDishes
        return serviceDays().mapIndexed { dayIndex, date ->
            val rotated = pool.drop(dayIndex % 2) + pool.take(dayIndex % 2)
            date to MensaDaySchedule(
                menuId = menuId,
                date = date,
                name = menu.name,
                active = true,
                mealTypes = menu.mealTypes.map { meal ->
                    MensaDayMealType(
                        id = meal.id,
                        name = meal.name,
                        order = meal.order,
                        courses = rotated
                            .map { it.copy(mealTypeId = meal.id, mealTypeName = meal.name) }
                            .groupBy { it.courseId }
                            .map { (courseId, list) ->
                                MensaCourse(courseId ?: 0, list.first().courseName.orEmpty(), "", courseOrder(courseId), list)
                            }
                            .sortedBy { it.order },
                    )
                },
            )
        }.toMap()
    }

    val allDishes: List<MensaDish> = (mainDishes + u3Dishes).distinctBy { it.id }

    private fun courseOrder(courseId: Int?): Int = listOf(1, 2, 14, 17, 12, 13, 4, 6, 7, 9).indexOf(courseId)
}
