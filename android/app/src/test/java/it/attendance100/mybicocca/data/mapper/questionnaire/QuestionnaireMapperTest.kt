package it.attendance100.mybicocca.data.mapper.questionnaire

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Applications
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3AvailableAnswers
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3CompiledAnswers
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Paragraphs
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3QuestionnairePage
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3QuestionnaireSummary
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3State
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TeachingUnitLogStudyPlanWebList
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TeachingUnitWithQuestionnaire
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TranscriptRowWithQuestionnaireStatus
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivityStatus
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireQuestionKind
import org.junit.Test

/**
 * Covers the VAL_DID questionnaire DTO -> domain decoding: the `linkState` activity status map,
 * the teaching-unit decode (tags/name guards, lecturer title-casing, "N0" partition hiding), the
 * server-page decode (end-page sentinel, format-code question kinds, saved answers), and the
 * summary `completoFlg` flag.
 */
class QuestionnaireMapperTest {

    private fun activityRow(
        linkState: Int? = 3,
        activityChoiceId: Long = 1000L,
        activityCode: String? = "E3101Q123",
        activityDescription: String = "Analisi",
        weight: Float = 8f,
        courseYear: Int = 1,
        academicYearAttendanceId: Int? = 2024,
    ) = Esse3TranscriptRowWithQuestionnaireStatus(
        activityChoiceId = activityChoiceId,
        activityCode = activityCode,
        activityDescription = activityDescription,
        courseYear = courseYear,
        state = Esse3State.Frequented,
        weight = weight,
        academicYearAttendanceId = academicYearAttendanceId,
        linkState = linkState,
    )

    @Test
    fun `link state codes map to activity statuses`() {
        assertThat(activityRow(linkState = 1).toQuestionnaireActivity()!!.status)
            .isEqualTo(QuestionnaireActivityStatus.Completed)
        assertThat(activityRow(linkState = 2).toQuestionnaireActivity()!!.status)
            .isEqualTo(QuestionnaireActivityStatus.PartiallyCompleted)
        assertThat(activityRow(linkState = 3).toQuestionnaireActivity()!!.status)
            .isEqualTo(QuestionnaireActivityStatus.ToCompile)
        assertThat(activityRow(linkState = 4).toQuestionnaireActivity()!!.status)
            .isEqualTo(QuestionnaireActivityStatus.ConfigurationError)
    }

    @Test
    fun `link state zero or absent yields no activity`() {
        assertThat(activityRow(linkState = 0).toQuestionnaireActivity()).isNull()
        assertThat(activityRow(linkState = null).toQuestionnaireActivity()).isNull()
        assertThat(activityRow(linkState = 9).toQuestionnaireActivity()).isNull()
    }

    @Test
    fun `activity fields carried through`() {
        val activity = activityRow(
            activityChoiceId = 77L,
            activityCode = "AB",
            activityDescription = "Reti",
            weight = 6f,
            courseYear = 2,
            academicYearAttendanceId = 2023,
        ).toQuestionnaireActivity()!!
        assertThat(activity.activityChoiceId).isEqualTo(77L)
        assertThat(activity.activityCode).isEqualTo("AB")
        assertThat(activity.activityName).isEqualTo("Reti")
        assertThat(activity.credits).isEqualTo(6f)
        assertThat(activity.courseYear).isEqualTo(2)
        assertThat(activity.attendanceYear).isEqualTo(2023)
    }

    private fun unit(
        tagsValidationDid: String? = "TAG1",
        teachingUnitDescription: String? = "Modulo A",
        activityDescription: String? = "Analisi",
        lecturersName: String? = "VINCENZINA",
        lecturersSurname: String? = "MESSINA",
        domicilePartialDescription: String? = "Turno 1",
        domicilePartialCode: String? = "T1",
        linkState: Int? = 1,
    ) = Esse3TeachingUnitLogStudyPlanWebList(
        tagsValidationDid = tagsValidationDid,
        teachingUnitDescription = teachingUnitDescription,
        activityDescription = activityDescription,
        lecturersName = lecturersName,
        lecturersSurname = lecturersSurname,
        domicilePartialDescription = domicilePartialDescription,
        domicilePartialCode = domicilePartialCode,
        linkState = linkState,
    )

