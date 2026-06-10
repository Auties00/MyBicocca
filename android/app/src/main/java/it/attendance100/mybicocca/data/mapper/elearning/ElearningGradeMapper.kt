package it.attendance100.mybicocca.data.mapper.elearning

import it.attendance100.mybicocca.data.local.elearning.grade.CourseGradeOverviewEntity
import it.attendance100.mybicocca.data.local.elearning.grade.GradeItemEntity
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningCourseGrade
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGradeItem
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.grade.CourseGradeOverview
import it.attendance100.mybicocca.domain.model.elearning.grade.GradeItem
import it.attendance100.mybicocca.domain.model.elearning.grade.GradeItemType
import java.time.Instant

/**
 * Maps one row of the Moodle grade-items web service into its cache row, recording the
 * gradebook position as the sort order. The name falls back to the activity module
 * name when blank, the percentage is parsed out of Moodle's formatted "NN %" string
 * (commas tolerated as decimal separators), and epoch-second timestamps are normalized
 * to milliseconds.
 */
internal fun ElearningGradeItem.toEntity(
    accountId: AccountId,
    courseId: Int,
    sortOrder: Int,
): GradeItemEntity =
    GradeItemEntity(
        accountId = accountId.value,
        courseId = courseId,
        itemId = id.toLong(),
        name = itemName?.takeIf { it.isNotBlank() } ?: itemModule.orEmpty(),
        typeRaw = itemType ?: "other",
        activityType = itemModule,
        grade = gradeRaw,
        maxGrade = gradeMax,
        percentage = percentageFormatted?.parsePercent(),
        gradeFormatted = gradeFormatted,
        feedback = feedback,
        gradedAtMs = gradeDateGraded.toMillisOrNullSec(),
        sortOrder = sortOrder,
    )

/** Maps a cached grade-item row to the domain model, resolving the item type from its raw value. */
internal fun GradeItemEntity.toDomain(): GradeItem =
    GradeItem(
        id = itemId,
        name = name,
        type = GradeItemType.fromRaw(typeRaw),
        activityType = activityType,
        grade = grade,
        maxGrade = maxGrade,
        percentage = percentage,
        gradeFormatted = gradeFormatted,
        feedback = feedback,
        gradedAt = gradedAtMs?.let(Instant::ofEpochMilli),
    )

/**
 * Maps one course total of the Moodle grades-overview web service into its cache row.
 * The endpoint ships the grade as a formatted string plus a raw value and no course
 * name, so the name is supplied by the caller (or stored empty) and the raw value is
 * parsed to a number when possible.
 */
internal fun ElearningCourseGrade.toEntity(
    accountId: AccountId,
    courseName: String?,
): CourseGradeOverviewEntity =
    CourseGradeOverviewEntity(
        accountId = accountId.value,
        courseId = courseId,
        courseName = courseName.orEmpty(),
        grade = rawGrade?.toDoubleOrNull(),
        maxGrade = null,
        gradeFormatted = grade,
    )

/** Maps a cached course-grade overview row to the domain model. */
internal fun CourseGradeOverviewEntity.toDomain(): CourseGradeOverview =
    CourseGradeOverview(
        courseId = CourseId(courseId),
        courseName = courseName,
        grade = grade,
        maxGrade = maxGrade,
        gradeFormatted = gradeFormatted,
    )

/** Parses Moodle's formatted percentage string ("87,50 %") to a number, null when malformed. */
private fun String.parsePercent(): Double? =
    runCatching { trim().removeSuffix("%").trim().replace(',', '.').toDouble() }.getOrNull()

/** Converts a Moodle epoch-second timestamp to milliseconds, reading 0 as absent. */
private fun Long?.toMillisOrNullSec(): Long? = this?.takeIf { it > 0 }?.let { it * 1000L }
