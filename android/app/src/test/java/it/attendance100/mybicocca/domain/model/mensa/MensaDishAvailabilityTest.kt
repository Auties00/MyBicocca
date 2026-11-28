package it.attendance100.mybicocca.domain.model.mensa

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MensaDishAvailabilityTest {

    private fun dish(name: String, course: String) = MensaDish(
        id = 1, name = name, shortName = name, courseId = 1, courseName = course, mealTypeId = 3,
        mealTypeName = "Pranzo", imageUrl = null, thumbUrl = null, majorAllergens = emptyList(),
        traceAllergens = emptyList(), caloricLabel = null, caloricColorHex = null, dietaryBadges = emptyList(),
    )

    @Test
    fun `a dish of one building is greyed elsewhere and orderable there`() {
        val grill = dish("GRIGLIA (SOLO IN MENSA U12)", "Secondi Piatti")
        assertThat(grill.availabilityAt(MensaMenuKind.CANTEEN, MensaPlace.CANTEEN_U6))
            .isEqualTo(MensaDishAvailability.OnlyAt("U12"))
        assertThat(grill.availabilityAt(MensaMenuKind.CANTEEN, MensaPlace.CANTEEN_U12))
            .isEqualTo(MensaDishAvailability.Orderable)
    }

    @Test
    fun `a building with a floor suffix matches its place`() {
        val bresaola = dish("BRESAOLA (SOLO IN MENSA U6-1)", "Secondi Piatti")
        assertThat(bresaola.availabilityAt(MensaMenuKind.CANTEEN, MensaPlace.CANTEEN_U6))
            .isEqualTo(MensaDishAvailability.Orderable)
        assertThat(bresaola.availabilityAt(MensaMenuKind.CANTEEN, MensaPlace.CANTEEN_U12))
            .isEqualTo(MensaDishAvailability.OnlyAt("U6-1"))
    }

    @Test
    fun `courses Compass always refuses are not for take-away`() {
        assertThat(dish("PIZZA MARGHERITA", "Pizze").availabilityAt(MensaMenuKind.CANTEEN, MensaPlace.CANTEEN_U12))
            .isEqualTo(MensaDishAvailability.NotForTakeaway)
        assertThat(dish("PIZZA MARGHERITA", "Pizze").availabilityAt(MensaMenuKind.BAR, MensaPlace.BAR_U3))
            .isEqualTo(MensaDishAvailability.Orderable)
        listOf("Piatto unico", "Insalatone", "Dessert", "Pane e Prodotti da forno").forEach { course ->
            assertThat(dish("X", course).availabilityAt(MensaMenuKind.GLUTEN_FREE, MensaPlace.CANTEEN_U12))
                .isEqualTo(MensaDishAvailability.NotForTakeaway)
        }
        assertThat(dish("PASTA S/G", "Primi Piatti").availabilityAt(MensaMenuKind.GLUTEN_FREE, MensaPlace.CANTEEN_U12))
            .isEqualTo(MensaDishAvailability.Orderable)
    }

    @Test
    fun `the place note leaves the displayed name`() {
        assertThat(mensaDishNameWithoutPlaceNote("Griglia Vegetariana S/G (Solo In Mensa U12)")).isEqualTo("Griglia Vegetariana S/G")
        assertThat(mensaDishOnlyAt("Pasta al pomodoro")).isNull()
    }
}
