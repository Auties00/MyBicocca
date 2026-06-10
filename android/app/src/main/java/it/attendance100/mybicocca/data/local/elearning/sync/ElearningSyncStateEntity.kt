package it.attendance100.mybicocca.data.local.elearning.sync

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Staleness bookkeeping for the e-learning caches: one row per refreshed resource,
 * keyed by (account_id, scope, scope_id). `scope` names the resource family (one of
 * the ElearningSyncScope constants) and `scopeId` narrows it to an instance — a
 * course id, forum id, discussion id — with 0 for account-wide resources. A
 * repository refresh consults the row's `lastRefreshedAtMs` (epoch milliseconds)
 * against the stale policy's TTL and skips the network while the data is fresh,
 * unless forced; every successful refresh re-stamps the row.
 */
@Entity(
    tableName = "elearning_sync_state",
    primaryKeys = ["account_id", "scope", "scope_id"],
)
data class ElearningSyncStateEntity(
    @ColumnInfo(name = "account_id") val accountId: String,
    val scope: String,
    @ColumnInfo(name = "scope_id") val scopeId: Long,
    @ColumnInfo(name = "last_refreshed_at_ms") val lastRefreshedAtMs: Long,
)

/** The `scope` values used by the e-learning repositories, one per cached resource family. */
object ElearningSyncScope {
    const val ENROLLED_COURSES = "enrolled_courses"
    const val COURSE_DETAILS = "course_details"
    const val COURSE_ASSIGNMENTS = "course_assignments"
    const val COURSE_QUIZZES = "course_quizzes"
    const val COURSE_FORUMS = "course_forums"
    const val COURSE_GRADES = "course_grades"
    const val ALL_COURSE_GRADES = "all_course_grades"
    const val FORUM_DISCUSSIONS = "forum_discussions"
    const val DISCUSSION_POSTS = "discussion_posts"
    const val QUIZ_ATTEMPTS = "quiz_attempts"
    const val BADGES = "badges"
}
