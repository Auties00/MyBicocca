package it.attendance100.mybicocca.ui.screen.map.ext

import it.attendance100.mybicocca.domain.model.map.MapBuilding

/** "ATLAS ex U1" -> "ATLAS" + "U1"; sites without a legacy U-number keep their full name. */
fun splitLegacyAlias(name: String): Pair<String, String?> {
    val idx = name.lastIndexOf(" ex U")
    if (idx < 0) return name to null
    return name.substring(0, idx) to name.substring(idx + " ex ".length)
}

/** Maps canteen-equipped Bicocca buildings (`U3`, `U6`, `U12`) to their primary Compass menu id. */
fun MapBuilding.canteenMenuIdOrNull(): Int? {
    val alias = splitLegacyAlias(name).second?.uppercase()
    val normalizedCode = code.value.uppercase()
    return when {
        alias == "U3" || normalizedCode == "U03" || normalizedCode == "U3" -> 701
        alias == "U12" || normalizedCode == "U12" -> 709
        alias == "U6" || normalizedCode == "U06" || normalizedCode == "U6" -> 400
        else -> null
    }
}
