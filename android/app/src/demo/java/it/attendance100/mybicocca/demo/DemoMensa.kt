package it.attendance100.mybicocca.demo

import it.attendance100.mybicocca.domain.model.mensa.MensaAllergen
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

    val allergens: List<MensaAllergen> = listOf(
        "Glutine", "Crostacei", "Uova", "Pesce", "Arachidi", "Soia", "Latte", "Frutta a guscio",
        "Sedano", "Senape", "Sesamo", "Solfiti", "Lupini", "Molluschi",
    ).mapIndexed { index, name -> MensaAllergen(index + 1, name, name, null) }

    private fun allergen(name: String) = allergens.first { it.name == name }

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
        dish(1001, "PASTA AL POMODORO", 1, "Primi Piatti", calories = "Leggero", contains = listOf("Glutine"), badges = listOf(vegan)),
        dish(1002, "RISOTTO ALLA MONZESE", 1, "Primi Piatti", contains = listOf("Latte"), traces = listOf("Sedano")),
        dish(1003, "CREMA DI CAROTE E ORIGANO", 1, "Primi Piatti", calories = "Leggero", badges = listOf(vegan, organic)),
        dish(1004, "PASTA AL PESTO", 1, "Primi Piatti", contains = listOf("Glutine", "Frutta a guscio", "Latte")),
        dish(1005, "PASTA E FAGIOLI", 1, "Primi Piatti", contains = listOf("Glutine", "Sedano")),
        dish(1101, "COSCE DI POLLO ALLA PAPRIKA", 2, "Secondi Piatti"),
        dish(1102, "FILETTO DI MERLUZZO DORATO", 2, "Secondi Piatti", contains = listOf("Pesce", "Glutine")),
        dish(1103, "ARROSTO DI MAIALE AL VINO ROSSO", 2, "Secondi Piatti", calories = "Calorico", contains = listOf("Solfiti")),
        dish(1104, "FRITTATA AI CARCIOFI", 2, "Secondi Piatti", contains = listOf("Uova")),
        dish(1201, "PATATE AL ROSMARINO", 4, "Contorni", badges = listOf(vegan)),
        dish(1202, "SPINACI AL VAPORE", 4, "Contorni", calories = "Leggero", badges = listOf(vegan)),
        dish(1203, "CAVOLFIORE GRATINATO", 4, "Contorni", contains = listOf("Latte", "Glutine")),
        dish(1301, "FRUTTA FRESCA 150G", 6, "Dessert", calories = "Leggero", badges = listOf(vegan)),
        dish(1302, "YOGURT BIO", 6, "Dessert", contains = listOf("Latte"), badges = listOf(organic)),
        dish(1401, "PANE INTEGRALE BIO", 7, "Pane e Prodotti da forno", contains = listOf("Glutine"), badges = listOf(organic)),
        dish(1402, "GRISSINI", 7, "Pane e Prodotti da forno", contains = listOf("Glutine")),
        dish(1501, "ACQUA NATURALE 0,5 L", 9, "Bevande", calories = "Leggero"),
        dish(1601, "POKE RISO VENERE, SALMONE, ZUCCHINE", 14, "Piatto unico", contains = listOf("Pesce", "Soia", "Sesamo")),
        dish(1701, "INSALATA, CETRIOLO, POMODORO, FETA DOP", 17, "Insalatone", calories = "Leggero", contains = listOf("Latte")),
        dish(1702, "INSALATA, RADICCHIO, FINOCCHI, CECI", 17, "Insalatone", calories = "Leggero", badges = listOf(vegan)),
    )

    private val u3Dishes = listOf(
        dish(2001, "PIZZA MARGHERITA", 12, "Pizze", contains = listOf("Glutine", "Latte")),
        dish(2002, "PIZZA CON VERDURE", 12, "Pizze", contains = listOf("Glutine")),
        dish(2003, "PIZZA 4 STAGIONI", 12, "Pizze", calories = "Calorico", contains = listOf("Glutine", "Latte")),
        dish(2101, "PANINO AL PROSCIUTTO COTTO", 13, "Toast, panini e tramezzini", contains = listOf("Glutine")),
        dish(2102, "PANINO ALLA CAPRESE", 13, "Toast, panini e tramezzini", contains = listOf("Glutine", "Latte")),
        dish(2103, "PIADINA CRUDO ROBIOLA E INSALATA", 13, "Toast, panini e tramezzini", contains = listOf("Glutine", "Latte")),
        dish(1301, "FRUTTA FRESCA 150G", 6, "Dessert", calories = "Leggero", badges = listOf(vegan)),
        dish(1501, "ACQUA NATURALE 0,5 L", 9, "Bevande", calories = "Leggero"),
    )

    val menus: List<MensaMenuSummary> = listOf(
        MensaMenuSummary(
            id = 400, name = "Menù Bicocca", shortName = "Menù Bicocca", description = "Mense U6 e U12",
            cateringTypeDescription = "Mensa", activeDays = serviceDays(),
            mealTypes = listOf(MensaMealType(3, "Pranzo", "Pr", 1), MensaMealType(5, "Cena", "Ce", 2)),
        ),
        MensaMenuSummary(
            id = 701, name = "Ristorazione complementare U3", shortName = "U3", description = "Edificio U3",
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
        val pool = if (menuId == 701) u3Dishes else mainDishes
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
