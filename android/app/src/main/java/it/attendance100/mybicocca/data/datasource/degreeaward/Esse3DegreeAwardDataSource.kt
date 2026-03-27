package it.attendance100.mybicocca.data.datasource.degreeaward

import it.attendance100.mybicocca.data.api.esse3.Esse3Api
import it.attendance100.mybicocca.data.datastore.AuthTokenStore
import it.attendance100.mybicocca.data.model.degreeaward.CommitteeApplication
import it.attendance100.mybicocca.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Esse3DegreeAwardDataSource @Inject constructor(
    private val esse3Api: Esse3Api,
    private val authTokenStore: AuthTokenStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun getCommitteeApplications(studentId: Long): List<CommitteeApplication> = withContext(ioDispatcher) {
        val summaries = esse3Api.degreeAward.getCommitteeApplicationByStudentId(studentId)
        summaries.mapNotNull { summary ->
            val id = summary.domicileCommitteeId ?: return@mapNotNull null
            CommitteeApplication(
                id = id,
                studentId = studentId,
                status = summary.state,
                callDescription = summary.callCommitteeDescription,
                submissionDate = summary.committeeApplicationDate,
            )
        }
    }
}
