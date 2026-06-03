package it.attendance100.mybicocca.ui.screen.map.ext

// "ATLAS ex U1" -> "ATLAS" + "U1"; sites without a legacy U-number keep their full name.
fun splitLegacyAlias(name: String): Pair<String, String?> {
    val idx = name.lastIndexOf(" ex U")
    if (idx < 0) return name to null
    return name.substring(0, idx) to name.substring(idx + " ex ".length)
}
