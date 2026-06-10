package it.attendance100.mybicocca.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.time.StalePolicy
import it.attendance100.mybicocca.data.auth.ElearningSession
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.elearning.badge.BadgeDao
import it.attendance100.mybicocca.data.local.elearning.badge.BadgeEntity
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncScope
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncStateDao
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncStateEntity
import it.attendance100.mybicocca.data.remote.elearning.api.ElearningApi
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetUserBadgesResponse
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningUserBadge
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Behaviour coverage for the badge repository: scope selection (account-wide vs per-course) on
 * both the observe source and the per-scope refresh, the staleness skip, and the scope-id
 * convention (0 for the account-wide set, the course id otherwise).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ElearningBadgeRepositoryImplTest {

    private val accountId = AccountId("acc-1")
    private val courseId = CourseId(42)
    private val lmsUserId = 7
    private val account = elearningRepoTestAccount(accountId, lmsUserId)

    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val badgeDao = mockk<BadgeDao>(relaxed = true)
    private val syncStateDao = mockk<ElearningSyncStateDao>(relaxed = true)
    private val elearningApi = mockk<ElearningApi>(relaxed = true)
    private val token = "x".repeat(32)
    private val stalePolicy = StalePolicy(defaultTtlMs = 60_000L)

    private fun newRepository(): ElearningBadgeRepositoryImpl {
        every { sessionManager.activeAccount } returns MutableStateFlow(account)
        coEvery { sessionManager.elearning() } returns ElearningSession(elearningApi, token)
        coEvery { syncStateDao.getState(any(), any(), any()) } returns null
        return ElearningBadgeRepositoryImpl(sessionManager, badgeDao, syncStateDao, stalePolicy)
    }

    private fun badgeRow(id: Int) = BadgeEntity(
        accountId = accountId.value,
        badgeId = id,
        name = "Distintivo $id",
        description = null,
        imageUrl = null,
        issuedAtMs = null,
        courseId = null,
    )

    @Test
    fun `observeBadges with null course reads the account-wide source`() = runTest {
        val repository = newRepository()
        every { badgeDao.observeAll(accountId.value) } returns flowOf(listOf(badgeRow(1)))

        repository.observeBadges(accountId, courseId = null).test {
            val loaded = awaitItem() as Loadable.Loaded
            assertThat(loaded.value).hasSize(1)
            assertThat(loaded.value.first().id).isEqualTo(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeBadges with course reads the per-course source`() = runTest {
        val repository = newRepository()
        every { badgeDao.observeForCourse(accountId.value, courseId.value) } returns
            flowOf(listOf(badgeRow(2)))

        repository.observeBadges(accountId, courseId).test {
            val loaded = awaitItem() as Loadable.Loaded
            assertThat(loaded.value.first().id).isEqualTo(2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refreshBadges account-wide replaces the null scope and stamps scope id zero`() = runTest {
        val repository = newRepository()
        coEvery {
            elearningApi.badges.getUserBadges(token, lmsUserId, null, 0, 0, null, false)
        } returns ElearningGetUserBadgesResponse(
            badges = listOf(ElearningUserBadge(id = 9, name = "Pioniere")),
        )

        repository.refreshBadges(accountId, courseId = null, force = false)

        val rows = slot<List<BadgeEntity>>()
        coVerify { badgeDao.replaceForCourse(accountId.value, null, capture(rows)) }
        assertThat(rows.captured.first().badgeId).isEqualTo(9)
        coVerify {
            syncStateDao.upsertState(match { it.scope == ElearningSyncScope.BADGES && it.scopeId == 0L })
        }
    }

    @Test
    fun `refreshBadges per-course passes the course id and stamps it as the scope id`() = runTest {
        val repository = newRepository()
        coEvery {
            elearningApi.badges.getUserBadges(token, lmsUserId, courseId.value, 0, 0, null, false)
        } returns ElearningGetUserBadgesResponse(badges = emptyList())

        repository.refreshBadges(accountId, courseId, force = false)

        coVerify { badgeDao.replaceForCourse(accountId.value, courseId.value, emptyList()) }
        coVerify {
            syncStateDao.upsertState(match { it.scope == ElearningSyncScope.BADGES && it.scopeId == courseId.value.toLong() })
        }
    }

    @Test
    fun `refreshBadges skips network while fresh and not forced`() = runTest {
        val repository = newRepository()
        coEvery {
            syncStateDao.getState(accountId.value, ElearningSyncScope.BADGES, 0L)
        } returns ElearningSyncStateEntity(
            accountId = accountId.value,
            scope = ElearningSyncScope.BADGES,
            scopeId = 0L,
            lastRefreshedAtMs = System.currentTimeMillis(),
        )

        repository.refreshBadges(accountId, courseId = null, force = false)

        coVerify(exactly = 0) { elearningApi.badges.getUserBadges(any(), any(), any(), any(), any(), any(), any()) }
        coVerify(exactly = 0) { badgeDao.replaceForCourse(any(), any(), any()) }
    }

    @Test
    fun `refreshBadges with force fetches even when fresh`() = runTest {
        val repository = newRepository()
        coEvery {
            syncStateDao.getState(accountId.value, ElearningSyncScope.BADGES, 0L)
        } returns ElearningSyncStateEntity(
            accountId = accountId.value,
            scope = ElearningSyncScope.BADGES,
            scopeId = 0L,
            lastRefreshedAtMs = System.currentTimeMillis(),
        )
        coEvery {
            elearningApi.badges.getUserBadges(token, lmsUserId, null, 0, 0, null, false)
        } returns ElearningGetUserBadgesResponse(badges = emptyList())

        repository.refreshBadges(accountId, courseId = null, force = true)

        coVerify { elearningApi.badges.getUserBadges(token, lmsUserId, null, 0, 0, null, false) }
    }

    @Test
    fun `clearForAccount delegates to the DAO`() = runTest {
        val repository = newRepository()

        repository.clearForAccount(accountId)

        coVerify { badgeDao.deleteForAccount(accountId.value) }
    }
}
