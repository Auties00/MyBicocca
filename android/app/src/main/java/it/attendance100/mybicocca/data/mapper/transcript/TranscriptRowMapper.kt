package it.attendance100.mybicocca.data.mapper.transcript

import it.attendance100.mybicocca.data.local.transcript.GradeRollupProjection
import it.attendance100.mybicocca.data.local.transcript.TranscriptRowEntity
import it.attendance100.mybicocca.data.local.transcript.TranscriptStatsEntity
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3AverageTypeCode
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3State
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TranscriptRow
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TranscriptStats
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.transcript.GradeRollup
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRow
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRowState
import it.attendance100.mybicocca.domain.model.transcript.TranscriptStats
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val ESSE3_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy")

fun Esse3TranscriptRow.toEntity(careerId: CareerId): TranscriptRowEntity {
    val mappedState = state.toDomain()
    val outcome = outcome
    val grade = outcome?.grade?.takeIf { outcome.evaluationModeCode.value == "V" }?.toInt()
    val cumLaude = outcome?.cumLaudeFlag == 1
    val parsedDate = outcome?.graduationDate?.let { runCatching { LocalDate.parse(it, ESSE3_DATE) }.getOrNull() }
    return TranscriptRowEntity(
        id = activityChoiceId,
        careerId = careerId.value,
        activityCode = activityCode,
        activityName = activityDescription.trim(),
        courseYear = courseYear,
        credits = weight,
        state = mappedState.name,
        grade = grade,
        cumLaude = cumLaude,
        examDate = parsedDate?.toString(),
        academicYear = outcome?.academicYearSupervisorId ?: academicYearAttendanceId,
    )
}

fun TranscriptRowEntity.toDomain(): TranscriptRow = TranscriptRow(
    id = id,
    careerId = CareerId(careerId),
    activityCode = activityCode,
    activityName = activityName,
    courseYear = courseYear,
    credits = credits,
    state = enumValueOf<TranscriptRowState>(state),
    grade = grade,
    cumLaude = cumLaude,
    examDate = examDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
    academicYear = academicYear,
)

fun Esse3TranscriptStats.toEntity(careerId: CareerId): TranscriptStatsEntity {
    val arithmetic = averages.firstOrNull { it.averageTypeCode is Esse3AverageTypeCode.Arithmetic }?.average
    val weighted = averages.firstOrNull { it.averageTypeCode is Esse3AverageTypeCode.Weighted }?.average
    return TranscriptStatsEntity(
        careerId = careerId.value,
        passedCredits = passedMeasurementUnitWeight ?: 0f,
        totalCreditsRequired = minMeasurementUnitWeight ?: maxMeasurementUnitWeight ?: 0f,
        arithmeticAverage = arithmetic,
        weightedAverage = weighted,
        passedExamCount = passedTeachingActivityNumber ?: 0,
        plannedExamCount = plannedTeachingActivityNumber ?: 0,
        maxGrade = gradeGroup?.maxPoints ?: 30,
        cumLaudeAvailable = (gradeGroup?.cumLaudeFlag ?: 0) == 1,
    )
}

fun TranscriptStatsEntity.toDomain(): TranscriptStats = TranscriptStats(
    careerId = CareerId(careerId),
    passedCredits = passedCredits,
    totalCreditsRequired = totalCreditsRequired,
    arithmeticAverage = arithmeticAverage,
    weightedAverage = weightedAverage,
    passedExamCount = passedExamCount,
    plannedExamCount = plannedExamCount,
    maxGrade = maxGrade,
    cumLaudeAvailable = cumLaudeAvailable,
)

fun GradeRollupProjection.toDomain(): GradeRollup = GradeRollup(
    gradedExamCount = gradedExamCount,
    gradeSum = gradeSum ?: 0L,
    weightedGradeSum = weightedGradeSum ?: 0.0,
    gradedCreditsSum = gradedCreditsSum ?: 0f,
)

private fun Esse3State.toDomain(): TranscriptRowState = when (this) {
    is Esse3State.Passed -> TranscriptRowState.Passed
    is Esse3State.Frequented -> TranscriptRowState.Frequented
    is Esse3State.Planned -> TranscriptRowState.Planned
    is Esse3State.Unknown -> TranscriptRowState.Planned
}
