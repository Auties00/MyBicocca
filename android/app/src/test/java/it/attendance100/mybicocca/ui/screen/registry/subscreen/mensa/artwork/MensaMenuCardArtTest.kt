package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MensaMenuCardArtTest {

    @Test
    fun `fan cards never place matching colors next to each other horizontally or in the entrance stack`() {
        assertThat(mensaMenuFanCards).hasSize(7)
        assertThat(mensaMenuFanCards.map { it.drawableRes }.toSet()).hasSize(7)

        // Resting Poker Spread (left to right): no adjacent cards share a color.
        mensaMenuFanCards.zipWithNext { left, right ->
            assertThat(left.color).isNotEqualTo(right.color)
        }

        // Entrance Layered Wallet stack (back to front): no adjacent stacked layers share a color.
        val mid = (mensaMenuFanCards.size - 1) / 2
        val stackBackToFront = mensaMenuFanCards
            .withIndex()
            .sortedBy { (index, _) ->
                val absDist = kotlin.math.abs(index - mid)
                val sideRank = if (index > mid) 1 else 0
                (mid - absDist) * 2 + sideRank
            }
            .map { it.value }

        stackBackToFront.zipWithNext { back, front ->
            assertThat(back.color).isNotEqualTo(front.color)
        }
    }
}
