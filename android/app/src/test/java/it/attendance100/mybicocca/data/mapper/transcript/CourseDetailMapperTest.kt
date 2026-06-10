package it.attendance100.mybicocca.data.mapper.transcript

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3EvaluationModeCode
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PrerequisitesCheck
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Result
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TranscriptTest
import it.attendance100.mybicocca.domain.model.transcript.PrerequisiteStatus
import org.junit.Test
import java.time.LocalDate

/**
 * Covers the course-detail mapping: the attempt ("prova") decode, the per-facet outcome
 * decode (grade only under mode "V", empty facets collapsing to null), and the
 * prerequisite-check `esito == 1` rule.
 */
class CourseDetailMapperTest {

    private fun result(
        evaluationModeCode: Esse3EvaluationModeCode = Esse3EvaluationModeCode.GradeThirtieths,
        supGraduationFlag: Int = 0,
        grade: Float? = null,
        cumLaudeFlag: Int? = null,
        judgmentTypeDescription: String? = null,
        graduationDate: String? = null,
    ) = Esse3Result(
        evaluationModeCode = evaluationModeCode,
        supGraduationFlag = supGraduationFlag,
        grade = grade,
        cumLaudeFlag = cumLaudeFlag,
        judgmentTypeDescription = judgmentTypeDescription,
        graduationDate = graduationDate,
    )

    private fun test(
        activityRegulationId: Long = 9001L,
        callDate: String? = "10/02/2024 00:00:00",
        sessionDescription: String? = "Sessione Invernale",
        regulationStatusDescription: String? = "Superata",
        finalOutcome: Esse3Result? = null,
        writingOutcome: Esse3Result? = null,
        partialOutcome: Esse3Result? = null,
    ) = Esse3TranscriptTest(
        activityRegulationId = activityRegulationId,
        callDate = callDate,
        sessionDescription = sessionDescription,
        regulationStatusDescription = regulationStatusDescription,
        finalOutcome = finalOutcome,
        writingOutcome = writingOutcome,
        partialOutcome = partialOutcome,
    )

    @Test
    fun `attempt carries id and parsed call date`() {
        val attempt = test(activityRegulationId = 42L, callDate = "10/02/2024 00:00:00").toDomain()
        assertThat(attempt.id).isEqualTo(42L)
        assertThat(attempt.callDate).isEqualTo(LocalDate.of(2024, 2, 10))
    }

    @Test
    fun `null call date stays null`() {
        assertThat(test(callDate = null).toDomain().callDate).isNull()
    }

    @Test
    fun `session and status descriptions trimmed and blanked to null`() {
        val trimmed = test(sessionDescription = "  Estiva  ", regulationStatusDescription = "  OK  ")
            .toDomain()
        assertThat(trimmed.sessionDescription).isEqualTo("Estiva")
        assertThat(trimmed.statusDescription).isEqualTo("OK")

        val blanked = test(sessionDescription = "   ", regulationStatusDescription = "")
            .toDomain()
        assertThat(blanked.sessionDescription).isNull()
        assertThat(blanked.statusDescription).isNull()
    }

    @Test
    fun `passed final outcome decodes grade under mode V`() {
        val attempt = test(
            finalOutcome = result(
                supGraduationFlag = 1,
                grade = 30f,
                cumLaudeFlag = 1,
                graduationDate = "10/02/2024",
            ),
        ).toDomain()
        val outcome = attempt.finalOutcome
        assertThat(outcome).isNotNull()
        assertThat(outcome!!.passed).isTrue()
        assertThat(outcome.grade).isEqualTo(30f)
        assertThat(outcome.cumLaude).isTrue()
        assertThat(outcome.date).isEqualTo(LocalDate.of(2024, 2, 10))
    }

    @Test
    fun `grade dropped when not mode V keeping judgment`() {
        val attempt = test(
            finalOutcome = result(
                evaluationModeCode = Esse3EvaluationModeCode.JudgmentPassFail,
                supGraduationFlag = 1,
                grade = 30f,
                judgmentTypeDescription = "Idoneo",
            ),
        ).toDomain()
        val outcome = attempt.finalOutcome!!
        assertThat(outcome.grade).isNull()
        assertThat(outcome.judgment).isEqualTo("Idoneo")
    }

    @Test
    fun `empty facet collapses to null`() {
        val attempt = test(
            finalOutcome = result(
                supGraduationFlag = 0,
                grade = null,
                judgmentTypeDescription = "   ",
                graduationDate = null,
            ),
        ).toDomain()
        assertThat(attempt.finalOutcome).isNull()
    }

    @Test
    fun `facet kept when only the date is present`() {
        val attempt = test(
            finalOutcome = result(supGraduationFlag = 0, graduationDate = "10/02/2024"),
        ).toDomain()
        val outcome = attempt.finalOutcome
        assertThat(outcome).isNotNull()
        assertThat(outcome!!.passed).isFalse()
        assertThat(outcome.date).isEqualTo(LocalDate.of(2024, 2, 10))
    }

    @Test
    fun `facet kept when only passed flag is set`() {
        val attempt = test(finalOutcome = result(supGraduationFlag = 1)).toDomain()
        val outcome = attempt.finalOutcome
        assertThat(outcome).isNotNull()
        assertThat(outcome!!.passed).isTrue()
        assertThat(outcome.grade).isNull()
        assertThat(outcome.judgment).isNull()
        assertThat(outcome.date).isNull()
    }

    @Test
    fun `each facet decodes independently`() {
        val attempt = test(
            finalOutcome = result(supGraduationFlag = 1, grade = 28f),
            writingOutcome = result(supGraduationFlag = 1, grade = 26f, graduationDate = "01/01/2024"),
            partialOutcome = null,
        ).toDomain()
        assertThat(attempt.finalOutcome).isNotNull()
        assertThat(attempt.writtenOutcome).isNotNull()
        assertThat(attempt.partialOutcome).isNull()
        assertThat(attempt.writtenOutcome!!.grade).isEqualTo(26f)
        assertThat(attempt.bestOutcome).isEqualTo(attempt.finalOutcome)
    }

    @Test
    fun `prerequisite outcome one means satisfied`() {
        assertThat(Esse3PrerequisitesCheck(outcome = 1).toDomain())
            .isEqualTo(PrerequisiteStatus.Satisfied)
    }

    @Test
    fun `prerequisite outcome other than one means not satisfied`() {
        assertThat(Esse3PrerequisitesCheck(outcome = 0).toDomain())
            .isEqualTo(PrerequisiteStatus.NotSatisfied)
        assertThat(Esse3PrerequisitesCheck(outcome = null).toDomain())
            .isEqualTo(PrerequisiteStatus.NotSatisfied)
    }
}
