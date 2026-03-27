package it.attendance100.mybicocca.data.datasource.grade

import it.attendance100.mybicocca.data.api.elearning.ElearningApi
import it.attendance100.mybicocca.data.datastore.AuthTokenStore
import it.attendance100.mybicocca.data.model.course.CourseGrade
import it.attendance100.mybicocca.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ElearningGradeDataSource @Inject constructor(
    private val elearningApi: ElearningApi,
    private val authTokenStore: AuthTokenStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun getCourseGrades(): List<CourseGrade> = withContext(ioDispatcher) {
        val wsToken = authTokenStore.elearningWsToken ?: return@withContext emptyList()
        val userId = authTokenStore.elearningUserId
        if (userId == -1) return@withContext emptyList()

        elearningApi.grades.getCourseGrades(wsToken, userId = userId).grades.map { dto ->
            CourseGrade(
                courseId = dto.courseId,
                courseName = "", // Will be enriched by the repository using course data
                grade = dto.grade,
                rawGrade = dto.rawGrade,
                rank = dto.rank,
            )
        }
    }
}
