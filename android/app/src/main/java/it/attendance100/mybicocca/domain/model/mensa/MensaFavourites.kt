package it.attendance100.mybicocca.domain.model.mensa

import java.text.Normalizer
import java.time.LocalDate

/** A dish the user wants to hear about. The name is kept to show it while no menu lists it. */
data class MensaFavourite(val dishId: Int, val name: String)

/**
 * Whether to be told when a favourite is on a menu, and how far ahead to look: 0 is today only,
 * more looks at the next days too (the canteens publish up to about four).
 */
data class MensaFavouriteSettings(
    val notify: Boolean = false,
    val lookaheadDays: Int = 0,
)

/** A favourite found on a menu: which day, which menu and meal, and the dish as listed there. */
data class MensaFavouriteHit(
    val date: LocalDate,
    val menuId: Int,
    val mealName: String,
    val dish: MensaDish,
) {
    /** Identifies the hit across checks, so the same dish on the same day is announced once. */
    val key: String get() = "$date:$menuId:${dish.id}"
}

object MensaFavouriteMatcher {

    /** The days the check covers: [from] and the following [lookaheadDays]. */
    fun days(from: LocalDate, lookaheadDays: Int): List<LocalDate> =
        (0..lookaheadDays.coerceIn(0, MAX_LOOKAHEAD_DAYS)).map { from.plusDays(it.toLong()) }

    /**
     * The favourites served on [days] across [schedules] (menu id -> day -> schedule). A dish
     * matches by id, or by name for the same dish listed under another id (a gluten-free
     * variant keeps its own id, so it is not matched by name alone unless the names agree).
     */
    fun find(
        favourites: List<MensaFavourite>,
        schedules: Map<Int, Map<LocalDate, MensaDaySchedule>>,
        days: List<LocalDate>,
    ): List<MensaFavouriteHit> {
        if (favourites.isEmpty()) return emptyList()
        val ids = favourites.map { it.dishId }.toSet()
        val names = favourites.map { normalize(it.name) }.toSet()
        return buildList {
            schedules.forEach { (menuId, byDay) ->
                days.forEach { day ->
                    byDay[day]?.mealTypes?.forEach { meal ->
                        meal.courses.flatMap { it.dishes }.forEach { dish ->
                            if (dish.id in ids || normalize(dish.name) in names) {
                                add(MensaFavouriteHit(day, menuId, meal.name, dish))
                            }
                        }
                    }
                }
            }
        }.distinctBy { it.key }.sortedWith(compareBy({ it.date }, { it.menuId }))
    }

    fun normalize(name: String): String =
        Normalizer.normalize(name.lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
            .replace(Regex("[^a-z0-9]+"), " ")
            .trim()

    const val MAX_LOOKAHEAD_DAYS = 4
}
