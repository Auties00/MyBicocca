package it.attendance100.mybicocca.ui.screen.map.ext

import it.attendance100.mybicocca.domain.model.map.BuildingCategory
import it.attendance100.mybicocca.domain.model.map.MapBuilding
import it.attendance100.mybicocca.domain.model.mensa.MensaPlace

/** "ATLAS ex U1" -> "ATLAS" + "U1"; sites without a legacy U-number keep their full name. */
fun splitLegacyAlias(name: String): Pair<String, String?> {
    val idx = name.lastIndexOf(" ex U")
    if (idx < 0) return name to null
    return name.substring(0, idx) to name.substring(idx + " ex ".length)
}

/** A place one can open the Mensa on from a building: a canteen or a bar. */
data class CanteenTarget(val place: MensaPlace) {
    val isBar: Boolean get() = !place.isCanteen
}

/** The places served in a Bicocca building: U6 and U12 have a canteen and a bar, U3 only a bar. */
fun MapBuilding.canteenTargets(): List<CanteenTarget> {
    val alias = splitLegacyAlias(name).second?.uppercase()
    val normalizedCode = code.value.uppercase()
    return when {
        alias == "U3" || normalizedCode == "U03" || normalizedCode == "U3" ->
            listOf(CanteenTarget(MensaPlace.BAR_U3))
        alias == "U12" || normalizedCode == "U12" ->
            listOf(CanteenTarget(MensaPlace.CANTEEN_U12), CanteenTarget(MensaPlace.BAR_U12))
        alias == "U6" || normalizedCode == "U06" || normalizedCode == "U6" ->
            listOf(CanteenTarget(MensaPlace.CANTEEN_U6), CanteenTarget(MensaPlace.BAR_U6))
        else -> emptyList()
    }
}

/** Whether the building has rooms to list: a canteen-only one, like U12, has none. */
val MapBuilding.hasRooms: Boolean
    get() = category != BuildingCategory.CANTEEN