    private fun teachingUnit(
        activityChoiceId: Long? = 1000L,
        questionnaireId: Int? = 11,
        questionConfigId: Int? = 22,
        questionnaireDescription: String? = "Valutazione",
        anonymousFlag: Int? = 1,
        units: List<Esse3TeachingUnitLogStudyPlanWebList> = listOf(unit()),
    ) = Esse3TeachingUnitWithQuestionnaire(
        activityChoiceId = activityChoiceId,
        questionnaireId = questionnaireId,
        questionConfigId = questionConfigId,
        questionnaireDescription = questionnaireDescription,
        anonymousFlag = anonymousFlag,
        teachingUnitLogWebStudyPlanList = units,
    )

    @Test
    fun `teaching unit response decodes header and units`() {
        val result = teachingUnit().toActivityQuestionnaires()!!
        assertThat(result.activityChoiceId).isEqualTo(1000L)
        assertThat(result.questionnaireId).isEqualTo(11)
        assertThat(result.questionnaireConfigId).isEqualTo(22)
        assertThat(result.questionnaireName).isEqualTo("Valutazione")
        assertThat(result.anonymous).isTrue()
        assertThat(result.units).hasSize(1)
    }

    @Test
    fun `null activity id makes the response unusable`() {
        assertThat(teachingUnit(activityChoiceId = null).toActivityQuestionnaires()).isNull()
    }

    @Test
    fun `blank questionnaire name and zero anonymous flag normalize`() {
        val result = teachingUnit(questionnaireDescription = "   ", anonymousFlag = 0)
            .toActivityQuestionnaires()!!
        assertThat(result.questionnaireName).isNull()
        assertThat(result.anonymous).isFalse()
    }

    @Test
    fun `unit dropped without a validation tag`() {
        val result = teachingUnit(units = listOf(unit(tagsValidationDid = null), unit(tagsValidationDid = "  ")))
            .toActivityQuestionnaires()!!
        assertThat(result.units).isEmpty()
    }

    @Test
    fun `unit name falls back to activity description then null`() {
        val fallback = teachingUnit(
            units = listOf(unit(teachingUnitDescription = "   ", activityDescription = "Fisica")),
        ).toActivityQuestionnaires()!!.units.single()
        assertThat(fallback.teachingUnitName).isEqualTo("Fisica")

        val dropped = teachingUnit(
            units = listOf(unit(teachingUnitDescription = null, activityDescription = null)),
        ).toActivityQuestionnaires()!!.units
        assertThat(dropped).isEmpty()
    }

    @Test
    fun `lecturer name is title cased from all caps`() {
        val unit = teachingUnit().toActivityQuestionnaires()!!.units.single()
        assertThat(unit.lecturerName).isEqualTo("Vincenzina Messina")
    }

    @Test
    fun `lecturer name is null when no parts are present`() {
        val unit = teachingUnit(
            units = listOf(unit(lecturersName = "  ", lecturersSurname = null)),
        ).toActivityQuestionnaires()!!.units.single()
        assertThat(unit.lecturerName).isNull()
    }

    @Test
    fun `partition label hidden for the N0 no-partitioning marker`() {
        val hidden = teachingUnit(
            units = listOf(unit(domicilePartialDescription = "Nessuno", domicilePartialCode = "N0")),
        ).toActivityQuestionnaires()!!.units.single()
        assertThat(hidden.partitionName).isNull()

        val shown = teachingUnit(
            units = listOf(unit(domicilePartialDescription = "Turno 2", domicilePartialCode = "T2")),
        ).toActivityQuestionnaires()!!.units.single()
        assertThat(shown.partitionName).isEqualTo("Turno 2")
    }

