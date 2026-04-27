package it.attendance100.mybicocca.data.datasource.transcript

import it.attendance100.mybicocca.data.api.esse3.Esse3Api
import it.attendance100.mybicocca.data.dto.esse3.Esse3AverageTypeCode
import it.attendance100.mybicocca.data.dto.esse3.Esse3RegulationStatusCode
import it.attendance100.mybicocca.data.dto.esse3.Esse3State
import it.attendance100.mybicocca.data.dto.esse3.Esse3TranscriptRow
import it.attendance100.mybicocca.data.dto.esse3.Esse3TranscriptStats
import it.attendance100.mybicocca.data.model.transcript.RecordBookRow
import it.attendance100.mybicocca.data.model.transcript.RecordBookStats
import it.attendance100.mybicocca.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Esse3TranscriptDataSource @Inject constructor(
    private val esse3Api: Esse3Api,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun getRecordBookRows(matId: Long): List<RecordBookRow> = withContext(ioDispatcher) {
        esse3Api.transcript.getRecordBookRows(matId)
            .map { it.toRecordBookRow(matId) }
    }

    suspend fun getRecordBookStats(matId: Long): RecordBookStats = withContext(ioDispatcher) {
        esse3Api.transcript.getRecordBookStats(matId).toRecordBookStats(matId)
    }

    private fun Esse3TranscriptRow.toRecordBookRow(careerId: Long) = RecordBookRow(
        id = activityChoiceId,
        careerId = careerId,
        activityName = activityDescription,
        activityCode = activityCode,
        credits = weight,
        grade = outcome?.grade?.toInt(),
        cumLaude = outcome?.cumLaudeFlag == 1,
        date = outcome?.graduationDate,
        status = state.toRecordBookStatus()
    )

    private fun Esse3State.toRecordBookStatus() = when(this) {
        Esse3State.Frequented -> RecordBookRow.Status.FREQUENTED
        Esse3State.Passed -> RecordBookRow.Status.PASSED
        Esse3State.Planned -> RecordBookRow.Status.PLANNED
        is Esse3State.Unknown -> RecordBookRow.Status.UNKNOWN
    }

    private fun Esse3TranscriptStats.toRecordBookStats(careerId: Long) = RecordBookStats(
        careerId = careerId,
        passedCredits = passedMeasurementUnitWeight ?: 0f,
        totalCredits = maxMeasurementUnitWeight ?: 0f,
        weightedAverage = averages.firstOrNull { it.averageTypeCode == Esse3AverageTypeCode.Weighted }?.average,
        arithmeticAverage = averages.firstOrNull { it.averageTypeCode == Esse3AverageTypeCode.Arithmetic }?.average,
        passedExamCount = passedTeachingActivityNumber ?: 0,
    )
}
