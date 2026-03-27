package it.attendance100.mybicocca.data.datasource.forum

import it.attendance100.mybicocca.data.api.elearning.ElearningApi
import it.attendance100.mybicocca.data.datastore.AuthTokenStore
import it.attendance100.mybicocca.data.model.forum.Forum
import it.attendance100.mybicocca.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ElearningForumDataSource @Inject constructor(
    private val elearningApi: ElearningApi,
    private val authTokenStore: AuthTokenStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun getForums(courseIds: List<Int>): List<Forum> = withContext(ioDispatcher) {
        val wsToken = authTokenStore.elearningWsToken ?: return@withContext emptyList()
        if (courseIds.isEmpty()) return@withContext emptyList()

        elearningApi.forums.getForums(wsToken, courseIds).forums.map { dto ->
            Forum(
                id = dto.id,
                courseId = dto.courseId,
                name = dto.name,
                description = dto.introduction,
                discussionCount = dto.numberOfDiscussions ?: 0,
                type = dto.type,
            )
        }
    }
}
