package it.attendance100.mybicocca.data.mapper.transcript

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.local.transcript.GradeRollupProjection
import it.attendance100.mybicocca.data.local.transcript.TranscriptRowEntity
import it.attendance100.mybicocca.data.local.transcript.TranscriptStatsEntity
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3EvaluationModeCode
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Result
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3State
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TranscriptAverage
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TranscriptRow
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TranscriptStats
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3AverageTypeCode
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3BaseDefinition
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3VoteGroup
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRowState
import org.junit.Test
import java.time.LocalDate

/**
 * Covers the Esse3 libretto-row and record-book-stats mapping into Room entities and back into
 * the domain. Pins the derived-field rules documented in the mappers: grade only under
 * evaluation mode "V", date stripping/re-encoding, the `inStudyPlan` predicate, the
 * average base-30 preference, the planned-exam fallback chain, and the empty grade-rollup fold.
 */
class TranscriptRowMapperTest {

    private val career = CareerId(7L)

    private fun result(
        evaluationModeCode: Esse3EvaluationModeCode = Esse3EvaluationModeCode.GradeThirtieths,
        grade: Float? = null,
        cumLaudeFlag: Int? = null,
        graduationDate: String? = null,
        academicYearSupervisorId: Int? = null,
    ) = Esse3Result(
        evaluationModeCode = evaluationModeCode,
        grade = grade,
        cumLaudeFlag = cumLaudeFlag,
        graduationDate = graduationDate,
        academicYearSupervisorId = academicYearSupervisorId,
    )

    private fun row(
        activityChoiceId: Long = 100L,
        activityCode: String? = "E3101Q123",
        activityDescription: String = "Analisi Matematica I",
        courseYear: Int = 1,
        weight: Float = 8f,
        state: Esse3State = Esse3State.Passed,
        outcome: Esse3Result? = null,
        planId: Long? = 55L,
        graduationTypeDescription: String? = "Scritto",
        bookableCallsNumber: Int? = 3,
        academicYearAttendanceId: Int? = 2024,
    ) = Esse3TranscriptRow(
        activityChoiceId = activityChoiceId,
        activityCode = activityCode,
        activityDescription = activityDescription,
        courseYear = courseYear,
        state = state,
        weight = weight,
        outcome = outcome,
        planId = planId,
        graduationTypeDescription = graduationTypeDescription,
        bookableCallsNumber = bookableCallsNumber,
        academicYearAttendanceId = academicYearAttendanceId,
    )

    @Test
    fun `grade kept only under evaluation mode V`() {
        val entity = row(outcome = result(grade = 28f)).toEntity(career)
        assertThat(entity.grade).isEqualTo(28)
    }

    @Test
    fun `grade dropped when evaluation mode is not V`() {
        val entity = row(
            outcome = result(evaluationModeCode = Esse3EvaluationModeCode.JudgmentPassFail, grade = 30f),
        ).toEntity(career)
        assertThat(entity.grade).isNull()
    }

    @Test
    fun `grade is null when there is no outcome`() {
        val entity = row(outcome = null).toEntity(career)
        assertThat(entity.grade).isNull()
        assertThat(entity.cumLaude).isFalse()
        assertThat(entity.examDate).isNull()
    }

    @Test
    fun `cum laude derived from lode flag`() {
        val laude = row(outcome = result(grade = 30f, cumLaudeFlag = 1)).toEntity(career)
        val plain = row(outcome = result(grade = 30f, cumLaudeFlag = 0)).toEntity(career)
        assertThat(laude.cumLaude).isTrue()
        assertThat(plain.cumLaude).isFalse()
    }

    @Test
    fun `exam date keeps only the date part re-encoded as ISO`() {
        val entity = row(outcome = result(graduationDate = "12/06/2024 23:59:59")).toEntity(career)
        assertThat(entity.examDate).isEqualTo("2024-06-12")
    }

    @Test
    fun `unparseable exam date becomes null`() {
        val entity = row(outcome = result(graduationDate = "not-a-date")).toEntity(career)
        assertThat(entity.examDate).isNull()
    }

    @Test
    fun `academic year prefers outcome supervisor id over attendance id`() {
        val entity = row(
            outcome = result(academicYearSupervisorId = 2023),
            academicYearAttendanceId = 2024,
        ).toEntity(career)
        assertThat(entity.academicYear).isEqualTo(2023)
    }

    @Test
    fun `academic year falls back to attendance id when supervisor id absent`() {
        val entity = row(
            outcome = result(academicYearSupervisorId = null),
            academicYearAttendanceId = 2024,
        ).toEntity(career)
        assertThat(entity.academicYear).isEqualTo(2024)
    }

    @Test
    fun `inStudyPlan true when linked to a plan`() {
        val entity = row(planId = 99L, courseYear = 2).toEntity(career)
        assertThat(entity.inStudyPlan).isTrue()
    }

    @Test
    fun `inStudyPlan true for year-zero prerequisite without plan link`() {
        val entity = row(planId = null, courseYear = 0).toEntity(career)
        assertThat(entity.inStudyPlan).isTrue()
    }

