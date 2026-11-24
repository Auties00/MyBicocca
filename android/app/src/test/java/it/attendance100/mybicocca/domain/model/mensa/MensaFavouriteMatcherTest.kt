package it.attendance100.mybicocca.domain.model.mensa

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class MensaFavouriteMatcherTest {

    private val monday = LocalDate.of(2026, 11, 23)

    private fun dish(id: Int, name: String) = MensaDish(
        id = id, name = name, shortName = name, courseId = 1, courseName = "Primi", mealTypeId = 3,
        mealTypeName = "Pranzo", imageUrl = null, thumbUrl = null, majorAllergens = emptyList(),
        traceAllergens = emptyList(), caloricLabel = null, caloricColorHex = null, dietaryBadges = emptyList(),
    )

    private fun day(date: LocalDate, vararg dishes: MensaDish) = date to MensaDaySchedule(
        menuId = 400, date = date, name = "Menu", active = true,
        mealTypes = listOf(MensaDayMealType(3, "Pranzo", 1, listOf(MensaCourse(1, "Primi", "PR", 1, dishes.toList())))),
    )

    @Test
    fun `finds favourites by id or by the same name, only on the days asked`() {
        val pasta = dish(38612, "PASTA AL POMODORO")
        val sameNameOtherId = dish(99999, "Pasta al pomodoro")
        val other = dish(1, "RISOTTO")
        val schedules = mapOf(
            400 to mapOf(day(monday, pasta, other), day(monday.plusDays(1), sameNameOtherId), day(monday.plusDays(3), pasta)),
        )
        val favourites = listOf(MensaFavourite(38612, "PASTA AL POMODORO"))

        val todayOnly = MensaFavouriteMatcher.find(favourites, schedules, MensaFavouriteMatcher.days(monday, 0))
        assertThat(todayOnly.map { it.dish.id }).containsExactly(38612)

        val twoDays = MensaFavouriteMatcher.find(favourites, schedules, MensaFavouriteMatcher.days(monday, 1))
        assertThat(twoDays.map { it.date }).containsExactly(monday, monday.plusDays(1)).inOrder()
        assertThat(twoDays.map { it.key }).containsNoDuplicates()
    }

    @Test
    fun `no favourites means nothing to report`() {
        assertThat(MensaFavouriteMatcher.find(emptyList(), emptyMap(), listOf(monday))).isEmpty()
    }

    @Test
    fun `the lookahead never reaches past what the canteens publish`() {
        assertThat(MensaFavouriteMatcher.days(monday, 10)).hasSize(MensaFavouriteMatcher.MAX_LOOKAHEAD_DAYS + 1)
    }
}
