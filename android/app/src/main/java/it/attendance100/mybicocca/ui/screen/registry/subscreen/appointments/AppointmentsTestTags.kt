package it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments

/**
 * Stable `testTag` identifiers for [AppointmentsPage] and its in-sheet pages, referenced by both the
 * screen and its UI tests so a user-visible copy change never breaks a test and a tag rename is a
 * compile error rather than a silently missed node. The screen applies them with `Modifier.testTag`;
 * tests anchor on the constants instead of on Italian text.
 */
object AppointmentsTestTags {
    const val ROOT = "appointments:root"

    const val RESERVATIONS_PAGE = "appointments:page:reservations"
    const val RESERVATIONS_EMPTY = "appointments:reservations:empty"
    const val RESERVATIONS_LIST = "appointments:reservations:list"
    const val PRENOTA_BUTTON = "appointments:reservations:prenota"

    const val SECTIONS_LOADING = "appointments:sections:loading"
    const val SECTIONS_ERROR = "appointments:sections:error"
    const val SECTIONS_LIST = "appointments:sections:list"

    /** Per-section directory tile, keyed by the macro-section name. */
    fun section(name: String): String = "appointments:section:$name"
}
