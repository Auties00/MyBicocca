package it.attendance100.mybicocca.data.model.evaluation

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "evaluation_entries")
data class EvaluationEntry(
    @PrimaryKey val id: Long,
    val activityName: String,
    val activityCode: String? = null,
    val questionnaireId: String? = null,
    val isCompiled: Boolean = false,
    val academicYear: Int? = null,
)
