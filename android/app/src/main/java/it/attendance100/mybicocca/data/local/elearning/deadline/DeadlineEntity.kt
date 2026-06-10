package it.attendance100.mybicocca.data.local.elearning.deadline

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

/**
 * Cached assignment/quiz deadline harvested from the Moodle calendar web service,
 * keyed by (account_id, event_id) — the calendar event id, which stays unique even
 * when one activity carries several dated events. Refreshes replace all rows of the
 * account in one transaction. `instanceId` holds the module instance id (assignment
 * or quiz id), already translated from the course-module id the calendar exports in
 * its `instance` field; `kind` discriminates which module table it points into.
 * `dueAtMs` is epoch milliseconds.
 */
@Entity(
    tableName = "elearning_deadlines",
    primaryKeys = ["account_id", "event_id"],
    indices = [
        Index("account_id", "course_id"),
        Index("account_id", "due_at_ms"),
    ],
)
data class DeadlineEntity(
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "event_id") val eventId: Int,
    @ColumnInfo(name = "course_id") val courseId: Int,
    val kind: String,
    @ColumnInfo(name = "instance_id") val instanceId: Int,
    val title: String,
    @ColumnInfo(name = "due_at_ms") val dueAtMs: Long,
) {
    /** The `kind` discriminator values, matching the Moodle module names. */
    object Kind {
        const val ASSIGNMENT = "assign"
        const val QUIZ = "quiz"
    }
}
