package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork

import androidx.annotation.DrawableRes
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.mensa.MensaMenuIdentity
import it.attendance100.mybicocca.domain.model.mensa.MensaMenuKind
import it.attendance100.mybicocca.domain.model.mensa.MensaPlace

/** Width over height of the menu card illustrations (designed at 195 x 312.672). */
internal const val MENSA_MENU_CARD_ASPECT = 195f / 312.672f

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