    @Test
    fun `unit completed flag derived from link state one`() {
        val done = teachingUnit(units = listOf(unit(linkState = 1)))
            .toActivityQuestionnaires()!!.units.single()
        val todo = teachingUnit(units = listOf(unit(linkState = 3)))
            .toActivityQuestionnaires()!!.units.single()
        assertThat(done.completed).isTrue()
        assertThat(todo.completed).isFalse()
        assertThat(done.tags).isEqualTo("TAG1")
    }

    private fun answer(
        answerId: Int? = 1,
        elementsDescription: String? = "Molto d'accordo",
        answerFormatCode: String? = null,
    ) = Esse3AvailableAnswers(
        answerId = answerId,
        elementsDescription = elementsDescription,
        answerFormatCode = answerFormatCode,
    )

    private fun application(
        applicationId: Int? = 10,
        elementsDescription: String? = "Domanda?",
        elementsNote: String? = null,
        mandatoryFlag: Int? = 1,
        formatTypeCode: String? = "TL_DOM_DFS",
        maxChoiceNumber: Int? = null,
        availableAnswers: List<Esse3AvailableAnswers> = listOf(answer(1), answer(2)),
        completeAnswers: List<Esse3CompiledAnswers> = emptyList(),
    ) = Esse3Applications(
        applicationId = applicationId,
        elementsDescription = elementsDescription,
        elementsNote = elementsNote,
        mandatoryFlag = mandatoryFlag,
        formatTypeCode = formatTypeCode,
        maxChoiceNumber = maxChoiceNumber,
        availableAnswers = availableAnswers,
        completeAnswers = completeAnswers,
    )

    private fun page(
        pageId: Int? = 5,
        paragraphs: List<Esse3Paragraphs> = listOf(
            Esse3Paragraphs(
                paragraphId = 3,
                elementsDescription = "Sezione",
                applications = listOf(application()),
            ),
        ),
    ) = Esse3QuestionnairePage(pageId = pageId, paragraphs = paragraphs)

    @Test
    fun `page decodes id and paragraph`() {
        val domain = page(pageId = 5).toDomain()
        assertThat(domain.id).isEqualTo(5L)
        assertThat(domain.isEnd).isFalse()
        assertThat(domain.paragraphs).hasSize(1)
        assertThat(domain.paragraphs.single().title).isEqualTo("Sezione")
        assertThat(domain.paragraphs.single().id).isEqualTo(3L)
    }

    @Test
    fun `missing page id is the negative end sentinel`() {
        val domain = page(pageId = null, paragraphs = emptyList()).toDomain()
        assertThat(domain.id).isEqualTo(-1L)
        assertThat(domain.isEnd).isTrue()
    }

    @Test
    fun `paragraph without id is dropped`() {
        val domain = page(
            paragraphs = listOf(Esse3Paragraphs(paragraphId = null, applications = emptyList())),
        ).toDomain()
        assertThat(domain.paragraphs).isEmpty()
    }

    @Test
    fun `blank paragraph title becomes null`() {
        val domain = page(
            paragraphs = listOf(
                Esse3Paragraphs(paragraphId = 3, elementsDescription = "  ", applications = emptyList()),
            ),
        ).toDomain()
        assertThat(domain.paragraphs.single().title).isNull()
    }

    private fun singleQuestion(application: Esse3Applications) =
        page(paragraphs = listOf(Esse3Paragraphs(paragraphId = 1, applications = listOf(application))))
            .toDomain().paragraphs.single().questions.single()

    @Test
    fun `question decodes prompt mandatory and options`() {
        val question = singleQuestion(application(applicationId = 10, mandatoryFlag = 1))
        assertThat(question.id).isEqualTo(10L)
        assertThat(question.text).isEqualTo("Domanda?")
        assertThat(question.mandatory).isTrue()
        assertThat(question.options).hasSize(2)
    }