    @Test
    fun `inStudyPlan false when neither planned nor year-zero`() {
        val entity = row(planId = null, courseYear = 2).toEntity(career)
        assertThat(entity.inStudyPlan).isFalse()
    }

    @Test
    fun `activity name is trimmed`() {
        val entity = row(activityDescription = "  Fisica  ").toEntity(career)
        assertThat(entity.activityName).isEqualTo("Fisica")
    }

    @Test
    fun `exam type trimmed and blank dropped to null`() {
        assertThat(row(graduationTypeDescription = "  Orale  ").toEntity(career).examType)
            .isEqualTo("Orale")
        assertThat(row(graduationTypeDescription = "   ").toEntity(career).examType).isNull()
        assertThat(row(graduationTypeDescription = null).toEntity(career).examType).isNull()
    }

    @Test
    fun `bookable calls fall back to zero`() {
        assertThat(row(bookableCallsNumber = null).toEntity(career).bookableCallsCount).isEqualTo(0)
        assertThat(row(bookableCallsNumber = 4).toEntity(career).bookableCallsCount).isEqualTo(4)
    }

    @Test
    fun `state codes map to domain row states`() {
        assertThat(row(state = Esse3State.Passed).toEntity(career).state)
            .isEqualTo(TranscriptRowState.Passed.name)
        assertThat(row(state = Esse3State.Frequented).toEntity(career).state)
            .isEqualTo(TranscriptRowState.Frequented.name)
        assertThat(row(state = Esse3State.Planned).toEntity(career).state)
            .isEqualTo(TranscriptRowState.Planned.name)
    }

    @Test
    fun `unknown state folds into Planned`() {
        val entity = row(state = Esse3State.Unknown("Z")).toEntity(career)
        assertThat(entity.state).isEqualTo(TranscriptRowState.Planned.name)
    }

    @Test
    fun `entity identity and scalars carried through`() {
        val entity = row(activityChoiceId = 321L, activityCode = "AB12", weight = 6f, courseYear = 3)
            .toEntity(career)
        assertThat(entity.id).isEqualTo(321L)
        assertThat(entity.careerId).isEqualTo(career.value)
        assertThat(entity.activityCode).isEqualTo("AB12")
        assertThat(entity.credits).isEqualTo(6f)
        assertThat(entity.courseYear).isEqualTo(3)
    }

    @Test
    fun `entity round-trips to domain`() {
        val entity = TranscriptRowEntity(
            id = 5L,
            careerId = career.value,
            activityCode = "E3101Q123",
            activityName = "Analisi",
            courseYear = 1,
            credits = 8f,
            state = TranscriptRowState.Passed.name,
            grade = 27,
            cumLaude = true,
            examDate = "2024-06-12",
            academicYear = 2024,
            inStudyPlan = true,
            examType = "Scritto",
            bookableCallsCount = 2,
        )
        val domain = entity.toDomain()
        assertThat(domain.id).isEqualTo(5L)
        assertThat(domain.careerId).isEqualTo(career)
        assertThat(domain.state).isEqualTo(TranscriptRowState.Passed)
        assertThat(domain.grade).isEqualTo(27)
        assertThat(domain.cumLaude).isTrue()
        assertThat(domain.examDate).isEqualTo(LocalDate.of(2024, 6, 12))
        assertThat(domain.passed).isTrue()
    }

    @Test
    fun `domain mapping drops unparseable stored exam date`() {
        val entity = TranscriptRowEntity(
            id = 5L,
            careerId = career.value,
            activityCode = null,
            activityName = "X",
            courseYear = 1,
            credits = 8f,
            state = TranscriptRowState.Planned.name,
            grade = null,
            cumLaude = false,
            examDate = "garbage",
            academicYear = null,
            inStudyPlan = false,
        )
        assertThat(entity.toDomain().examDate).isNull()
    }

    private fun average(typeCode: Esse3AverageTypeCode, base: Int, value: Float) =
        Esse3TranscriptAverage(
            baseDefinition = Esse3BaseDefinition.Unknown(""),
            averageTypeCode = typeCode,
            base = base,
            average = value,
        )

    private fun stats(
        passedWeight: Float? = 90f,
        minWeight: Float? = 180f,
        maxWeight: Float? = 200f,
        passedCount: Int? = 12,
        studyPlanCount: Int? = 20,
        bookletCount: Int? = 18,
        gradeGroup: Esse3VoteGroup? = Esse3VoteGroup(maxPoints = 30, cumLaudeFlag = 1),
        averages: List<Esse3TranscriptAverage> = emptyList(),
    ) = Esse3TranscriptStats(
        passedMeasurementUnitWeight = passedWeight,
        minMeasurementUnitWeight = minWeight,
        maxMeasurementUnitWeight = maxWeight,
        passedTeachingActivityNumber = passedCount,
        studyPlanTeachingActivityNumber = studyPlanCount,
        bookletTeachingActivityNumber = bookletCount,
        gradeGroup = gradeGroup,
        averages = averages,
    )

