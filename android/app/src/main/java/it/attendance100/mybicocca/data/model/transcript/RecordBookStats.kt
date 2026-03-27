package it.attendance100.mybicocca.data.model.transcript

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "record_book_stats")
data class RecordBookStats(
    @PrimaryKey val careerId: Long,
    val passedCredits: Float,
    val totalCredits: Float,
    val weightedAverage: Float? = null,
    val arithmeticAverage: Float? = null,
    val passedExamCount: Int,
)
