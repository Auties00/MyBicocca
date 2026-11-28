package it.attendance100.mybicocca.domain.model.mensa

/**
 * What a Compass menu is, in the way students think of it: the canteen's own menu, its gluten-free
 * variant, or the daily offer of one of the campus bars. The raw names ("Ristorazione
 * complementare U6PT", "Menu Bicocca Senza Glutine") say the same thing less clearly.
 */
enum class MensaMenuKind { CANTEEN, GLUTEN_FREE, BAR }

/**
 * The canteen menus are set meals: Compass takes one dish per course and refuses a second portion
 * or a second dish of the same course. The bars sell any number of anything.
 */
val MensaMenuKind.onePerCourse: Boolean get() = this != MensaMenuKind.BAR

data class MensaMenuIdentity(
    val kind: MensaMenuKind,
    val displayName: String,
    /** The building a bar is in ("U6"); null for the canteen menus, which serve U6 and U12 alike. */
    val building: String?,
)

/** Menu ids Compass publishes for the Bicocca campus; names are the fallback when one changes. */
object MensaMenuIds {
    const val CANTEEN = 400
    const val GLUTEN_FREE = 598
    const val BAR_U3 = 701
    const val BAR_U12 = 709
    const val BAR_U6 = 712
}

fun MensaMenuSummary.identity(): MensaMenuIdentity = mensaMenuIdentity(id, name)

fun mensaMenuIdentity(id: Int, rawName: String): MensaMenuIdentity {
    val name = rawName.lowercase()
    return when {
        id == MensaMenuIds.GLUTEN_FREE || "glutine" in name ->
            MensaMenuIdentity(MensaMenuKind.GLUTEN_FREE, "Senza glutine", null)

        id == MensaMenuIds.BAR_U3 -> bar("U3")
        id == MensaMenuIds.BAR_U12 -> bar("U12")
        id == MensaMenuIds.BAR_U6 -> bar("U6")
        "complementare" in name -> {
            // "ristorazione complementare u6pt" -> the building is the first U-number in it.
            val building = Regex("u\\s?(\\d+)").find(name)?.let { "U${it.groupValues[1]}" }
            if (building != null) bar(building) else MensaMenuIdentity(MensaMenuKind.BAR, rawName.trim(), null)
        }

        // The server sends "Menù" with a broken accent, so the name is rebuilt rather than shown.
        id == MensaMenuIds.CANTEEN || "bicocca" in name ->
            MensaMenuIdentity(MensaMenuKind.CANTEEN, "Menù Bicocca", null)

        else -> MensaMenuIdentity(MensaMenuKind.CANTEEN, rawName.trim(), null)
    }
}

private fun bar(building: String) = MensaMenuIdentity(MensaMenuKind.BAR, "Bar $building", building)
