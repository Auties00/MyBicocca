package it.attendance100.mybicocca.data.local.elearning.course

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "elearning_course_modules",
    primaryKeys = ["account_id", "course_id", "cm_id"],
    indices = [
        Index("account_id", "course_id", "section_id", "sort_order"),
    ],
)
data class CourseModuleEntity(
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "course_id") val courseId: Int,
    @ColumnInfo(name = "cm_id") val cmId: Int,
    @ColumnInfo(name = "section_id") val sectionId: Int,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    @ColumnInfo(name = "instance_id") val instanceId: Int?,
    val name: String,
    @ColumnInfo(name = "mod_name") val modName: String?,
    @ColumnInfo(name = "type_label") val typeLabel: String?,
    val description: String?,
    val url: String?,
    @ColumnInfo(name = "icon_url") val iconUrl: String?,
    val visible: Boolean,
    @ColumnInfo(name = "accessible") val accessible: Boolean,
    @ColumnInfo(name = "availability_info") val availabilityInfo: String?,
    @ColumnInfo(name = "on_course_page") val onCoursePage: Boolean,
    val indent: Int,
    @ColumnInfo(name = "after_link") val afterLink: String?,
    @ColumnInfo(name = "linked_section_id") val linkedSectionId: Int?,
    // JSON-encoded List<ModuleDateJson> per ElearningJson schema
    @ColumnInfo(name = "dates_json") val datesJson: String?,
    // JSON-encoded List<ModuleContentJson> per ElearningJson schema
    @ColumnInfo(name = "contents_json") val contentsJson: String?,
)
