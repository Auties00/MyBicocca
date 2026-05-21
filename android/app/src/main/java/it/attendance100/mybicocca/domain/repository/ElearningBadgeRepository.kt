package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.badge.Badge
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import kotlinx.coroutines.flow.Flow

interface ElearningBadgeRepository {
    fun observeBadges(accountId: AccountId, courseId: CourseId? = null): Flow<Loadable<List<Badge>>>
    suspend fun refreshBadges(accountId: AccountId, courseId: CourseId? = null, force: Boolean = false)
    suspend fun clearForAccount(accountId: AccountId)
}
