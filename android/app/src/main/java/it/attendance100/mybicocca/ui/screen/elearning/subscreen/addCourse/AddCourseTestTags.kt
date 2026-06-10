package it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse

/**
 * Stable `testTag` identifiers for the [AddCourseSheet] catalog browser, referenced by both the
 * sheet and its UI tests so a user-visible copy change never breaks a test and a tag rename is a
 * compile error rather than a silently missed node.
 *
 * The root catalog landing swaps between [ROOT_STATE_LOADING], [ROOT_STATE_ERROR] and
 * [ROOT_STATE_CONTENT] in place. Deeper levels list enrollable courses keyed by their course id
 * via [courseRow], each carrying an enrol affordance tagged via [enrolButton]; the catalog search
 * field is tagged [SEARCH_FIELD].
 */
object AddCourseTestTags {
    const val SEARCH_FIELD = "addCourse:searchField"
    const val ROOT_STATE_LOADING = "addCourse:root:loading"
    const val ROOT_STATE_ERROR = "addCourse:root:error"
    const val ROOT_STATE_CONTENT = "addCourse:root:content"
    const val RETRY_BUTTON = "addCourse:retry"

    /** Prefix of an enrollable course row tag; the suffix is the catalog course id. */
    const val COURSE_ROW_PREFIX = "addCourse:course:"

    /** Prefix of a course row's enrol button tag; the suffix is the catalog course id. */
    const val ENROL_BUTTON_PREFIX = "addCourse:enrol:"

    fun courseRow(courseId: Int): String = "$COURSE_ROW_PREFIX$courseId"

    fun enrolButton(courseId: Int): String = "$ENROL_BUTTON_PREFIX$courseId"
}
