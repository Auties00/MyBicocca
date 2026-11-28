package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergen
import org.junit.Test

class MensaAllergenBentoTest {

    @Test
    fun `the published allergens fill the hand-placed grid with no overlap and no gaps`() {
        val ids = AllergenBentoCells.keys.toList()
        val cells = placeBento(ids, AllergenBentoCells)

        val spots = cells.flatMap { c -> (c.row until c.row + c.rowSpan).flatMap { r -> (c.col until c.col + c.colSpan).map { r to it } } }
        assertThat(spots).containsNoDuplicates()
        val rows = cells.maxOf { it.row + it.rowSpan }
        assertThat(spots).hasSize(rows * 4)
    }

    @Test
    fun `unknown allergens fill the gaps one cell each`() {
        // Without milk and eggs their two cells are free, and the two unknown ids take them.
        val ids = AllergenBentoCells.keys.filter { it != 7 && it != 3 } + listOf(900, 901)
        val cells = placeBento(ids, AllergenBentoCells)

        assertThat(cells[ids.indexOf(900)]).isEqualTo(BentoCell(2, 0))
        assertThat(cells[ids.indexOf(901)]).isEqualTo(BentoCell(3, 0))
    }

    @Test
    fun `a grid of only unknown allergens is laid out row by row`() {
        val cells = placeBento(listOf(1, 2, 3, 4, 5), emptyMap())

        assertThat(cells).containsExactly(
            BentoCell(0, 0), BentoCell(1, 0), BentoCell(2, 0), BentoCell(3, 0), BentoCell(0, 1),
        ).inOrder()
    }

    @Test
    fun `tile labels drop the allergen family and keep the short name otherwise`() {
        assertThat(allergenTileLabel(MensaAllergen(39, "Frutta a guscio: <strong>Noci di Macadamia</strong>", "Macadam.", null)))
            .isEqualTo("Noci di Macadamia")
        assertThat(allergenTileLabel(MensaAllergen(37, "Cereali contenenti glutine: <strong>Grano</strong>", "Grano", null)))
            .isEqualTo("Grano")
        assertThat(allergenTileLabel(MensaAllergen(2, "Crostacei e prodotti a base di Crostacei", "Crostacei", null)))
            .isEqualTo("Crostacei")
    }
}
