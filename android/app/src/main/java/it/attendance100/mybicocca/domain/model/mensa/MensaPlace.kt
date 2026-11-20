package it.attendance100.mybicocca.domain.model.mensa

/**
 * Where a meal is eaten or picked up: the first choice of the menu flow. The two canteens share the
 * same menus and differ in when they serve; each bar has a menu of its own.
 *
 * [lunchOnly] mirrors the operator's own label, "Mensa U6 -1 (solo pranzo)": the booking data lists
 * dinner for both canteens and says nothing else about it, so it is kept here for when nobody is
 * signed in and the pickup places are not available.
 */
enum class MensaPlace(
    val kind: MensaMenuKind,
    val building: String,
    val menuIds: List<Int>,
    val lunchOnly: Boolean,
) {
    CANTEEN_U12(MensaMenuKind.CANTEEN, "U12", listOf(MensaMenuIds.CANTEEN, MensaMenuIds.GLUTEN_FREE), lunchOnly = false),
    CANTEEN_U6(MensaMenuKind.CANTEEN, "U6", listOf(MensaMenuIds.CANTEEN, MensaMenuIds.GLUTEN_FREE), lunchOnly = true),
    BAR_U3(MensaMenuKind.BAR, "U3", listOf(MensaMenuIds.BAR_U3), lunchOnly = true),
    BAR_U6(MensaMenuKind.BAR, "U6", listOf(MensaMenuIds.BAR_U6), lunchOnly = true),
    BAR_U12(MensaMenuKind.BAR, "U12", listOf(MensaMenuIds.BAR_U12), lunchOnly = true),
    ;

    val isCanteen: Boolean get() = kind == MensaMenuKind.CANTEEN

    /** Whether a menu has to be picked after the place: only the canteens have more than one. */
    val hasMenuChoice: Boolean get() = menuIds.size > 1

    val displayName: String get() = if (isCanteen) "Mensa $building" else "Bar $building"

    fun identity(): MensaMenuIdentity = MensaMenuIdentity(kind, displayName, building)

    /** The pickup place the operator lists for this canteen, if the signed-in account is shown any. */
    fun pickupLocation(service: MensaTakeawayService?): MensaPickupLocation? {
        if (!isCanteen) return null
        return service?.pickupLocations?.firstOrNull { it.mentions(building) }
    }

    /** Meals to offer out of those a day has, lunch being the first by [order]. */
    fun <T> mealsOf(meals: List<T>, order: (T) -> Int, pickup: MensaPickupLocation? = null): List<T> =
        if (lunchOnly || pickup?.lunchOnly == true) listOfNotNull(meals.minByOrNull(order)) else meals

    companion object {
        /** The place a menu is usually eaten at, for callers that only know the menu. */
        fun forMenu(menuId: Int): MensaPlace? = entries.firstOrNull { !it.isCanteen && menuId in it.menuIds }
    }
}

private val BuildingWord = Regex("""\bU(\d+)\b""", RegexOption.IGNORE_CASE)

private fun MensaPickupLocation.mentions(building: String): Boolean =
    BuildingWord.findAll(name).any { it.value.equals(building, ignoreCase = true) }

/** The operator marks a pickup place that closes after lunch in its name. */
val MensaPickupLocation.lunchOnly: Boolean
    get() = name.contains("solo pranzo", ignoreCase = true)
