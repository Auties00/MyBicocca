package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa

import it.attendance100.mybicocca.domain.model.mensa.MensaDaySchedule
import it.attendance100.mybicocca.domain.model.mensa.MensaDish
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.MensaDishDrawings

private const val PRIMI_COURSE_ID = 1
private const val SECONDI_COURSE_ID = 2

/** The primo and secondo on the hub's tray: one of each from today's menu, null where it has none. */
data class MensaHubTray(val primo: MensaDish? = null, val secondo: MensaDish? = null)

/**
 * Today's tray from [day]'s menu, for the meal [mealTypeId] when the day serves it, otherwise its
 * first meal. Each course offers the first dish that has a drawing of its own, so the tray shows
 * the real dish rather than a generic plate whenever it can.
 */
internal fun hubTrayOf(day: MensaDaySchedule?, mealTypeId: Int?): MensaHubTray {
    val meals = day?.mealTypes.orEmpty()
    val meal = meals.firstOrNull { it.id == mealTypeId } ?: meals.minByOrNull { it.order } ?: return MensaHubTray()
    fun pick(courseId: Int): MensaDish? {
        val dishes = meal.courses.filter { it.id == courseId }.flatMap { it.dishes }
        return dishes.firstOrNull { MensaDishDrawings.of(it.id) != null } ?: dishes.firstOrNull()
    }
    return MensaHubTray(primo = pick(PRIMI_COURSE_ID), secondo = pick(SECONDI_COURSE_ID))
}
