package it.attendance100.mybicocca.data.datasource.evaluation

import it.attendance100.mybicocca.data.api.esse3.Esse3Api
import it.attendance100.mybicocca.data.model.evaluation.EvaluationEntry
import it.attendance100.mybicocca.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Esse3EvaluationDataSource @Inject constructor(
    private val esse3Api: Esse3Api,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun getEvaluationEntries(matId: Long): List<EvaluationEntry> = withContext(ioDispatcher) {
        val rows = esse3Api.questionnaires.getRecordBookQuestionnaires(matId)
        rows.map { row ->
            EvaluationEntry(
                id = row.activityChoiceId,
                activityName = row.activityDescription,
                activityCode = row.activityCode,
                questionnaireId = null,
                isCompiled = false, // determined by checking questionnaire compilation status separately
                academicYear = row.academicYearAttendanceId,
            )
        }
    }
}
