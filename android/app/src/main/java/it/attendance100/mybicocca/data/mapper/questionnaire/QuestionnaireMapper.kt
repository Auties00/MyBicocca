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

/**
 * Maps a libretto row annotated with questionnaire status to the domain activity,
 * decoding `linkState` (1 completed, 2 partially completed, 3 to compile, 4
 * configuration error). Returns null for `linkState` 0 or absent — no questionnaires
 * attached, nothing to show.
 */
fun Esse3TranscriptRowWithQuestionnaireStatus.toQuestionnaireActivity(): QuestionnaireActivity? {
    val status = when (linkState) {
        1 -> QuestionnaireActivityStatus.Completed
        2 -> QuestionnaireActivityStatus.PartiallyCompleted
        3 -> QuestionnaireActivityStatus.ToCompile
        4 -> QuestionnaireActivityStatus.ConfigurationError
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

/**
 * Maps the teaching-unit evaluation response to the domain activity questionnaires.
 * Returns null when Esse3 omits the activity id, which makes the payload unusable.
 */
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

/**
 * Maps one teaching-unit row to a compilable questionnaire unit. Rows without the
 * `tagsValdid` token resolve to null — that token is what the start-compilation call
 * needs to target the unit, so a unit is useless without it. The partition label is
 * hidden for code "N0", Esse3's marker for "no partitioning" rather than a real turno.
 */
private fun Esse3TeachingUnitLogStudyPlanWebList.toQuestionnaireUnit(): QuestionnaireUnit? {
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
        partitionName = domicilePartialDescription
            ?.takeIf { it.isNotBlank() && domicilePartialCode != "N0" },
        completed = linkState == 1,
        tags = tags,
    )
}

/**
 * Maps a server questionnaire page to the domain page. A missing page id marks the
 * server's end-of-questionnaire response, encoded as the negative end-page sentinel so
 * `isEnd` holds for it.
 */
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

/**
 * Maps a server question (`applicazione`) to the domain question, decoding the format
 * code into a question kind: "TL_DOM_DFS" single choice, "TL_DOM_OFS" scale,
 * "TL_DOM_DFM" multi choice, "TL_DOM_LIB" free text. Unknown formats degrade to
 * something answerable rather than disappearing. Saved option ids come from the
 * compiled-answers list, where `quesitoId` is the id of the CHOSEN answer option.
 */
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
            else -> if (options.size > 1) {
                QuestionnaireQuestionKind.SingleChoice
            } else {
                QuestionnaireQuestionKind.FreeText
            }
        },
        options = options,
        savedOptionIds = completeAnswers.mapNotNull { it.questionId?.toLong() }.toSet(),
        savedFreeText = completeAnswers
            .firstNotNullOfOrNull { it.freeText?.takeIf { text -> text.isNotBlank() } },
    )
}

/** Decodes the summary's string-typed `completeFlag` ("1" = complete). */
fun Esse3QuestionnaireSummary.toDomain() = QuestionnaireSummary(
    complete = completeFlag == "1",
)

/** Esse3 stores names in all caps ("VINCENZINA MESSINA"); rendered as title case. */
private fun String.toDisplayCase(): String = trim()
    .split(Regex("\\s+"))
    .joinToString(" ") { word ->
        word.lowercase(Locale.ITALIAN).replaceFirstChar { it.titlecase(Locale.ITALIAN) }
    }

private const val END_PAGE_ID = -1L
private const val ANSWER_FORMAT_FREE_TEXT = "TL_RSP_ALF"
