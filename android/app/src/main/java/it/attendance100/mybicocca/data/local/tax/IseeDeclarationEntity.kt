package it.attendance100.mybicocca.data.local.tax

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Offline mirror of one ISEE declaration row (Esse3 parametri-iscrizioni-per-tasse) for a
 * career. Written through only on a successful fetch and read back only when the device has no
 * network. Mirrors [it.attendance100.mybicocca.domain.model.tax.IseeDeclaration], which carries
 * no stable id of its own, so the row is keyed by career plus its list position.
 *
 * @property careerId Owning career (Esse3 stuId); scopes the row so accounts never cross.
 * @property cacheOrder Server list position; pairs with [careerId] as the key and preserves the
 *   order the API returned.
 */
@Entity(tableName = "cached_tax_isee", primaryKeys = ["career_id", "cache_order"])
data class IseeDeclarationEntity(
    @ColumnInfo("career_id") val careerId: Long,
    @ColumnInfo("cache_order") val cacheOrder: Int,
    @ColumnInfo("academic_year_enrollment_id") val academicYearEnrollmentId: Long?,
    @ColumnInfo("course_description") val courseDescription: String?,
    val isee: Double?,
    @ColumnInfo("isee_threshold") val iseeThreshold: Double?,
    @ColumnInfo("exemption_description") val exemptionDescription: String?,
)
