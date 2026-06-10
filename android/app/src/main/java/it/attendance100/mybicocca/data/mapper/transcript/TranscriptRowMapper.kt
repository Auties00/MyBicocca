package it.attendance100.mybicocca.data.mapper.transcript

import it.attendance100.mybicocca.data.local.transcript.GradeRollupProjection
import it.attendance100.mybicocca.data.local.transcript.TranscriptRowEntity
import it.attendance100.mybicocca.data.local.transcript.TranscriptStatsEntity
import it.attendance100.mybicocca.data.mapper.common.parseEsse3Date
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3State
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TranscriptRow
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TranscriptStats
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.transcript.GradeRollup
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRow
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRowState
import it.attendance100.mybicocca.domain.model.transcript.TranscriptStats
import java.time.LocalDate

/**
 * Maps an Esse3 libretto row to its Room entity. Derived fields: the numeric grade
 * counts only under evaluation mode "V" (voto); the exam date comes from `dataEsa`,
 * a "dd/MM/yyyy HH:mm:ss" value of which only the date part is kept (re-encoded as
 * ISO-8601 for sortable storage); `inStudyPlan` is true when the row is linked to a
 * plan (`pianoId` set), which matches Esse3's `numAdPiano` count, or when it is a
 * year-0 prerequisite — those carry no plan link but are still part of the path —
 * while libretto rows that are neither (e.g. pending/extra activities) are excluded.
 */
fun Esse3TranscriptRow.toEntity(careerId: CareerId): TranscriptRowEntity {
    val mappedState = state.toDomain()
    val outcome = outcome
    val grade = outcome?.grade?.takeIf { outcome.evaluationModeCode.value == "V" }?.toInt()
    val cumLaude = outcome?.cumLaudeFlag == 1
    val parsedDate = outcome?.graduationDate.parseEsse3Date()
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

/**
 * Maps Esse3's record-book stats to the Room stats entity. Averages pick the exam-scale
 * (base 30) entry of each type — base 110 is the graduation-grade projection — where
 * "A" = arithmetic and "P" = weighted (ponderata). The planned-exam count ("exams to
 * take") uses the study-plan activity count (`numAdPiano`), falling back to the libretto
 * activity count: the planned-STATE count (`numAdPianificate`) is unsuitable because it
 * collapses to 0 once everything is passed/attended.
 */
fun Esse3TranscriptStats.toEntity(careerId: CareerId): TranscriptStatsEntity {
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
