package it.attendance100.mybicocca.data.local.document

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Offline mirror of a career's Esse3 student card ("tessera"). The badge read is live-first, so
 * this single-row table is written through only on a successful fetch and read back only when the
 * device has no network. The row mirrors the
 * [it.attendance100.mybicocca.domain.model.document.StudentBadge] fields one-to-one, with the
 * [BadgeId]/[BadgeBlobId] value classes unwrapped to their underlying longs and dates stored as
 * ISO-8601 strings. A career holds at most one card, so [careerId] is the whole primary key; a
 * live read that finds no badge clears the row, so the absence is faithfully mirrored too.
 *
 * @property careerId Owning career (Esse3 stuId), the row's primary key; scopes the card so
 *   accounts never cross.
 * @property badgeId Esse3 badge identifier.
 * @property blobId Identifier of the card's printable artwork; null when the server holds none.
 */
@Entity(tableName = "cached_student_badge", primaryKeys = ["career_id"])
data class StudentBadgeEntity(
    @ColumnInfo("career_id") val careerId: Long,
    @ColumnInfo("badge_id") val badgeId: Long,
    @ColumnInfo("blob_id") val blobId: Long?,
    @ColumnInfo("has_front_image") val hasFrontImage: Boolean,
    @ColumnInfo("has_rear_image") val hasRearImage: Boolean,
    val rfid: String?,
    @ColumnInfo("student_number") val studentNumber: String?,
    @ColumnInfo("full_name") val fullName: String?,
    @ColumnInfo("course_description") val courseDescription: String?,
    @ColumnInfo("faculty_description") val facultyDescription: String?,
    @ColumnInfo("academic_year") val academicYear: Int?,
    val delivered: Boolean,
    val cancelled: Boolean,
    val returned: Boolean,
    @ColumnInfo("created_on") val createdOn: String?,
    @ColumnInfo("printed_on") val printedOn: String?,
    @ColumnInfo("delivered_on") val deliveredOn: String?,
)