    @Test
    fun `average picks the base-30 entry of its type`() {
        val entity = stats(
            averages = listOf(
                average(Esse3AverageTypeCode.Arithmetic, base = 110, value = 99f),
                average(Esse3AverageTypeCode.Arithmetic, base = 30, value = 27.5f),
                average(Esse3AverageTypeCode.Weighted, base = 30, value = 26.5f),
            ),
        ).toEntity(career)
        assertThat(entity.arithmeticAverage).isEqualTo(27.5f)
        assertThat(entity.weightedAverage).isEqualTo(26.5f)
    }

    @Test
    fun `average falls back to first of type when no base-30 entry`() {
        val entity = stats(
            averages = listOf(
                average(Esse3AverageTypeCode.Arithmetic, base = 110, value = 99f),
            ),
        ).toEntity(career)
        assertThat(entity.arithmeticAverage).isEqualTo(99f)
    }

    @Test
    fun `average is null when its type is absent`() {
        val entity = stats(averages = emptyList()).toEntity(career)
        assertThat(entity.arithmeticAverage).isNull()
        assertThat(entity.weightedAverage).isNull()
    }

    @Test
    fun `total credits required prefers min then max then zero`() {
        assertThat(stats(minWeight = 180f, maxWeight = 200f).toEntity(career).totalCreditsRequired)
            .isEqualTo(180f)
        assertThat(stats(minWeight = null, maxWeight = 200f).toEntity(career).totalCreditsRequired)
            .isEqualTo(200f)
        assertThat(stats(minWeight = null, maxWeight = null).toEntity(career).totalCreditsRequired)
            .isEqualTo(0f)
    }

    @Test
    fun `passed credits fall back to zero`() {
        assertThat(stats(passedWeight = null).toEntity(career).passedCredits).isEqualTo(0f)
    }

    @Test
    fun `planned exam count prefers study-plan then booklet then zero`() {
        assertThat(stats(studyPlanCount = 20, bookletCount = 18).toEntity(career).plannedExamCount)
            .isEqualTo(20)
        assertThat(stats(studyPlanCount = null, bookletCount = 18).toEntity(career).plannedExamCount)
            .isEqualTo(18)
        assertThat(stats(studyPlanCount = null, bookletCount = null).toEntity(career).plannedExamCount)
            .isEqualTo(0)
    }

    @Test
    fun `passed exam count falls back to zero`() {
        assertThat(stats(passedCount = null).toEntity(career).passedExamCount).isEqualTo(0)
    }

    @Test
    fun `max grade defaults to thirty and cum laude derived from grade group`() {
        val present = stats(gradeGroup = Esse3VoteGroup(maxPoints = 110, cumLaudeFlag = 1))
            .toEntity(career)
        assertThat(present.maxGrade).isEqualTo(110)
        assertThat(present.cumLaudeAvailable).isTrue()

        val absent = stats(gradeGroup = null).toEntity(career)
        assertThat(absent.maxGrade).isEqualTo(30)
        assertThat(absent.cumLaudeAvailable).isFalse()
    }

    @Test
    fun `stats entity round-trips to domain`() {
        val entity = TranscriptStatsEntity(
            careerId = career.value,
            passedCredits = 90f,
            totalCreditsRequired = 180f,
            arithmeticAverage = 27.5f,
            weightedAverage = 26.5f,
            passedExamCount = 12,
            plannedExamCount = 20,
            maxGrade = 30,
            cumLaudeAvailable = true,
        )
        val domain = entity.toDomain()
        assertThat(domain.careerId).isEqualTo(career)
        assertThat(domain.passedCredits).isEqualTo(90f)
        assertThat(domain.totalCreditsRequired).isEqualTo(180f)
        assertThat(domain.arithmeticAverage).isEqualTo(27.5f)
        assertThat(domain.weightedAverage).isEqualTo(26.5f)
        assertThat(domain.passedExamCount).isEqualTo(12)
        assertThat(domain.plannedExamCount).isEqualTo(20)
        assertThat(domain.maxGrade).isEqualTo(30)
        assertThat(domain.cumLaudeAvailable).isTrue()
    }

    @Test
    fun `grade rollup folds null sql sums into zeros`() {
        val rollup = GradeRollupProjection(
            gradedExamCount = 0,
            gradeSum = null,
            weightedGradeSum = null,
            gradedCreditsSum = null,
        ).toDomain()
        assertThat(rollup.gradedExamCount).isEqualTo(0)
        assertThat(rollup.gradeSum).isEqualTo(0L)
        assertThat(rollup.weightedGradeSum).isEqualTo(0.0)
        assertThat(rollup.gradedCreditsSum).isEqualTo(0f)
    }

    @Test
    fun `grade rollup carries non-null sums`() {
        val rollup = GradeRollupProjection(
            gradedExamCount = 3,
            gradeSum = 84L,
            weightedGradeSum = 700.0,
            gradedCreditsSum = 24f,
        ).toDomain()
        assertThat(rollup.gradedExamCount).isEqualTo(3)
        assertThat(rollup.gradeSum).isEqualTo(84L)
        assertThat(rollup.weightedGradeSum).isEqualTo(700.0)
        assertThat(rollup.gradedCreditsSum).isEqualTo(24f)
    }
}
