package it.attendance100.mybicocca.ui.screen.elearning

/**
 * Stable `testTag` identifiers for [ElearningScreen], referenced by both the screen and its UI
 * tests so a user-visible copy change never breaks a test and a tag rename is a compile error
 * rather than a silently missed node. The screen applies them with `Modifier.testTag`; tests
 * anchor on the constants instead of on Italian text.
 *
 * The state markers are mutually exclusive: exactly one of [STATE_LOADING], [STATE_ERROR],
 * [STATE_EMPTY] or [STATE_CONTENT] shows for a given InitialFetch / Loadable combination. Course
 * rows are keyed by the active edition's course id via [courseItem].
 */
object ElearningTestTags {
    const val ROOT = "elearning:root"
    const val STATE_LOADING = "elearning:state:loading"
    const val STATE_ERROR = "elearning:state:error"
    const val STATE_EMPTY = "elearning:state:empty"
    const val STATE_CONTENT = "elearning:state:content"
    const val FILTER_BAR = "elearning:filterBar"
    const val ADD_COURSE_FAB = "elearning:addCourseFab"

    /** Prefix of a filter toggle tag; the suffix is the option label key. */
    const val FILTER_OPTION_PREFIX = "elearning:filter:"

    /** Prefix of a course-group row tag; the suffix is the active edition's course id. */
    const val COURSE_ITEM_PREFIX = "elearning:course:"

    fun filterOption(key: String): String = "$FILTER_OPTION_PREFIX$key"

    fun courseItem(courseId: Int): String = "$COURSE_ITEM_PREFIX$courseId"
}
