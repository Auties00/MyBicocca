package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.badge.Badge
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import kotlinx.coroutines.flow.Flow

/**
 * Access to the badges Moodle has awarded the student. The local cache is the single
 * source of truth: observing streams hot from it; refreshing hits the user-badges web
 * service, writes back to the cache, skips the network while the data is within its
 * staleness window unless `force` is set, and throws on failure for the ViewModel to
 * translate into a sync status.
 */
interface ElearningBadgeRepository {
    /** Streams the account's badges; scoped to one awarding course when given. */
    fun observeBadges(accountId: AccountId, courseId: CourseId? = null): Flow<Loadable<List<Badge>>>

    /** Re-fetches the account's badges from Moodle, optionally for one course. */
    suspend fun refreshBadges(accountId: AccountId, courseId: CourseId? = null, force: Boolean = false)

    /** Drops every cached badge of the account, e.g. on sign-out. */
    suspend fun clearForAccount(accountId: AccountId)
}
