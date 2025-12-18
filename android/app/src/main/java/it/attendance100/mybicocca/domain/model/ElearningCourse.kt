package it.attendance100.mybicocca.domain.model

import androidx.room.*
import java.time.*

@Entity(tableName = "elearning_courses")
data class ElearningCourse(
	@PrimaryKey val id: Int,
	@ColumnInfo(name = "full_name") val fullName: String,
	@ColumnInfo(name = "short_name") val shortName: String,
	@ColumnInfo(name = "summary") val summary: String?,
	@ColumnInfo(name = "start_date") val startDate: Instant,
	@ColumnInfo(name = "end_date") val endDate: Instant,
	@ColumnInfo(name = "is_visible") val isVisible: Boolean,
	@ColumnInfo(name = "progress") val progress: Int? = null, // 0-100
	@ColumnInfo(name = "is_favorite") val isFavorite: Boolean = false,
)
