package it.attendance100.mybicocca.data.datasource.career

import it.attendance100.mybicocca.data.api.esse3.Esse3Api
import it.attendance100.mybicocca.data.datastore.AuthTokenStore
import it.attendance100.mybicocca.data.dto.esse3.Esse3Career
import it.attendance100.mybicocca.data.model.career.Career
import it.attendance100.mybicocca.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Esse3CareerDataSource @Inject constructor(
    private val esse3Api: Esse3Api,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun getCareers(): List<Career> = withContext(ioDispatcher) {
        val careers = esse3Api.careers.getCareers()
        careers.mapNotNull { it.toCareer() }
    }

    private fun Esse3Career.toCareer(): Career? {
        val stuId = studentId ?: return null
        return Career(
            studentId = stuId,
            courseOfStudyDescription = p06CourseOfStudyDescription ?: "",
            courseOfStudyCode = p06CourseOfStudyCode,
            enrollmentYear = academicYearId,
            statusDescription = studentStatesDescription,
            statusCode = studentStatusCode,
            matricola = matricola,
            matricolaId = matId
        )
    }
}
