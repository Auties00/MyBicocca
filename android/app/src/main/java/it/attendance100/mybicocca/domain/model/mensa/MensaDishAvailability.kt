package it.attendance100.mybicocca.domain.model.mensa

/** Whether a dish on the menu can go into a Take-Away order at the place the user picked. */
sealed interface MensaDishAvailability {
    data object Orderable : MensaDishAvailability

    /** Served only at another canteen of the shared menu, e.g. "Griglia (solo in mensa U12)" seen from U6. */
    data class OnlyAt(val building: String) : MensaDishAvailability

    /** On the menu, but Compass never accepts it in a Take-Away order from this menu. */
    data object NotForTakeaway : MensaDishAvailability
}

/** The operator's note on dishes of the shared canteen menu served in one building only. */
private val OnlyAtNote = Regex("""\(\s*solo\s+in\s+mensa\s+([^)]+?)\s*\)""", RegexOption.IGNORE_CASE)

/** The building a dish is limited to ("U12", "U6-1"), or null when every canteen serves it. */
fun mensaDishOnlyAt(name: String): String? = OnlyAtNote.find(name)?.groupValues?.get(1)?.trim()?.uppercase()

/** The dish name without its "(solo in mensa …)" note, which the app shows as a label instead. */
fun mensaDishNameWithoutPlaceNote(name: String): String = name.replace(OnlyAtNote, "").replace(Regex("\\s{2,}"), " ").trim()

/**
 * Courses Compass refuses in a Take-Away order from a menu, whatever else is in it (tested against
 * `temporaryCost`). Other refusals depend on the combination and come back from the server.
 */
private fun MensaMenuKind.refusesCourse(courseName: String): Boolean {
    val course = courseName.trim().lowercase()
    return when (this) {
        MensaMenuKind.CANTEEN -> course.startsWith("pizz")
        MensaMenuKind.GLUTEN_FREE -> listOf("piatto unico", "insalaton", "dessert", "pane").any(course::startsWith)
        MensaMenuKind.BAR -> false
    }
}

/**
 * Whether [dish] can be ordered from a menu of [kind] at [place]. Unknown kind or place leaves it
 * orderable: the server still has the last word.
 */
fun MensaDish.availabilityAt(kind: MensaMenuKind?, place: MensaPlace?): MensaDishAvailability {
    val onlyAt = mensaDishOnlyAt(name)
    if (onlyAt != null && place != null && onlyAt.substringBefore('-') != place.building.uppercase()) {
        return MensaDishAvailability.OnlyAt(onlyAt)
    }
    val course = courseName
    if (kind != null && course != null && kind.refusesCourse(course)) return MensaDishAvailability.NotForTakeaway
    return MensaDishAvailability.Orderable
}
