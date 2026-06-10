package it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires

/**
 * Stable `testTag` identifiers for [QuestionnairesPage] and its in-sheet pages, referenced by both
 * the screen and its UI tests so a user-visible copy change never breaks a test and a tag rename is a
 * compile error rather than a silently missed node. The screen applies them with `Modifier.testTag`;
 * tests anchor on the constants instead of on Italian text.
 */
object QuestionnairesTestTags {
    const val ROOT = "questionnaires:root"

    const val ROOT_LOADING = "questionnaires:root:loading"
    const val ROOT_ERROR = "questionnaires:root:error"
    const val ROOT_EMPTY = "questionnaires:root:empty"
    const val ACTIVITY_LIST = "questionnaires:root:list"

    const val UNITS_PAGE = "questionnaires:page:units"

    /** Per-activity row in the evaluation list, keyed by the activity choice id. */
    fun activity(choiceId: Long): String = "questionnaires:activity:$choiceId"
}
