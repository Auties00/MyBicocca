package it.attendance100.mybicocca.domain.model

import androidx.room.*

@Entity(tableName = "questionnaires")
data class Questionnaire(
	@PrimaryKey val id: String,
	@ColumnInfo(name = "title") val title: String,
	@ColumnInfo(name = "description") val description: String?,
	@ColumnInfo(name = "event_code") val eventCode: String, // e.g., EV_VAL_DID
	@ColumnInfo(name = "context_id") val contextId: String?, // adsce_id
	@ColumnInfo(name = "is_mandatory") val isMandatory: Boolean,
	@ColumnInfo(name = "is_completed") val isCompleted: Boolean,
	@ColumnInfo(name = "course_name") val courseName: String?,
)
