package it.attendance100.mybicocca.data.local.elearning.course

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "elearning_course_syllabus",
    primaryKeys = ["account_id", "course_id"],
)
data class CourseSyllabusEntity(
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "course_id") val courseId: Int,
    val language: String,
    @ColumnInfo(name = "export_pdf_url") val exportPdfUrl: String?,
    // JSON-encoded List<SyllabusFieldJson>
    @ColumnInfo(name = "fields_json") val fieldsJson: String,
)
