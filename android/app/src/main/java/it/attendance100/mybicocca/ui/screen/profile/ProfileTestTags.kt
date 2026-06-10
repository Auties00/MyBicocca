package it.attendance100.mybicocca.ui.screen.profile

/**
 * Stable `testTag` identifiers for [ProfileScreen]/[ProfileContent], referenced by both the body
 * and its UI tests so a user-visible copy change never breaks a test and a tag rename is a compile
 * error rather than a silently missed node. The body applies them with `Modifier.testTag`; tests
 * anchor on the constants instead of on Italian text.
 */
object ProfileTestTags {
    const val ROOT = "profile:root"
    const val STATE_SKELETON = "profile:state:skeleton"
    const val STATE_CONTENT = "profile:state:content"
    const val STUDENT_CARD = "profile:studentCard"
    const val STAT_ARITHMETIC = "profile:stat:arithmetic"
    const val STAT_WEIGHTED = "profile:stat:weighted"
    const val PROGRESS_EXAMS = "profile:progress:exams"
    const val PROGRESS_CREDITS = "profile:progress:credits"
    const val GRADE_CHART = "profile:gradeChart"
}
