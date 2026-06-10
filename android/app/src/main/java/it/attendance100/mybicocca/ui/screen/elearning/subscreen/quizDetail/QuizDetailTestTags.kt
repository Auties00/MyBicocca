package it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail

/**
 * Stable `testTag` identifiers for [QuizDetailPage] and its overview / history pages, referenced by
 * both the production composables and the UI tests so a user-visible copy change never breaks a test
 * and a tag rename is a compile error rather than a silently missed node. The page applies them with
 * `Modifier.testTag`; tests anchor on the constants instead of on Italian text.
 */
object QuizDetailTestTags {
    const val ROOT = "quiz:root"
    const val STATE_LOADING = "quiz:state:loading"
    const val OVERVIEW = "quiz:overview"
    const val PRIMARY_ACTION = "quiz:action:primary"
    const val HISTORY_ACTION = "quiz:action:history"
    const val HISTORY_LIST = "quiz:historyList"

    /** An attempt row in the history list, keyed by its stable attempt id. */
    fun attempt(id: Int): String = "quiz:attempt:$id"
}
