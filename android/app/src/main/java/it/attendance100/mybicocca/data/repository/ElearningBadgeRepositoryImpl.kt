package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.time.StalePolicy
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.elearning.badge.BadgeDao
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncScope
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncStateDao
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncStateEntity
import it.attendance100.mybicocca.data.mapper.elearning.toDomain
import it.attendance100.mybicocca.data.mapper.elearning.toEntity
import it.attendance100.mybicocca.di.ApplicationScope
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.badge.Badge
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.repository.ElearningBadgeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ElearningBadgeRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val badgeDao: BadgeDao,
    private val syncStateDao: ElearningSyncStateDao,
    private val stalePolicy: StalePolicy,
    @Suppress("unused") @ApplicationScope private val scope: CoroutineScope,
) : ElearningBadgeRepository {

    override fun observeBadges(accountId: AccountId, courseId: CourseId?): Flow<Loadable<List<Badge>>> {
        val source = if (courseId == null) badgeDao.observeAll(accountId.value)
        else badgeDao.observeForCourse(accountId.value, courseId.value)
        return source
            .map { rows -> Loadable.Loaded(rows.map { it.toDomain() }) as Loadable<List<Badge>> }
            .flowOn(Dispatchers.Default)
    }

    override suspend fun refreshBadges(accountId: AccountId, courseId: CourseId?, force: Boolean) {
        val scopeId = (courseId?.value ?: 0).toLong()
        if (!force && !isStale(accountId, ElearningSyncScope.BADGES, scopeId)) return
        val account = sessionManager.activeAccount.value ?: error("No active account.")
        val (api, token) = sessionManager.elearning()
        val response = api.badges.getUserBadges(
            wsToken = token,
            userId = account.learning.lmsUserId,
            courseId = courseId?.value,
            page = 0,
            perPage = 0,
            search = null,
            onlyPublic = false,
        )
        val rows = response.badges.map { it.toEntity(accountId) }
        badgeDao.replaceForCourse(accountId.value, courseId?.value, rows)
        stamp(accountId, ElearningSyncScope.BADGES, scopeId)
    }

    override suspend fun clearForAccount(accountId: AccountId) {
        badgeDao.deleteForAccount(accountId.value)
    }

    private suspend fun isStale(accountId: AccountId, scope: String, scopeId: Long): Boolean {
        val state = syncStateDao.getState(accountId.value, scope, scopeId) ?: return true
        return kotlin.time.Clock.System.now().toEpochMilliseconds() - state.lastRefreshedAtMs > stalePolicy.ttlFor(scope)
    }

    private suspend fun stamp(accountId: AccountId, scope: String, scopeId: Long) {
        syncStateDao.upsertState(
            ElearningSyncStateEntity(
                accountId = accountId.value,
                scope = scope,
                scopeId = scopeId,
                lastRefreshedAtMs = kotlin.time.Clock.System.now().toEpochMilliseconds(),
            )
        )
    }
}
