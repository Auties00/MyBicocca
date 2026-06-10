package it.attendance100.mybicocca.data.local.elearning.course

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Cached course sheet ("scheda") of a course, one row per course per account, keyed by
 * (account_id, course_id). Only the language tab selected for the user is stored.
 *
 * @property fieldsJson JSON blob holding the sheet's fields plus the structured info
 * extracted from them, per the syllabus-blob schema in the course mapper.
 */
@Entity(
    tableName = "elearning_course_syllabus",
    primaryKeys = ["account_id", "course_id"],
)
data class CourseSyllabusEntity(
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "course_id") val courseId: Int,
    val language: String,
    @ColumnInfo(name = "export_pdf_url") val exportPdfUrl: String?,
    @ColumnInfo(name = "fields_json") val fieldsJson: String,
)
