package it.attendance100.mybicocca.data.local.document

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Offline mirror of one row of a career's academic-titles list (Esse3 titoli). The titles read is
 * live-first, so this table is replaced wholesale on a successful fetch and read back only when the
 * device has no network. Each row mirrors the scalar fields of an
 * [it.attendance100.mybicocca.domain.model.document.AcademicTitle]; its [attributes] list lives in
 * the sibling [TitleAttributeEntity] table, keyed by the same career and title. Enums round-trip by
 * name with a fallback.
 *
 * @property careerId Owning career (Esse3 stuId); scopes the row so accounts never cross.
 * @property titleId Family-prefixed title identifier; with [careerId], the row's key.
 * @property cacheOrder Server list position, preserved so the offline list reads back in the same
 *   order the API returned it.
 * @property category Title family ([it.attendance100.mybicocca.domain.model.document.TitleCategory]
 *   name).
 * @property status Award state ([it.attendance100.mybicocca.domain.model.document.TitleStatus]
 *   name).
 */
@Entity(tableName = "cached_academic_title", primaryKeys = ["career_id", "title_id"])
data class AcademicTitleEntity(
    @ColumnInfo("career_id") val careerId: Long,
    @ColumnInfo("title_id") val titleId: String,
    @ColumnInfo("cache_order") val cacheOrder: Int,
    val category: String,
    val status: String,
    @ColumnInfo("type_description") val typeDescription: String?,
    val subject: String?,
    val institution: String?,
    val country: String?,
    val year: String?,
    val grade: String?,
    @ColumnInfo("cum_laude") val cumLaude: Boolean,
    @ColumnInfo("value_declaration_filed") val valueDeclarationFiled: Boolean,
)
