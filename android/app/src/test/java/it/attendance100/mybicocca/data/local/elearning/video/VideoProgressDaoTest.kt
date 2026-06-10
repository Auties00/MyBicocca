package it.attendance100.mybicocca.data.local.elearning.video

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.local.account.MyBicoccaDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Behaviour coverage for [VideoProgressDao] against a real in-memory Room database (Robolectric).
 * Exercises the resume-position cache keyed by (account, cm): the REPLACE-on-conflict upsert that
 * overwrites a video's saved position rather than duplicating it, the single-row `observe`/`getOnce`
 * lookups, the per-course stream, account/course scoping, and that `deleteForAccount` clears one
 * account only. No foreign key is declared, so rows insert without a parent account.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class VideoProgressDaoTest {

    private lateinit var db: MyBicoccaDatabase
    private lateinit var dao: VideoProgressDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MyBicoccaDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.elearningVideoProgressDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `upsert then getOnce round-trips the saved position`() = runTest {
        dao.upsert(progress(cmId = 7, positionMs = 42_000L, durationMs = 120_000L))

        val stored = dao.getOnce("acc-1", cmId = 7)

        assertThat(stored).isNotNull()
        assertThat(stored!!.positionMs).isEqualTo(42_000L)
        assertThat(stored.durationMs).isEqualTo(120_000L)
    }

    @Test
    fun `upsert replaces the position of an existing video rather than duplicating it`() = runTest {
        dao.upsert(progress(cmId = 7, positionMs = 10_000L, completed = false))

        dao.upsert(progress(cmId = 7, positionMs = 90_000L, completed = true))

        val stored = dao.getOnce("acc-1", cmId = 7)
        assertThat(stored!!.positionMs).isEqualTo(90_000L)
        assertThat(stored.completed).isTrue()
        assertThat(dao.observeByCourse("acc-1", courseId = 100).first()).hasSize(1)
    }

    @Test
    fun `getOnce and observe are scoped to account and cm`() = runTest {
        dao.upsert(progress(cmId = 7, accountId = "acc-1"))
        dao.upsert(progress(cmId = 7, accountId = "acc-2", positionMs = 99_000L))

        assertThat(dao.getOnce("acc-1", cmId = 7)!!.positionMs).isEqualTo(42_000L)
        assertThat(dao.getOnce("acc-2", cmId = 7)!!.positionMs).isEqualTo(99_000L)
        assertThat(dao.getOnce("acc-1", cmId = 8)).isNull()
    }

    @Test
    fun `observeByCourse returns only that course's videos for the account`() = runTest {
        dao.upsert(progress(cmId = 1, courseId = 100))
        dao.upsert(progress(cmId = 2, courseId = 100))
        dao.upsert(progress(cmId = 3, courseId = 200))
        dao.upsert(progress(cmId = 4, courseId = 100, accountId = "acc-2"))

        val rows = dao.observeByCourse("acc-1", courseId = 100).first()

        assertThat(rows.map { it.cmId }).containsExactly(1, 2)
    }

    @Test
    fun `deleteForAccount clears one account only`() = runTest {
        dao.upsert(progress(cmId = 1, accountId = "acc-1"))
        dao.upsert(progress(cmId = 2, accountId = "acc-2"))

        dao.deleteForAccount("acc-1")

        assertThat(dao.getOnce("acc-1", cmId = 1)).isNull()
        assertThat(dao.getOnce("acc-2", cmId = 2)).isNotNull()
    }

    @Test
    fun `observe re-emits after the position is updated`() = runTest {
        dao.upsert(progress(cmId = 7, positionMs = 1_000L))

        dao.observe("acc-1", cmId = 7).test {
            assertThat(awaitItem()!!.positionMs).isEqualTo(1_000L)

            dao.upsert(progress(cmId = 7, positionMs = 55_000L))
            assertThat(awaitItem()!!.positionMs).isEqualTo(55_000L)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun progress(
        cmId: Int,
        accountId: String = "acc-1",
        courseId: Int = 100,
        positionMs: Long = 42_000L,
        durationMs: Long = 120_000L,
        completed: Boolean = false,
    ) = VideoProgressEntity(
        accountId = accountId,
        cmId = cmId,
        courseId = courseId,
        positionMs = positionMs,
        durationMs = durationMs,
        completed = completed,
        lastUpdatedAtMs = 1_000L,
    )
}
