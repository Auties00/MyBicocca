package it.attendance100.mybicocca.demo.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.demo.DemoActivity
import it.attendance100.mybicocca.demo.DemoCareer
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.transcript.AttemptOutcome
import it.attendance100.mybicocca.domain.model.transcript.CourseAttempt
import it.attendance100.mybicocca.domain.model.transcript.CourseDetail
import it.attendance100.mybicocca.domain.model.transcript.GradeRollup
import it.attendance100.mybicocca.domain.model.transcript.PrerequisiteStatus
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRow
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRowState
import it.attendance100.mybicocca.domain.model.transcript.TranscriptStats
import it.attendance100.mybicocca.domain.repository.TranscriptRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/** The transcript and its averages, derived from [DemoCareer.activities]. */
@Singleton
class DemoTranscriptRepository @Inject constructor() : TranscriptRepository {

    private val graded = DemoCareer.activities.filter { it.passed && it.grade != null }

    override fun observeRows(careerId: CareerId): Flow<Loadable<List<TranscriptRow>>> =
        flowOf(Loadable.Loaded(DemoCareer.activities.map { it.toRow(careerId) }))

    override fun observeStats(careerId: CareerId): Flow<Loadable<TranscriptStats>> {
        val passed = DemoCareer.activities.filter { it.passed }
        val weighted = graded.sumOf { it.grade!! * it.credits.toDouble() } / graded.sumOf { it.credits.toDouble() }
        return flowOf(
            Loadable.Loaded(
                TranscriptStats(
                    careerId = careerId,
                    passedCredits = passed.sumOf { it.credits.toDouble() }.toFloat(),
                    totalCreditsRequired = DemoCareer.TOTAL_CREDITS,
                    arithmeticAverage = graded.map { it.grade!! }.average().toFloat(),
                    weightedAverage = weighted.toFloat(),
                    passedExamCount = passed.size,
                    plannedExamCount = DemoCareer.activities.size,
                    maxGrade = 30,
                    cumLaudeAvailable = true,
                ),
            ),
        )
    }

    override fun observeGradeRollup(careerId: CareerId): Flow<Loadable<GradeRollup>> = flowOf(
        Loadable.Loaded(
            GradeRollup(
                gradedExamCount = graded.size,
                gradeSum = graded.sumOf { it.grade!!.toLong() },
                weightedGradeSum = graded.sumOf { it.grade!! * it.credits.toDouble() },
                gradedCreditsSum = graded.sumOf { it.credits.toDouble() }.toFloat(),
            ),
        ),
    )

    override suspend fun refresh(careerId: CareerId, force: Boolean) = Unit

    override suspend fun getCourseDetail(careerId: CareerId, activityChoiceId: Long, alreadyPassed: Boolean): CourseDetail {
        val activity = DemoCareer.activities.firstOrNull { it.id == activityChoiceId }
        return CourseDetail(
            attempts = listOfNotNull(activity?.takeIf { it.passed }?.toAttempt()),
            prerequisites = PrerequisiteStatus.Satisfied,
            bookableCallsCount = if (activity?.attending == true) 2 else 0,
        )
    }

    override suspend fun getPrerequisiteStatus(careerId: CareerId, activityChoiceId: Long): PrerequisiteStatus =
        PrerequisiteStatus.Satisfied

    override suspend fun getPassedCourseNames(careerId: CareerId): Set<String> =
        DemoCareer.activities.filter { it.passed }.map { it.name }.toSet()

    private fun DemoActivity.toRow(careerId: CareerId) = TranscriptRow(
        id = id,
        careerId = careerId,
        activityCode = code,
        activityName = name,
        courseYear = year,
        credits = credits,
        state = when {
            passed -> TranscriptRowState.Passed
            attending -> TranscriptRowState.Frequented
            else -> TranscriptRowState.Planned
        },
        grade = grade,
        cumLaude = cumLaude,
        examDate = passedOn,
        academicYear = 2023 + year,
        inStudyPlan = true,
        examType = if (grade == null && passed) "Idoneità" else "Voto",
        bookableCallsCount = if (attending) 2 else 0,
    )

    private fun DemoActivity.toAttempt() = CourseAttempt(
        id = id * 10,
        callDate = passedOn,
        sessionDescription = "Appello ordinario",
        statusDescription = "Verbalizzato",
        finalOutcome = AttemptOutcome(
            passed = true,
            grade = grade?.toFloat(),
            cumLaude = cumLaude,
            judgment = if (grade == null) "Idoneo" else null,
            date = passedOn,
        ),
        writtenOutcome = null,
        partialOutcome = null,
    )
}
