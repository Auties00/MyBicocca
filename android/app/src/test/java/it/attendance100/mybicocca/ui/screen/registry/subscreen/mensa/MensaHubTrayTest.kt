package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.domain.model.mensa.MensaCourse
import it.attendance100.mybicocca.domain.model.mensa.MensaDaySchedule
import it.attendance100.mybicocca.domain.model.mensa.MensaDayMealType
import it.attendance100.mybicocca.domain.model.mensa.MensaDish
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.junit.Test

class MensaHubTrayTest {

    private fun dish(id: Int, course: Int): MensaDish = mockk(relaxed = true) {
        every { this@mockk.id } returns id
        every { courseId } returns course
    }

    private fun meal(id: Int, order: Int, vararg courses: Pair<Int, List<MensaDish>>) =
        MensaDayMealType(id, "meal $id", order, courses.map { (course, dishes) -> MensaCourse(course, "", "", course, dishes) })

    private fun day(vararg meals: MensaDayMealType) =
        MensaDaySchedule(menuId = 1, date = LocalDate.of(2026, 6, 18), name = "", active = true, mealTypes = meals.toList())

    @Test
    fun `no menu today leaves both plates to the stand-ins`() {
        assertThat(hubTrayOf(null, null)).isEqualTo(MensaHubTray())
        assertThat(hubTrayOf(day(), 3)).isEqualTo(MensaHubTray())
    }

    @Test
    fun `takes a primo and a secondo from the chosen meal`() {
        // 38612 (pasta al pomodoro) and 42239 (chicken) have drawings of their own.
        val lunch = meal(1, 1, 1 to listOf(dish(999001, 1), dish(38612, 1)), 2 to listOf(dish(42239, 2)))
        val dinner = meal(2, 2, 1 to listOf(dish(999002, 1)))
        val tray = hubTrayOf(day(lunch, dinner), mealTypeId = 1)
        assertThat(tray.primo?.id).isEqualTo(38612)
        assertThat(tray.secondo?.id).isEqualTo(42239)
        assertThat(hubTrayOf(day(lunch, dinner), mealTypeId = 2).primo?.id).isEqualTo(999002)
    }

    @Test
    fun `an unknown meal falls back to the day's first one`() {
        val lunch = meal(1, 1, 1 to listOf(dish(999001, 1)))
        val dinner = meal(2, 2, 2 to listOf(dish(999003, 2)))
        val tray = hubTrayOf(day(dinner, lunch), mealTypeId = 42)
        assertThat(tray.primo?.id).isEqualTo(999001)
        assertThat(tray.secondo).isNull()
    }
}
