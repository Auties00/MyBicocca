package it.attendance100.mybicocca.domain.model.questionnaire

/**
 * Everything needed to start one questionnaire compilation: the activity, the
 * server-side questionnaire config and the unit tags. Built from an activity's
 * questionnaires plus the teaching unit the student picked; plain values so it can
 * travel through navigation args to the compilation sub-screen.
 *
 * @property activityChoiceId Esse3 `adsceId` of the activity being evaluated.
 * @property questionnaireId Id of the questionnaire template to compile.
 * @property questionnaireConfigId Id of the questionnaire configuration to compile.
 * @property tags Opaque Esse3 `tagsValdid` token of the chosen unit/lecturer/partition.
 */
data class QuestionnaireTarget(
    val activityChoiceId: Long,
    val questionnaireId: Int,
    val questionnaireConfigId: Int,
    val tags: String,
)
