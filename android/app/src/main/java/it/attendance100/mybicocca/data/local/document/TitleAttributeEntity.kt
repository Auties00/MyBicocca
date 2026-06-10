package it.attendance100.mybicocca.data.local.document

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Offline mirror of one populated field of a cached academic title — the per-row children of
 * [AcademicTitleEntity] that rebuild its
 * [it.attendance100.mybicocca.domain.model.document.AcademicTitle.attributes] list. Replaced
 * wholesale alongside the titles on a successful fetch. The semantic field round-trips by name with
 * a fallback; the value is already display-ready text.
 *
 * @property careerId Owning career (Esse3 stuId), matching the parent title's career.
 * @property titleId Parent title identifier; with [careerId] and [attrOrder], the row's key.
 * @property attrOrder Position within the title's attribute list, preserved so the detail page
 *   reads the facts back in the order the mapper produced them.
 * @property field Semantic field
 *   ([it.attendance100.mybicocca.domain.model.document.TitleField] name).
 * @property value Already-formatted, display-ready text.
 */
@Entity(tableName = "cached_title_attribute", primaryKeys = ["career_id", "title_id", "attr_order"])
data class TitleAttributeEntity(
    @ColumnInfo("career_id") val careerId: Long,
    @ColumnInfo("title_id") val titleId: String,
    @ColumnInfo("attr_order") val attrOrder: Int,
    val field: String,
    val value: String,
)
