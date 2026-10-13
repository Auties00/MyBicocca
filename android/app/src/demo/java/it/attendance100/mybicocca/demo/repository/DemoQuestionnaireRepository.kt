package it.attendance100.mybicocca.demo.repository

import it.attendance100.mybicocca.demo.DemoCareer
import it.attendance100.mybicocca.demo.DemoCourse
import it.attendance100.mybicocca.demo.DemoCourses
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.questionnaire.ActivityQuestionnaires
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivity
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivityStatus
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireAnswer
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireCompilationStart
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireOption
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnairePage
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireParagraph
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireQuestion
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireQuestionKind
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireSession
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireSummary
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireTarget
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireUnit
import it.attendance100.mybicocca.domain.repository.QuestionnaireRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Teaching-evaluation questionnaires for the courses being attended: two already filled in, the
 * rest open. A compilation is two pages of questions; confirming it marks the course completed.
 */
@Singleton
class DemoQuestionnaireRepository @Inject constructor() : QuestionnaireRepository {

    private val completed = mutableSetOf(
        DemoCareer.activityId(DemoCourses.statistics),
        DemoCareer.activityId(DemoCourses.algorithms),
    )
    private val answers = mutableMapOf<Long, List<QuestionnaireAnswer>>()

    override suspend fun getActivities(careerId: CareerId): List<QuestionnaireActivity> = DemoCourses.current.map { course ->
        val id = DemoCareer.activityId(course)
        QuestionnaireActivity(
            activityChoiceId = id,
            activityCode = course.code,
            activityName = course.name,
            credits = course.cfu.toFloat(),
            courseYear = course.year,
            attendanceYear = 2025,
            status = if (id in completed) QuestionnaireActivityStatus.Completed else QuestionnaireActivityStatus.ToCompile,
        )
    }

    override suspend fun getActivityQuestionnaires(careerId: CareerId, activityChoiceId: Long): ActivityQuestionnaires {
        val course = courseOf(activityChoiceId)
        return ActivityQuestionnaires(
            activityChoiceId = activityChoiceId,
            questionnaireId = QUESTIONNAIRE_ID,
            questionnaireConfigId = CONFIG_ID,
            questionnaireName = "Valutazione della didattica 2025/26",
            anonymous = true,
            units = listOf(
                QuestionnaireUnit(
                    teachingUnitName = course.name,
                    lecturerName = course.teacher,
                    partitionName = null,
                    completed = activityChoiceId in completed,
                    tags = "UD-$activityChoiceId",
                ),
            ),
        )
    }

    override suspend fun startCompilation(careerId: CareerId, target: QuestionnaireTarget): QuestionnaireCompilationStart {
        answers.clear()
        return QuestionnaireCompilationStart(
            session = QuestionnaireSession(
                careerId = careerId,
                activityChoiceId = target.activityChoiceId,
                questionnaireId = target.questionnaireId,
                questionnaireConfigId = target.questionnaireConfigId,
                compilationId = 940000 + target.activityChoiceId,
                userCompilationId = 950000 + target.activityChoiceId,
            ),
            firstPage = page(FIRST_PAGE),
        )
    }

    override suspend fun savePageAnswers(session: QuestionnaireSession, pageId: Long, answers: List<QuestionnaireAnswer>) {
        this.answers[pageId] = answers
    }

    override suspend fun getNextPage(session: QuestionnaireSession, pageId: Long): QuestionnairePage =
        page(if (pageId == FIRST_PAGE) SECOND_PAGE else END_PAGE)

    override suspend fun getPreviousPage(session: QuestionnaireSession, pageId: Long): QuestionnairePage =
        page(if (pageId == END_PAGE) SECOND_PAGE else FIRST_PAGE)

    override suspend fun getSummary(session: QuestionnaireSession): QuestionnaireSummary = QuestionnaireSummary(complete = true)

    override suspend fun confirm(session: QuestionnaireSession) {
        completed += session.activityChoiceId
    }

    private fun courseOf(activityChoiceId: Long): DemoCourse =
        DemoCourses.current.first { DemoCareer.activityId(it) == activityChoiceId }

    private fun page(id: Long): QuestionnairePage = when (id) {
        FIRST_PAGE -> QuestionnairePage(
            id = id,
            isEnd = false,
            paragraphs = listOf(
                QuestionnaireParagraph(
                    id = 1,
                    title = "Insegnamento",
                    questions = listOf(
                        scale(id, 101, "Le conoscenze preliminari possedute sono risultate sufficienti per la comprensione degli argomenti?"),
                        scale(id, 102, "Il carico di studio dell'insegnamento è proporzionato ai crediti assegnati?"),
                        scale(id, 103, "Il materiale didattico è adeguato per lo studio della materia?"),
                    ),
                ),
            ),
        )
        SECOND_PAGE -> QuestionnairePage(
            id = id,
            isEnd = false,
            paragraphs = listOf(
                QuestionnaireParagraph(
                    id = 2,
                    title = "Docenza",
                    questions = listOf(
                        scale(id, 201, "Il docente espone gli argomenti in modo chiaro?"),
                        scale(id, 202, "Il docente è reperibile per chiarimenti e spiegazioni?"),
                        QuestionnaireQuestion(
                            id = 203,
                            text = "Suggerimenti",
                            note = "Facoltativo",
                            mandatory = false,
                            kind = QuestionnaireQuestionKind.FreeText,
                            options = listOf(QuestionnaireOption(2030, "", requiresFreeText = true)),
                            savedOptionIds = emptySet(),
                            savedFreeText = answers[id]?.firstOrNull { it.questionId == 203L }?.freeText,
                        ),
                    ),
                ),
            ),
        )
        else -> QuestionnairePage(id = END_PAGE, isEnd = true, paragraphs = emptyList())
    }

    private fun scale(pageId: Long, id: Long, text: String) = QuestionnaireQuestion(
        id = id,
        text = text,
        note = null,
        mandatory = true,
        kind = QuestionnaireQuestionKind.Scale,
        options = listOf("Decisamente no", "Più no che sì", "Più sì che no", "Decisamente sì").mapIndexed { index, label ->
            QuestionnaireOption(id * 10 + index, label, requiresFreeText = false)
        },
        savedOptionIds = answers[pageId].orEmpty().filter { it.questionId == id }.map { it.optionId }.toSet(),
        savedFreeText = null,
    )

    private companion object {
        const val QUESTIONNAIRE_ID = 7001
        const val CONFIG_ID = 7101
        const val FIRST_PAGE = 1L
        const val SECOND_PAGE = 2L
        const val END_PAGE = 3L
    }
}
