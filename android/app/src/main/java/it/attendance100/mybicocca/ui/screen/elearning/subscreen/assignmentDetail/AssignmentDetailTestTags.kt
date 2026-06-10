package it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail

/**
 * Stable `testTag` identifiers for [AssignmentDetailPage] and its overview page, referenced by both
 * the production composables and the UI tests so a user-visible copy change never breaks a test and
 * a tag rename is a compile error rather than a silently missed node. The page applies them with
 * `Modifier.testTag`; tests anchor on the constants instead of on Italian text.
 */
object AssignmentDetailTestTags {
    const val ROOT = "assignment:root"
    const val STATE_LOADING = "assignment:state:loading"
    const val OVERVIEW = "assignment:overview"
    const val COMPOSE_ACTION = "assignment:action:compose"
    const val REMOVE_ACTION = "assignment:action:remove"
}
