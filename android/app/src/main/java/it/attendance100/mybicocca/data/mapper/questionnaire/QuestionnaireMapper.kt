package it.attendance100.mybicocca.data.mapper.questionnaire

import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Applications
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3QuestionnairePage
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3QuestionnaireSummary
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TeachingUnitLogStudyPlanWebList
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TeachingUnitWithQuestionnaire
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TranscriptRowWithQuestionnaireStatus
import it.attendance100.mybicocca.domain.model.questionnaire.ActivityQuestionnaires
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivity
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivityStatus
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireOption
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnairePage
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireParagraph
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireQuestion
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireQuestionKind
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireSummary
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireUnit
import java.util.Locale

fun Esse3TranscriptRowWithQuestionnaireStatus.toQuestionnaireActivity(): QuestionnaireActivity? {
    val status = when (linkState) {
        1 -> QuestionnaireActivityStatus.Completed
        2 -> QuestionnaireActivityStatus.PartiallyCompleted
        3 -> QuestionnaireActivityStatus.ToCompile
        4 -> QuestionnaireActivityStatus.ConfigurationError
        // 0 / null = no questionnaires attached, nothing to show.
        else -> return null
    }
    return QuestionnaireActivity(
        activityChoiceId = activityChoiceId,
        activityCode = activityCode,
        activityName = activityDescription,
        credits = weight,
        courseYear = courseYear,
        attendanceYear = academicYearAttendanceId,
        status = status,
    )
}

fun Esse3TeachingUnitWithQuestionnaire.toActivityQuestionnaires(): ActivityQuestionnaires? {
    val activityChoiceId = activityChoiceId ?: return null
    return ActivityQuestionnaires(
        activityChoiceId = activityChoiceId,
        questionnaireId = questionnaireId,
        questionnaireConfigId = questionConfigId,
        questionnaireName = questionnaireDescription?.takeIf { it.isNotBlank() },
        anonymous = anonymousFlag == 1,
        units = teachingUnitLogWebStudyPlanList.mapNotNull { it.toQuestionnaireUnit() },
    )
}

private fun Esse3TeachingUnitLogStudyPlanWebList.toQuestionnaireUnit(): QuestionnaireUnit? {
    // The tags token is what setNewSurvey needs to target this unit — useless without it.
    val tags = tagsValidationDid?.takeIf { it.isNotBlank() } ?: return null
    val unitName = teachingUnitDescription?.takeIf { it.isNotBlank() }
        ?: activityDescription?.takeIf { it.isNotBlank() }
        ?: return null
    val lecturer = listOfNotNull(lecturersName, lecturersSurname)
        .filter { it.isNotBlank() }
        .joinToString(" ") { it.toDisplayCase() }
        .takeIf { it.isNotBlank() }
    return QuestionnaireUnit(
        teachingUnitName = unitName,
        lecturerName = lecturer,
        // "N0" is Esse3 for "no partitioning" — not a real turno, hide it.
        partitionName = domicilePartialDescription
            ?.takeIf { it.isNotBlank() && domicilePartialCode != "N0" },
        completed = linkState == 1,
        tags = tags,
    )
}

fun Esse3QuestionnairePage.toDomain(): QuestionnairePage {
    val id = pageId?.toLong() ?: END_PAGE_ID
    return QuestionnairePage(
        id = id,
        isEnd = id < 0,
        paragraphs = paragraphs.mapNotNull { paragraph ->
            val paragraphId = paragraph.paragraphId ?: return@mapNotNull null
            QuestionnaireParagraph(
                id = paragraphId.toLong(),
                title = paragraph.elementsDescription?.trim()?.takeIf { it.isNotEmpty() },
                questions = paragraph.applications.mapNotNull { it.toQuestion() },
            )
        },
    )
}

private fun Esse3Applications.toQuestion(): QuestionnaireQuestion? {
    val id = applicationId ?: return null
    val options = availableAnswers.mapNotNull { answer ->
        val answerId = answer.answerId ?: return@mapNotNull null
        QuestionnaireOption(
            id = answerId.toLong(),
            text = answer.elementsDescription?.trim().orEmpty(),
            requiresFreeText = answer.answerFormatCode == ANSWER_FORMAT_FREE_TEXT,
        )
    }
    return QuestionnaireQuestion(
        id = id.toLong(),
        text = elementsDescription?.trim().orEmpty(),
        note = elementsNote?.trim()?.takeIf { it.isNotEmpty() },
        mandatory = mandatoryFlag == 1,
        kind = when (formatTypeCode) {
            "TL_DOM_DFS" -> QuestionnaireQuestionKind.SingleChoice
            "TL_DOM_OFS" -> QuestionnaireQuestionKind.Scale
            "TL_DOM_DFM" -> QuestionnaireQuestionKind.MultiChoice(maxChoiceNumber)
            "TL_DOM_LIB" -> QuestionnaireQuestionKind.FreeText
            // Unknown formats degrade to something answerable rather than disappearing.
            else -> if (options.size > 1) {
                QuestionnaireQuestionKind.SingleChoice
            } else {
                QuestionnaireQuestionKind.FreeText
            }
        },
        options = options,
        // In compiled answers, quesitoId is the id of the CHOSEN answer option.
        savedOptionIds = completeAnswers.mapNotNull { it.questionId?.toLong() }.toSet(),
        savedFreeText = completeAnswers
            .firstNotNullOfOrNull { it.freeText?.takeIf { text -> text.isNotBlank() } },
    )
}

fun Esse3QuestionnaireSummary.toDomain() = QuestionnaireSummary(
    complete = completeFlag == "1",
)

// Esse3 stores names in all caps ("VINCENZINA MESSINA"); render them as title case.
private fun String.toDisplayCase(): String = trim()
    .split(Regex("\\s+"))
    .joinToString(" ") { word ->
        word.lowercase(Locale.ITALIAN).replaceFirstChar { it.titlecase(Locale.ITALIAN) }
    }

private const val END_PAGE_ID = -1L
private const val ANSWER_FORMAT_FREE_TEXT = "TL_RSP_ALF"
