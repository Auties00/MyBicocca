package it.attendance100.mybicocca.data.mapper.questionnaire

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.questionnaire.ActivityQuestionnaires
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivity
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivityStatus
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireUnit
import org.junit.Test

/**
 * Covers the offline questionnaire-list mirror entity<->domain round-trips: the activity status
 * enum name round-trip with a ConfigurationError fallback, the unit round-trip, and the header
 * round-trip where units are re-attached from the child table on read.
 */
class QuestionnaireCacheMappersTest {

    private val career = CareerId(12L)

    private fun activity(
        activityChoiceId: Long = 1000L,
        status: QuestionnaireActivityStatus = QuestionnaireActivityStatus.ToCompile,
    ) = QuestionnaireActivity(
        activityChoiceId = activityChoiceId,
        activityCode = "E3101Q123",
        activityName = "Analisi",
        credits = 8f,
        courseYear = 1,
        attendanceYear = 2024,
        status = status,
    )

    @Test
    fun `activity records career and order on the entity`() {
        val entity = activity().toEntity(career, order = 7)
        assertThat(entity.careerId).isEqualTo(career.value)
        assertThat(entity.activityChoiceId).isEqualTo(1000L)
        assertThat(entity.cacheOrder).isEqualTo(7)
        assertThat(entity.status).isEqualTo(QuestionnaireActivityStatus.ToCompile.name)
    }

    @Test
    fun `activity round-trips through the cache`() {
        QuestionnaireActivityStatus.entries.forEach { status ->
            val original = activity(status = status)
            val restored = original.toEntity(career, order = 0).toDomain()
            assertThat(restored).isEqualTo(original)
        }
    }

    @Test
    fun `unknown stored status falls back to ConfigurationError`() {
        val entity = activity().toEntity(career, order = 0).copy(status = "MYSTERY")
        assertThat(entity.toDomain().status).isEqualTo(QuestionnaireActivityStatus.ConfigurationError)
    }

    @Test
    fun `unit round-trips through the cache`() {
        val original = QuestionnaireUnit(
            teachingUnitName = "Modulo A",
            lecturerName = "Mario Rossi",
            partitionName = "Turno 1",
            completed = true,
            tags = "TAGS",
        )
        val entity = original.toEntity(career, activityChoiceId = 1000L, order = 2)
        assertThat(entity.careerId).isEqualTo(career.value)
        assertThat(entity.activityChoiceId).isEqualTo(1000L)
        assertThat(entity.unitOrder).isEqualTo(2)
        assertThat(entity.toDomain()).isEqualTo(original)
    }

    @Test
    fun `unit preserves null lecturer and partition`() {
        val original = QuestionnaireUnit(
            teachingUnitName = "Modulo B",
            lecturerName = null,
            partitionName = null,
            completed = false,
            tags = "T",
        )
        val restored = original.toEntity(career, activityChoiceId = 5L, order = 0).toDomain()
        assertThat(restored).isEqualTo(original)
    }

    @Test
    fun `header round-trips and re-attaches units on read`() {
        val original = ActivityQuestionnaires(
            activityChoiceId = 1000L,
            questionnaireId = 11,
            questionnaireConfigId = 22,
            questionnaireName = "Valutazione",
            anonymous = true,
            units = listOf(
                QuestionnaireUnit(
                    teachingUnitName = "Modulo A",
                    lecturerName = "Mario Rossi",
                    partitionName = null,
                    completed = false,
                    tags = "T1",
                ),
            ),
        )
        val entity = original.toEntity(career)
        assertThat(entity.careerId).isEqualTo(career.value)
        assertThat(entity.activityChoiceId).isEqualTo(1000L)
        assertThat(entity.questionnaireId).isEqualTo(11)
        assertThat(entity.questionnaireConfigId).isEqualTo(22)
        assertThat(entity.questionnaireName).isEqualTo("Valutazione")
        assertThat(entity.anonymous).isTrue()

        val restored = entity.toDomain(original.units)
        assertThat(restored).isEqualTo(original)
    }

    @Test
    fun `header round-trips with a closed evaluation window`() {
        val original = ActivityQuestionnaires(
            activityChoiceId = 9L,
            questionnaireId = null,
            questionnaireConfigId = null,
            questionnaireName = null,
            anonymous = false,
            units = emptyList(),
        )
        val restored = original.toEntity(career).toDomain(emptyList())
        assertThat(restored).isEqualTo(original)
    }
}
