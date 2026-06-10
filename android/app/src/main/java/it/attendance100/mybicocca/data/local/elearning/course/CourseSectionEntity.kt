package it.attendance100.mybicocca.data.local.elearning.course

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

/**
 * Cached section of a course page, keyed by (account_id, course_id, section_id) and
 * ordered by `sectionNumber`. Refreshes replace all section rows of the course
 * together with its modules in one transaction. `component`/`itemId` mark
 * plugin-owned sections (mod_subsection children) that render inline instead of as
 * top-level cards.
 */
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
    val component: String?,
    @ColumnInfo(name = "item_id") val itemId: Int?,
)
