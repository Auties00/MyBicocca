package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork

import androidx.annotation.DrawableRes
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.mensa.MensaMenuIdentity
import it.attendance100.mybicocca.domain.model.mensa.MensaMenuKind
import it.attendance100.mybicocca.domain.model.mensa.MensaPlace

/** Width over height of the menu card illustrations (designed at 195 x 312.706). */
internal const val MENSA_MENU_CARD_ASPECT = 195f / 312.706f

private const val CARD_TOP_NOTCH_RATIO = 5.706f / 312.706f
private const val CARD_CORNER_RATIO = 12.101f / 195f

/**
 * Draws a soft shadow beneath the V4 card body while leaving the protruding top bookmark ribbon
 * unshadowed so the notch never acquires a rectangular halo.
 */
internal fun Modifier.mensaMenuCardShadow(offsetY: Dp = 2.dp, doubleLayer: Boolean = false): Modifier =
    drawBehind {
        val bodyTop = size.height * CARD_TOP_NOTCH_RATIO
        val corner = CornerRadius(size.width * CARD_CORNER_RATIO)
        val dy = offsetY.toPx()
        if (doubleLayer) {
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.14f),
                topLeft = Offset(-0.5.dp.toPx(), bodyTop + dy),
                size = Size(size.width + 1.dp.toPx(), size.height - bodyTop + 0.5.dp.toPx()),
                cornerRadius = corner,
            )
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.20f),
                topLeft = Offset(0f, bodyTop + dy * 0.6f),
                size = Size(size.width, size.height - bodyTop),
                cornerRadius = corner,
            )
        } else {
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.24f),
                topLeft = Offset(0f, bodyTop + dy),
                size = Size(size.width, size.height - bodyTop),
                cornerRadius = corner,
            )
        }
    }

/** Color family of a menu card cover, used to verify no two adjacent cards in a spread collide visually. */
internal enum class MensaMenuCardColor { BROWN, CRIMSON, PURPLE, GREEN, NAVY }

/** Metadata for each card drawable shown in the hub fan deck. */
internal data class MensaMenuFanCard(
    @param:DrawableRes val drawableRes: Int,
    val color: MensaMenuCardColor,
)

/**
 * All seven menu booklet cards ordered left-to-right for the hub fan spread so no adjacent cards
 * (either side-by-side in the fanned spread or vertically stacked in the entrance deck) share a color.
 */
internal val mensaMenuFanCards: List<MensaMenuFanCard> = listOf(
    MensaMenuFanCard(R.drawable.mensa_menu_card_u6gf, MensaMenuCardColor.GREEN),
    MensaMenuFanCard(R.drawable.mensa_menu_card_u6, MensaMenuCardColor.CRIMSON),
    MensaMenuFanCard(R.drawable.mensa_menu_card_u3bar, MensaMenuCardColor.BROWN),
    MensaMenuFanCard(R.drawable.mensa_menu_card_u12, MensaMenuCardColor.CRIMSON),
    MensaMenuFanCard(R.drawable.mensa_menu_card_u12bar, MensaMenuCardColor.NAVY),
    MensaMenuFanCard(R.drawable.mensa_menu_card_u12gf, MensaMenuCardColor.GREEN),
    MensaMenuFanCard(R.drawable.mensa_menu_card_u6bar, MensaMenuCardColor.PURPLE),
)

internal val mensaMenuCardFanDrawables: List<Int> = mensaMenuFanCards.map { it.drawableRes }

/**
 * The illustrated card for a menu as served at a place: the canteen's own and gluten-free menus
 * differ by building, each bar has its own. The canteens default to U12 when no place is known.
 */
@DrawableRes
internal fun mensaMenuCardArt(identity: MensaMenuIdentity, place: MensaPlace? = null): Int {
    val building = (place?.building ?: identity.building)?.uppercase()
    return when (identity.kind) {
        MensaMenuKind.BAR -> when (building) {
            "U3" -> R.drawable.mensa_menu_card_u3bar
            "U6" -> R.drawable.mensa_menu_card_u6bar
            else -> R.drawable.mensa_menu_card_u12bar
        }
        MensaMenuKind.GLUTEN_FREE -> if (building == "U6") R.drawable.mensa_menu_card_u6gf else R.drawable.mensa_menu_card_u12gf
        MensaMenuKind.CANTEEN -> if (building == "U6") R.drawable.mensa_menu_card_u6 else R.drawable.mensa_menu_card_u12
    }
}
