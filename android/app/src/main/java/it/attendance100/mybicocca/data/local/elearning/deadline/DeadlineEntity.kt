package it.attendance100.mybicocca.data.local.elearning.deadline

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

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
    object Kind {
        const val ASSIGNMENT = "assign"
        const val QUIZ = "quiz"
    }
}
