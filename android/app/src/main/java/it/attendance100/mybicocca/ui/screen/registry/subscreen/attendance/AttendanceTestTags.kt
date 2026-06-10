package it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance

/**
 * Stable `testTag` identifiers for [AttendancePage] and its in-sheet pages, referenced by both the
 * screen and its UI tests so a user-visible copy change never breaks a test and a tag rename is a
 * compile error rather than a silently missed node. The screen applies them with `Modifier.testTag`;
 * tests anchor on the constants instead of on Italian text.
 */
object AttendanceTestTags {
    const val ROOT = "attendance:root"

    const val ROOT_LOADING = "attendance:root:loading"
    const val ROOT_ERROR = "attendance:root:error"
    const val ROOT_EMPTY = "attendance:root:empty"
    const val COURSE_LIST = "attendance:root:list"
    const val RILEVA_BUTTON = "attendance:root:rileva"

    const val COURSE_PAGE = "attendance:page:course"

    /** Per-course card in the pending-attendance list, keyed by the course code (falling back to name). */
    fun course(key: String): String = "attendance:course:$key"
}
