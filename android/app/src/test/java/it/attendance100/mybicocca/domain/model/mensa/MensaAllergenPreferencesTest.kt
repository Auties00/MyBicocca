package it.attendance100.mybicocca.domain.model.mensa

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MensaAllergenPreferencesTest {

    private val gluten = MensaAllergen(1, "Glutine", "Glutine", null)
    private val milk = MensaAllergen(7, "Latte", "Latte", null)
    private val plain = MensaDish(
        id = 1, name = "Riso in bianco", shortName = "Riso", courseId = 1, courseName = "Primi",
        mealTypeId = 3, mealTypeName = "Pranzo", imageUrl = null, thumbUrl = null,
        majorAllergens = emptyList(), traceAllergens = emptyList(),
        caloricLabel = null, caloricColorHex = null, dietaryBadges = emptyList(),
    )

    @Test
    fun `no selection never flags a dish`() {
        val dish = plain.copy(majorAllergens = listOf(gluten))
        assertThat(dish.allergenMatch(MensaAllergenPreferences())).isEqualTo(MensaAllergenMatch.NONE)
    }

    @Test
    fun `a contained allergen outranks a trace of another selected one`() {
        val dish = plain.copy(majorAllergens = listOf(gluten), traceAllergens = listOf(milk))
        val preferences = MensaAllergenPreferences(allergenIds = setOf(1, 7))
        assertThat(dish.allergenMatch(preferences)).isEqualTo(MensaAllergenMatch.CONTAINS)
    }

    @Test
    fun `the frozen-ingredients notice is never a match, even when its id was selected`() {
        val frozen = MensaAllergen(19, "Contiene ingredienti surgelati/congelati", "Contiene", null, MensaAllergenKind.CONTENT)
        val dish = plain.copy(majorAllergens = listOf(frozen))

        assertThat(frozen.isFrozenNotice).isTrue()
        assertThat(dish.allergenMatch(MensaAllergenPreferences(allergenIds = setOf(19)))).isEqualTo(MensaAllergenMatch.NONE)
    }

    @Test
    fun `pork is a content notice that matches only when the user chose it`() {
        val pork = MensaAllergen(18, "Contiene Carne di Maiale", "Maiale", null, MensaAllergenKind.CONTENT)
        val dish = plain.copy(majorAllergens = listOf(pork))

        assertThat(pork.isFrozenNotice).isFalse()
        assertThat(dish.allergenMatch(MensaAllergenPreferences(allergenIds = setOf(18)))).isEqualTo(MensaAllergenMatch.CONTAINS)
        assertThat(dish.allergenMatch(MensaAllergenPreferences(allergenIds = setOf(1)))).isEqualTo(MensaAllergenMatch.NONE)
    }

    @Test
    fun `only traces of a selected allergen give a trace match`() {
        val dish = plain.copy(traceAllergens = listOf(milk))
        assertThat(dish.allergenMatch(MensaAllergenPreferences(allergenIds = setOf(7)))).isEqualTo(MensaAllergenMatch.TRACE)
        assertThat(dish.allergenMatch(MensaAllergenPreferences(allergenIds = setOf(1)))).isEqualTo(MensaAllergenMatch.NONE)
    }
}
