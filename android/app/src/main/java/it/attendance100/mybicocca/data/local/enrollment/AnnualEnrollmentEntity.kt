package it.attendance100.mybicocca.data.local.enrollment

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Offline mirror of one academic-year enrollment row of a career (Esse3 `IscrizioneAnnuale`).
 * Enrollment history is live-first, so this table is written through only on a successful fetch
 * and read back only when the device has no network. The row mirrors the
 * [it.attendance100.mybicocca.domain.model.enrollment.AnnualEnrollment] fields one-to-one, with
 * the nested part-time and suspension objects flattened behind a presence flag, the
 * [it.attendance100.mybicocca.domain.model.enrollment.EnrollmentType] and
 * [it.attendance100.mybicocca.domain.model.enrollment.EnrollmentStatus] enums stored by name, and
 * dates stored as ISO-8601 strings. The derived
 * [it.attendance100.mybicocca.domain.model.enrollment.RenewalState] is recomputed from the cached
 * rows on read, never stored.
 *
 * @property careerId Owning career (Esse3 stuId); scopes the row so accounts never cross.
 * @property enrollmentId Together with [careerId] the row's key; an
 *   [it.attendance100.mybicocca.domain.model.enrollment.EnrollmentId] is unique within a career's
 *   history (one per year).
 * @property cacheOrder Server list position, preserved so the offline list reads back in the same
 *   most-recent-first order the API returned.
 * @property type [it.attendance100.mybicocca.domain.model.enrollment.EnrollmentType] by name, read
 *   back through `valueOf` with an `Unknown` fallback.
 * @property status [it.attendance100.mybicocca.domain.model.enrollment.EnrollmentStatus] by name,
 *   read back through `valueOf` with an `Unknown` fallback.
 * @property hasPartTime Whether the year carried part-time terms; the `partTime` sub-object is
 *   reconstructed only when true, since its scalar columns are all nullable on their own.
 * @property partTimeCredits CFU cap chosen at enrollment; null unless [hasPartTime].
 * @property partTimeExtraCredits CFU added on top of the cap; null unless [hasPartTime].
 * @property partTimeLocked Whether further part-time changes are blocked; null unless
 *   [hasPartTime].
 * @property hasSuspension Whether the year was a suspended (fictitious) enrollment; the
 *   `suspension` sub-object is reconstructed only when true, since its reason code may itself be
 *   null even when a suspension is present.
 * @property suspensionReasonCode Esse3 motivation code behind the suspension; null unless
 *   [hasSuspension].
 */
@Entity(tableName = "cached_annual_enrollment", primaryKeys = ["career_id", "enrollment_id"])
data class AnnualEnrollmentEntity(
    @ColumnInfo("career_id") val careerId: Long,
    @ColumnInfo("enrollment_id") val enrollmentId: Long,
    @ColumnInfo("cache_order") val cacheOrder: Int,
    @ColumnInfo("academic_year") val academicYear: Int,
    @ColumnInfo("course_year") val courseYear: Int,
    @ColumnInfo("out_of_course_years") val outOfCourseYears: Int,
    val type: String,
    @ColumnInfo("type_description") val typeDescription: String?,
    val status: String,
    @ColumnInfo("status_reason_code") val statusReasonCode: String?,
    val conditional: Boolean,
    val reconstructed: Boolean,
    @ColumnInfo("has_part_time") val hasPartTime: Boolean,
    @ColumnInfo("part_time_credits") val partTimeCredits: Int?,
    @ColumnInfo("part_time_extra_credits") val partTimeExtraCredits: Int?,
    @ColumnInfo("part_time_locked") val partTimeLocked: Boolean?,
    @ColumnInfo("has_suspension") val hasSuspension: Boolean,
    @ColumnInfo("suspension_reason_code") val suspensionReasonCode: String?,
    @ColumnInfo("awaiting_degree") val awaitingDegree: Boolean,
    @ColumnInfo("degree_award_date") val degreeAwardDate: String?,
    @ColumnInfo("student_type_description") val studentTypeDescription: String?,
    @ColumnInfo("exemption_description") val exemptionDescription: String?,
    @ColumnInfo("income_band_id") val incomeBandId: Long?,
    @ColumnInfo("canteen_band_id") val canteenBandId: Long?,
    @ColumnInfo("merit_band_id") val meritBandId: Long?,
    @ColumnInfo("merit_note") val meritNote: String?,
    @ColumnInfo("enrollment_note") val enrollmentNote: String?,
    @ColumnInfo("disability_percentage") val disabilityPercentage: Float?,
    @ColumnInfo("disability_type_description") val disabilityTypeDescription: String?,
    @ColumnInfo("course_description") val courseDescription: String?,
    @ColumnInfo("course_type_description") val courseTypeDescription: String?,
    @ColumnInfo("degree_class_code") val degreeClassCode: String?,
    @ColumnInfo("degree_class_description") val degreeClassDescription: String?,
    @ColumnInfo("orientation_description") val orientationDescription: String?,
    @ColumnInfo("address_description") val addressDescription: String?,
    @ColumnInfo("study_order_description") val studyOrderDescription: String?,
    @ColumnInfo("minimum_credits") val minimumCredits: Int?,
    @ColumnInfo("course_duration") val courseDuration: Int?,
    @ColumnInfo("teaching_language") val teachingLanguage: String?,
    @ColumnInfo("regulation_code") val regulationCode: String?,
    @ColumnInfo("university_description") val universityDescription: String?,
    @ColumnInfo("site_description") val siteDescription: String?,
    @ColumnInfo("enrollment_date") val enrollmentDate: String?,
    @ColumnInfo("insertion_date") val insertionDate: String?,
    @ColumnInfo("modification_date") val modificationDate: String?,
)
