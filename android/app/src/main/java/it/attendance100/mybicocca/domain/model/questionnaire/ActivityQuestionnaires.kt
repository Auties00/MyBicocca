package it.attendance100.mybicocca.domain.model.questionnaire

/**
 * The VAL_DID course-evaluation questionnaires of a single libretto activity, one per
 * teaching unit / partition. Sourced live from Esse3's teaching-unit evaluation endpoint
 * and rendered by the questionnaires sub-screen in the registry tab.
 *
 * @property activityChoiceId Esse3 `adsceId` of the libretto row the questionnaires
 *   evaluate.
 * @property questionnaireId Id of the questionnaire template from the server config;
 *   null when the evaluation window is closed, in which case units cannot be compiled.
 * @property questionnaireConfigId Id of the questionnaire configuration; null together
 *   with [questionnaireId] when the window is closed.
 * @property questionnaireName Title of the questionnaire template.
 * @property anonymous True when Esse3 marks the compilation as anonymous.
 * @property units The compilable teaching-unit targets.
 */
data class ActivityQuestionnaires(
    val activityChoiceId: Long,
    val questionnaireId: Int?,
    val questionnaireConfigId: Int?,
    val questionnaireName: String?,
    val anonymous: Boolean,
    val units: List<QuestionnaireUnit>,
)

/**
 * One compilable questionnaire target: a teaching unit taught by a lecturer in a
 * specific partition (turno).
 *
 * @property teachingUnitName Name of the teaching unit being evaluated.
 * @property lecturerName Lecturer the evaluation refers to, title-cased for display.
 * @property partitionName Partition (turno) label; null when the unit has no real
 *   partitioning.
 * @property completed True when the student already compiled this unit's questionnaire.
 * @property tags Opaque Esse3 `tagsValdid` token that identifies this exact
 *   unit/lecturer/partition combination when starting a compilation.
 */
data class QuestionnaireUnit(
    val teachingUnitName: String,
    val lecturerName: String?,
    val partitionName: String?,
    val completed: Boolean,
    val tags: String,
)
