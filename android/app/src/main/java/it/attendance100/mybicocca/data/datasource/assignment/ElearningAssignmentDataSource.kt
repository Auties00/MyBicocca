package it.attendance100.mybicocca.data.datasource.assignment

import it.attendance100.mybicocca.data.api.elearning.ElearningApi
import it.attendance100.mybicocca.data.datastore.AuthTokenStore
import it.attendance100.mybicocca.data.model.assignment.Assignment
import it.attendance100.mybicocca.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ElearningAssignmentDataSource @Inject constructor(
    private val elearningApi: ElearningApi,
    private val authTokenStore: AuthTokenStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun getAssignments(courseIds: List<Int>): List<Assignment> = withContext(ioDispatcher) {
        val wsToken = authTokenStore.elearningWsToken ?: return@withContext emptyList()
        if (courseIds.isEmpty()) return@withContext emptyList()

        elearningApi.assignments.getAssignments(wsToken, courseIds).courses.flatMap { courseAssignments ->
            courseAssignments.assignments.map { dto ->
                Assignment(
                    id = dto.id,
                    courseId = dto.courseId,
                    name = dto.name,
                    description = dto.introduction,
                    dueDate = dto.dueDateTimestamp,
                    cutOffDate = dto.cutOffDateTimestamp,
                    maxGrade = dto.maximumGrade?.toFloat(),
                )
            }
        }
    }
}
