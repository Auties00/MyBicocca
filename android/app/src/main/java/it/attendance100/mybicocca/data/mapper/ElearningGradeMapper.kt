package it.attendance100.mybicocca.data.mapper

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

internal fun CourseGradeOverviewEntity.toDomain(): CourseGradeOverview =
    CourseGradeOverview(
        courseId = CourseId(courseId),
        courseName = courseName,
        grade = grade,
        maxGrade = maxGrade,
        gradeFormatted = gradeFormatted,
    )

private fun String.parsePercent(): Double? =
    runCatching { trim().removeSuffix("%").trim().replace(',', '.').toDouble() }.getOrNull()

private fun Long?.toMillisOrNullSec(): Long? = this?.takeIf { it > 0 }?.let { it * 1000L }
