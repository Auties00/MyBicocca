package it.attendance100.mybicocca.data.mapper.questionnaire

import it.attendance100.mybicocca.data.local.questionnaire.ActivityQuestionnairesEntity
import it.attendance100.mybicocca.data.local.questionnaire.QuestionnaireActivityEntity
import it.attendance100.mybicocca.data.local.questionnaire.QuestionnaireUnitEntity
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.questionnaire.ActivityQuestionnaires
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivity
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivityStatus
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireUnit

// Entity <-> domain mapping for the offline questionnaire-list mirrors. The aggregate status
// enum round-trips by name, falling back to ConfigurationError on an unknown value. These
// receivers coexist with the DTO mappers in this package (different receiver types).

fun QuestionnaireActivity.toEntity(careerId: CareerId, order: Int): QuestionnaireActivityEntity =
    QuestionnaireActivityEntity(
        careerId = careerId.value,
        activityChoiceId = activityChoiceId,
        cacheOrder = order,
        activityCode = activityCode,
        activityName = activityName,
        credits = credits,
        courseYear = courseYear,
        attendanceYear = attendanceYear,
        status = status.name,
    )

fun QuestionnaireActivityEntity.toDomain(): QuestionnaireActivity = QuestionnaireActivity(
    activityChoiceId = activityChoiceId,
    activityCode = activityCode,
    activityName = activityName,
    credits = credits,
    courseYear = courseYear,
    attendanceYear = attendanceYear,
    status = runCatching { QuestionnaireActivityStatus.valueOf(status) }
        .getOrDefault(QuestionnaireActivityStatus.ConfigurationError),
)

fun ActivityQuestionnaires.toEntity(careerId: CareerId): ActivityQuestionnairesEntity =
    ActivityQuestionnairesEntity(
        careerId = careerId.value,
        activityChoiceId = activityChoiceId,
        questionnaireId = questionnaireId,
        questionnaireConfigId = questionnaireConfigId,
        questionnaireName = questionnaireName,
        anonymous = anonymous,
    )

fun QuestionnaireUnit.toEntity(careerId: CareerId, activityChoiceId: Long, order: Int): QuestionnaireUnitEntity =
    QuestionnaireUnitEntity(
        careerId = careerId.value,
        activityChoiceId = activityChoiceId,
        unitOrder = order,
        teachingUnitName = teachingUnitName,
        lecturerName = lecturerName,
        partitionName = partitionName,
        completed = completed,
        tags = tags,
    )

fun QuestionnaireUnitEntity.toDomain(): QuestionnaireUnit = QuestionnaireUnit(
    teachingUnitName = teachingUnitName,
    lecturerName = lecturerName,
    partitionName = partitionName,
    completed = completed,
    tags = tags,
)

fun ActivityQuestionnairesEntity.toDomain(units: List<QuestionnaireUnit>): ActivityQuestionnaires =
    ActivityQuestionnaires(
        activityChoiceId = activityChoiceId,
        questionnaireId = questionnaireId,
        questionnaireConfigId = questionnaireConfigId,
        questionnaireName = questionnaireName,
        anonymous = anonymous,
        units = units,
    )
