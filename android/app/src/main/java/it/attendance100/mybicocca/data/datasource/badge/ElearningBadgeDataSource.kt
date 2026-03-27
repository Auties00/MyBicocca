package it.attendance100.mybicocca.data.datasource.badge

import it.attendance100.mybicocca.data.api.elearning.ElearningApi
import it.attendance100.mybicocca.data.datastore.AuthTokenStore
import it.attendance100.mybicocca.data.model.badge.Badge
import it.attendance100.mybicocca.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ElearningBadgeDataSource @Inject constructor(
    private val elearningApi: ElearningApi,
    private val authTokenStore: AuthTokenStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun getBadges(): List<Badge> = withContext(ioDispatcher) {
        val wsToken = authTokenStore.elearningWsToken ?: return@withContext emptyList()
        val userId = authTokenStore.elearningUserId
        if (userId == -1) return@withContext emptyList()

        elearningApi.badges.getUserBadges(wsToken, userId = userId).badges.map { dto ->
            Badge(
                id = dto.id,
                name = dto.name,
                description = dto.description,
                imageUrl = dto.badgeUrl,
                issuedDate = dto.dateIssued,
                expireDate = dto.dateExpire,
                courseId = dto.courseId,
            )
        }
    }
}