    @Test
    fun `question without id is dropped`() {
        val domain = page(
            paragraphs = listOf(
                Esse3Paragraphs(paragraphId = 1, applications = listOf(application(applicationId = null))),
            ),
        ).toDomain()
        assertThat(domain.paragraphs.single().questions).isEmpty()
    }

    @Test
    fun `answer without id is dropped from options`() {
        val question = singleQuestion(
            application(availableAnswers = listOf(answer(answerId = null), answer(answerId = 2))),
        )
        assertThat(question.options).hasSize(1)
        assertThat(question.options.single().id).isEqualTo(2L)
    }

    @Test
    fun `option flags free text requirement on matching format`() {
        val question = singleQuestion(
            application(
                availableAnswers = listOf(
                    answer(answerId = 1, answerFormatCode = "TL_RSP_ALF"),
                    answer(answerId = 2, answerFormatCode = "TL_RSP_NUM"),
                ),
            ),
        )
        assertThat(question.options.first { it.id == 1L }.requiresFreeText).isTrue()
        assertThat(question.options.first { it.id == 2L }.requiresFreeText).isFalse()
    }

    @Test
    fun `format codes decode to question kinds`() {
        assertThat(singleQuestion(application(formatTypeCode = "TL_DOM_DFS")).kind)
            .isEqualTo(QuestionnaireQuestionKind.SingleChoice)
        assertThat(singleQuestion(application(formatTypeCode = "TL_DOM_OFS")).kind)
            .isEqualTo(QuestionnaireQuestionKind.Scale)
        assertThat(singleQuestion(application(formatTypeCode = "TL_DOM_LIB")).kind)
            .isEqualTo(QuestionnaireQuestionKind.FreeText)
        assertThat(singleQuestion(application(formatTypeCode = "TL_DOM_DFM", maxChoiceNumber = 3)).kind)
            .isEqualTo(QuestionnaireQuestionKind.MultiChoice(3))
    }

    @Test
    fun `unknown format degrades by option count`() {
        val multi = singleQuestion(
            application(formatTypeCode = "??", availableAnswers = listOf(answer(1), answer(2))),
        )
        assertThat(multi.kind).isEqualTo(QuestionnaireQuestionKind.SingleChoice)

        val lone = singleQuestion(
            application(formatTypeCode = "??", availableAnswers = listOf(answer(1))),
        )
        assertThat(lone.kind).isEqualTo(QuestionnaireQuestionKind.FreeText)
    }

    @Test
    fun `saved option ids come from complete answers`() {
        val question = singleQuestion(
            application(
                completeAnswers = listOf(
                    Esse3CompiledAnswers(questionId = 1),
                    Esse3CompiledAnswers(questionId = 2),
                    Esse3CompiledAnswers(questionId = null),
                ),
            ),
        )
        assertThat(question.savedOptionIds).containsExactly(1L, 2L)
    }

    @Test
    fun `saved free text is the first non-blank complete answer`() {
        val question = singleQuestion(
            application(
                completeAnswers = listOf(
                    Esse3CompiledAnswers(freeText = "   "),
                    Esse3CompiledAnswers(freeText = "ottimo corso"),
                ),
            ),
        )
        assertThat(question.savedFreeText).isEqualTo("ottimo corso")
    }

    @Test
    fun `question note trimmed and blanked`() {
        assertThat(singleQuestion(application(elementsNote = "  nota  ")).note).isEqualTo("nota")
        assertThat(singleQuestion(application(elementsNote = "  ")).note).isNull()
    }

    @Test
    fun `summary complete flag decodes from string one`() {
        assertThat(Esse3QuestionnaireSummary(completeFlag = "1").toDomain().complete).isTrue()
        assertThat(Esse3QuestionnaireSummary(completeFlag = "0").toDomain().complete).isFalse()
        assertThat(Esse3QuestionnaireSummary(completeFlag = null).toDomain().complete).isFalse()
    }
}
