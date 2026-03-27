package it.attendance100.mybicocca.data.model.studyplan

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "planned_courses")
data class PlannedCourse(
    @PrimaryKey val id: Long,
    val planId: Long,
    val activityName: String,
    val activityCode: String? = null,
    val credits: Float,
    val year: Int? = null,
    val statusCode: String? = null,
    val statusDescription: String? = null,
)
