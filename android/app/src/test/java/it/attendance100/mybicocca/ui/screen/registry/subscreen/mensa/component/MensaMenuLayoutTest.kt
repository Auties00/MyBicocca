package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergen
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergenPreferences
import it.attendance100.mybicocca.domain.model.mensa.MensaCourse
import it.attendance100.mybicocca.domain.model.mensa.MensaDish
import org.junit.Test

class MensaMenuLayoutTest {

    private val gluten = MensaAllergen(1, "Glutine", "Glutine", null)

    private fun dish(id: Int, course: Int, allergens: List<MensaAllergen> = emptyList()) = MensaDish(
        id = id, name = "D$id", shortName = "D$id", courseId = course, courseName = "C$course",
        mealTypeId = 3, mealTypeName = "Pranzo", imageUrl = null, thumbUrl = null,
        majorAllergens = allergens, traceAllergens = emptyList(),
        caloricLabel = null, caloricColorHex = null, dietaryBadges = emptyList(),
    )

    private val courses = listOf(
        MensaCourse(1, "Primi", "", 1, listOf(dish(1, 1), dish(2, 1, listOf(gluten)), dish(3, 1))),
        MensaCourse(2, "Secondi", "", 2, listOf(dish(4, 2))),
    )

    @Test
    fun `each course is a heading followed by rows of two`() {
        val layout = buildMenuLayout(courses, MensaAllergenPreferences())

        assertThat(layout.items.map { it.key }).containsExactly(
            "course_1", "row_1_0", "row_1_1", "course_2", "row_2_0",
        ).inOrder()
        assertThat(layout.headerOfCourse).containsExactly(1, 0, 2, 3)
    }

    @Test
    fun `every dish knows the list item it sits in`() {
        val layout = buildMenuLayout(courses, MensaAllergenPreferences())

        assertThat(layout.dishes.map { it.id }).containsExactly(1, 2, 3, 4).inOrder()
        assertThat(layout.rowOfDish).containsExactly(1, 1, 2, 4).inOrder()
    }

    @Test
    fun `hidden dishes leave the list, the rows and the carousel together`() {
        val layout = buildMenuLayout(
            courses,
            MensaAllergenPreferences(allergenIds = setOf(1), hideMatchingDishes = true),
        )

        assertThat(layout.dishes.map { it.id }).containsExactly(1, 3, 4).inOrder()
        assertThat(layout.rowOfDish).containsExactly(1, 1, 3).inOrder()
        assertThat(layout.items).hasSize(4)
    }

    @Test
    fun `a course with nothing left has no heading`() {
        val layout = buildMenuLayout(
            listOf(MensaCourse(9, "Solo glutine", "", 1, listOf(dish(7, 9, listOf(gluten))))),
            MensaAllergenPreferences(allergenIds = setOf(1), hideMatchingDishes = true),
        )

        assertThat(layout.items).isEmpty()
        assertThat(layout.headerOfCourse).isEmpty()
    }
}
