package it.attendance100.mybicocca.data.local.elearning.sync

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.local.account.MyBicoccaDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Behaviour coverage for [ElearningSyncStateDao] against a real in-memory Room database
 * (Robolectric). The table is the freshness ledger for every e-learning cache: keyed by
 * (account, scope, scope_id). Covers the get/upsert round-trip, that the composite key isolates
 * scopes and scope ids from one another, that an upsert re-stamps an existing row rather than
 * duplicating it, and that `deleteForAccount` clears every scope of one account only.
 * No foreign key is declared, so rows insert without a parent account.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ElearningSyncStateDaoTest {

    private lateinit var db: MyBicoccaDatabase
    private lateinit var dao: ElearningSyncStateDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MyBicoccaDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.elearningSyncStateDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `upsertState then getState round-trips the stamp`() = runTest {
        dao.upsertState(state(scope = ElearningSyncScope.ENROLLED_COURSES, scopeId = 0L, stampedAt = 5_000L))

        val row = dao.getState("acc-1", ElearningSyncScope.ENROLLED_COURSES, 0L)

        assertThat(row).isNotNull()
        assertThat(row!!.lastRefreshedAtMs).isEqualTo(5_000L)
    }

    @Test
    fun `getState returns null for an unstamped scope`() = runTest {
        dao.upsertState(state(scope = ElearningSyncScope.COURSE_FORUMS, scopeId = 100L, stampedAt = 1_000L))

        assertThat(dao.getState("acc-1", ElearningSyncScope.COURSE_QUIZZES, 100L)).isNull()
        assertThat(dao.getState("acc-1", ElearningSyncScope.COURSE_FORUMS, 999L)).isNull()
        assertThat(dao.getState("acc-2", ElearningSyncScope.COURSE_FORUMS, 100L)).isNull()
    }

    @Test
    fun `the composite key isolates scope id within the same scope`() = runTest {
        dao.upsertState(state(scope = ElearningSyncScope.FORUM_DISCUSSIONS, scopeId = 10L, stampedAt = 1_000L))
        dao.upsertState(state(scope = ElearningSyncScope.FORUM_DISCUSSIONS, scopeId = 20L, stampedAt = 2_000L))

        assertThat(dao.getState("acc-1", ElearningSyncScope.FORUM_DISCUSSIONS, 10L)!!.lastRefreshedAtMs)
            .isEqualTo(1_000L)
        assertThat(dao.getState("acc-1", ElearningSyncScope.FORUM_DISCUSSIONS, 20L)!!.lastRefreshedAtMs)
            .isEqualTo(2_000L)
    }

    @Test
    fun `upsertState re-stamps an existing row in place`() = runTest {
        dao.upsertState(state(scope = ElearningSyncScope.BADGES, scopeId = 0L, stampedAt = 1_000L))

        dao.upsertState(state(scope = ElearningSyncScope.BADGES, scopeId = 0L, stampedAt = 7_000L))

        assertThat(dao.getState("acc-1", ElearningSyncScope.BADGES, 0L)!!.lastRefreshedAtMs)
            .isEqualTo(7_000L)
    }

    @Test
    fun `deleteForAccount forces every scope of that account stale and leaves others`() = runTest {
        dao.upsertState(state(scope = ElearningSyncScope.ENROLLED_COURSES, scopeId = 0L, stampedAt = 1_000L))
        dao.upsertState(state(scope = ElearningSyncScope.COURSE_GRADES, scopeId = 100L, stampedAt = 2_000L))
        dao.upsertState(state(scope = ElearningSyncScope.ENROLLED_COURSES, scopeId = 0L, stampedAt = 3_000L, accountId = "acc-2"))

        dao.deleteForAccount("acc-1")

        assertThat(dao.getState("acc-1", ElearningSyncScope.ENROLLED_COURSES, 0L)).isNull()
        assertThat(dao.getState("acc-1", ElearningSyncScope.COURSE_GRADES, 100L)).isNull()
        assertThat(dao.getState("acc-2", ElearningSyncScope.ENROLLED_COURSES, 0L)).isNotNull()
    }

    private fun state(
        scope: String,
        scopeId: Long,
        stampedAt: Long,
        accountId: String = "acc-1",
    ) = ElearningSyncStateEntity(
        accountId = accountId,
        scope = scope,
        scopeId = scopeId,
        lastRefreshedAtMs = stampedAt,
    )
}
