package it.attendance100.mybicocca.data.local.elearning.course

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "elearning_course_sections",
    primaryKeys = ["account_id", "course_id", "section_id"],
    indices = [Index("account_id", "course_id", "section_number")],
)
data class CourseSectionEntity(
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "course_id") val courseId: Int,
    @ColumnInfo(name = "section_id") val sectionId: Int,
    @ColumnInfo(name = "section_number") val sectionNumber: Int,
    val name: String,
    val summary: String?,
    val visible: Boolean,
)
