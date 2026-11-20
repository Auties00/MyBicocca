package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import it.attendance100.mybicocca.domain.model.mensa.MensaAllergenMatch
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergenPreferences
import it.attendance100.mybicocca.domain.model.mensa.MensaCourse
import it.attendance100.mybicocca.domain.model.mensa.MensaDish
import it.attendance100.mybicocca.domain.model.mensa.allergenMatch

/** One line of the menu list: a course heading, or up to two dish cards. */
internal sealed interface MensaMenuItem {
    val key: String
    val courseId: Int

    data class Header(val course: MensaCourse) : MensaMenuItem {
        override val key: String = "course_${course.id}"
        override val courseId: Int = course.id
    }

    data class DishRow(override val courseId: Int, val position: Int, val dishes: List<MensaDish>) : MensaMenuItem {
        override val key: String = "row_${courseId}_$position"
    }
}

/**
 * The menu as a flat list, built once so the page that draws it and the carousel that scrolls it
 * agree on which list item holds which dish.
 */
internal class MensaMenuLayout(
    val items: List<MensaMenuItem>,
    /** Every dish shown, in menu order: what the carousel pages through. */
    val dishes: List<MensaDish>,
    /** The list item of each dish in [dishes], by position. */
    val rowOfDish: List<Int>,
    val headerOfCourse: Map<Int, Int>,
)

internal fun buildMenuLayout(
    courses: List<MensaCourse>,
    preferences: MensaAllergenPreferences,
): MensaMenuLayout {
    val items = mutableListOf<MensaMenuItem>()
    val dishes = mutableListOf<MensaDish>()
    val rowOfDish = mutableListOf<Int>()
    val headers = mutableMapOf<Int, Int>()
    courses.forEach { course ->
        val visible = course.dishes.filterNot {
            preferences.hideMatchingDishes && it.allergenMatch(preferences) != MensaAllergenMatch.NONE
        }
        if (visible.isEmpty()) return@forEach
        headers[course.id] = items.size
        items += MensaMenuItem.Header(course)
        visible.chunked(2).forEachIndexed { position, pair ->
            val index = items.size
            items += MensaMenuItem.DishRow(course.id, position, pair)
            pair.forEach {
                dishes += it
                rowOfDish += index
            }
        }
    }
    return MensaMenuLayout(items, dishes, rowOfDish, headers)
}
