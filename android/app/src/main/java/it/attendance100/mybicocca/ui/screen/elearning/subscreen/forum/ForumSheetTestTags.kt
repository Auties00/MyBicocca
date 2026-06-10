package it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum

/**
 * Stable `testTag` identifiers for [ForumSheetPage] and its list page, referenced by both the
 * production composables and the UI tests so a user-visible copy change never breaks a test and a
 * tag rename is a compile error rather than a silently missed node. The page applies them with
 * `Modifier.testTag`; tests anchor on the constants instead of on Italian text.
 */
object ForumSheetTestTags {
    const val ROOT = "forum:root"
    const val STATE_LOADING = "forum:state:loading"
    const val STATE_EMPTY = "forum:state:empty"
    const val DISCUSSIONS_LIST = "forum:discussionsList"
    const val NEW_DISCUSSION_ACTION = "forum:action:newDiscussion"
    const val COMPOSER = "forum:composer"

    /** A discussion row in the list, keyed by its stable discussion id. */
    fun discussion(id: Int): String = "forum:discussion:$id"
}
