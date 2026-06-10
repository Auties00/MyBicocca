package it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlan

/**
 * Stable `testTag` identifiers for [StudyPlanPage] and its in-sheet pages, referenced by both the
 * screen and its UI tests so a user-visible copy change never breaks a test and a tag rename is a
 * compile error rather than a silently missed node. The screen applies them with `Modifier.testTag`;
 * tests anchor on the constants instead of on Italian text.
 */
object StudyPlanTestTags {
    const val ROOT = "studyPlan:root"

    const val ROOT_LOADING = "studyPlan:root:loading"
    const val ROOT_ERROR = "studyPlan:root:error"
    const val ROOT_EMPTY = "studyPlan:root:empty"
    const val YEAR_LIST = "studyPlan:root:yearList"
    const val EDIT_BUTTON = "studyPlan:root:edit"
    const val PRINT_BUTTON = "studyPlan:root:print"

    const val YEAR_COURSES = "studyPlan:page:yearCourses"

    /** Per-year directory row, keyed by the academic year value. */
    fun yearRow(year: Int): String = "studyPlan:yearRow:$year"
}
