package it.attendance100.mybicocca.data.model.transcript

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "record_book_rows")
data class RecordBookRow(
    @PrimaryKey val id: Long,
    val careerId: Long,
    val activityName: String,
    val activityCode: String? = null,
    val credits: Float,
    val grade: Int? = null,
    val cumLaude: Boolean,
    val date: String? = null,
    val status: Status
) {
    enum class Status {
        PLANNED,
        FREQUENTED,
        PASSED,
        UNKNOWN
    }
}
