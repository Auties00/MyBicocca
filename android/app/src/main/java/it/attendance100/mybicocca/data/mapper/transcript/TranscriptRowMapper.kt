package it.attendance100.mybicocca.data.mapper.transcript

import it.attendance100.mybicocca.data.local.transcript.GradeRollupProjection
import it.attendance100.mybicocca.data.local.transcript.TranscriptRowEntity
import it.attendance100.mybicocca.data.local.transcript.TranscriptStatsEntity
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
    // dataEsa comes as "dd/MM/yyyy HH:mm:ss"; keep only the date part.
    val parsedDate = outcome?.graduationDate
        ?.substringBefore(' ')
        ?.let { runCatching { LocalDate.parse(it, ESSE3_DATE) }.getOrNull() }
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
        // In the study plan when linked to it (pianoId set) — matches Esse3's numAdPiano count.
        // Year-0 prerequisites carry no plan link but are still part of the path, so keep them;
        // libretto rows that are neither (e.g. pending/extra activities) are excluded.
        inStudyPlan = planId != null || courseYear <= 0,
        examType = graduationTypeDescription?.trim()?.takeIf { it.isNotEmpty() },
        bookableCallsCount = bookableCallsNumber ?: 0,
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
    inStudyPlan = inStudyPlan,
    examType = examType,
    bookableCallsCount = bookableCallsCount,
)

fun Esse3TranscriptStats.toEntity(careerId: CareerId): TranscriptStatsEntity {
    // Prefer the exam-scale (base 30) average; base 110 is the graduation-grade projection.
    // "A" = arithmetic, "P" = weighted (ponderata).
    fun average(typeCode: String): Float? {
        val ofType = averages.filter { it.averageTypeCode.value == typeCode }
        return (ofType.firstOrNull { it.base == 30 } ?: ofType.firstOrNull())?.average
    }
    return TranscriptStatsEntity(
        careerId = careerId.value,
        passedCredits = passedMeasurementUnitWeight ?: 0f,
        totalCreditsRequired = minMeasurementUnitWeight ?: maxMeasurementUnitWeight ?: 0f,
        arithmeticAverage = average("A"),
        weightedAverage = average("P"),
        passedExamCount = passedTeachingActivityNumber ?: 0,
        // "Exams to take" is the study-plan activity count (numAdPiano); the planned-STATE
        // count (numAdPianificate) collapses to 0 once everything is passed/attended.
        plannedExamCount = studyPlanTeachingActivityNumber ?: bookletTeachingActivityNumber ?: 0,
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
