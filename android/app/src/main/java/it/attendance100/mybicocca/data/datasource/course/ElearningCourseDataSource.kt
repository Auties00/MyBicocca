package it.attendance100.mybicocca.data.datasource.course

import it.attendance100.mybicocca.data.api.elearning.ElearningApi
import it.attendance100.mybicocca.data.datastore.AuthTokenStore
import it.attendance100.mybicocca.data.model.course.Course
import it.attendance100.mybicocca.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ElearningCourseDataSource @Inject constructor(
    private val elearningApi: ElearningApi,
    private val authTokenStore: AuthTokenStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun getCourses(): List<Course> = withContext(ioDispatcher) {
        val wsToken = authTokenStore.elearningWsToken ?: return@withContext emptyList()
        val userId = authTokenStore.elearningUserId
        if (userId == -1) return@withContext emptyList()

        elearningApi.courses.getUserCourses(wsToken, userId).courses.map { dto ->
            Course(
                id = dto.id,
                shortName = dto.shortName,
                fullName = dto.fullName,
                progress = dto.progress?.toFloat(),
                isFavourite = dto.isFavourite ?: false,
                imageUrl = dto.courseImageUrl,
                startDate = dto.startDateTimestamp,
                endDate = dto.endDateTimestamp,
            )
        }
    }
}
