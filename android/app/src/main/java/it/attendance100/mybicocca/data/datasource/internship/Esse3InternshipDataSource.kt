package it.attendance100.mybicocca.data.datasource.internship

import it.attendance100.mybicocca.data.api.esse3.Esse3Api
import it.attendance100.mybicocca.data.model.internship.InternshipApplication
import it.attendance100.mybicocca.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Esse3InternshipDataSource @Inject constructor(
    private val esse3Api: Esse3Api,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun getInternshipApplications(studentId: Long): List<InternshipApplication> = withContext(ioDispatcher) {
        val headers = esse3Api.internships.getStudentInternshipApplicationHeaders(studentId)
        headers.mapNotNull { header ->
            val id = header.domicileInternshipId ?: return@mapNotNull null
            InternshipApplication(
                id = id,
                studentId = studentId,
                companyName = header.entityDescription,
                status = header.internshipApplicationStateDescription,
                title = header.opportunityTitle,
                startDate = null,
                endDate = null,
            )
        }
    }
}
