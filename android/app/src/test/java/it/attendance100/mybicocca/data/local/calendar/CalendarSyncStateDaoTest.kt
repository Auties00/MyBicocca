package it.attendance100.mybicocca.data.local.calendar

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.local.account.AccountEntity
import it.attendance100.mybicocca.data.local.account.CareerEntity
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
 * Behaviour coverage for [CalendarSyncStateDao] against a real in-memory Room database
 * (Robolectric). Verifies the per-(career, source, scope) freshness stamp round-trip and hot
 * stream, the source-wide stamp invalidation that spans every scope, and the account-wide wipe
 * that resolves careers through the careers table.
 *
 * Stamps are keyed by a plain `career_id` column with no foreign key, so they insert without a
 * parent; only [CalendarSyncStateDao.deleteForAccount] joins the careers table and so needs real
 * account/career parents seeded via the account DAO.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CalendarSyncStateDaoTest {

    private lateinit var db: MyBicoccaDatabase
    private lateinit var dao: CalendarSyncStateDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MyBicoccaDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.calendarSyncStateDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `getState returns null when the unit has never synced`() = runTest {
        val state = dao.getState(careerId = 1L, source = LESSONS, yearMonth = "2026-06")

        assertThat(state).isNull()
    }

    @Test
    fun `upsertState then getState round-trips the stamp`() = runTest {
        dao.upsertState(state(careerId = 1L, source = LESSONS, yearMonth = "2026-06", refreshedAt = 5_000L))

        val state = dao.getState(careerId = 1L, source = LESSONS, yearMonth = "2026-06")

        assertThat(state).isNotNull()
        assertThat(state!!.lastRefreshedAtMs).isEqualTo(5_000L)
    }

    @Test
    fun `upsertState replaces the stamp of an existing key`() = runTest {
        dao.upsertState(state(careerId = 1L, source = LESSONS, yearMonth = "2026-06", refreshedAt = 1_000L))

        dao.upsertState(state(careerId = 1L, source = LESSONS, yearMonth = "2026-06", refreshedAt = 9_000L))

        assertThat(dao.getState(1L, LESSONS, "2026-06")!!.lastRefreshedAtMs).isEqualTo(9_000L)
    }

    @Test
    fun `getState distinguishes career source and scope of the composite key`() = runTest {
        dao.upsertState(state(careerId = 1L, source = LESSONS, yearMonth = "2026-06", refreshedAt = 100L))
        dao.upsertState(state(careerId = 2L, source = LESSONS, yearMonth = "2026-06", refreshedAt = 200L))
        dao.upsertState(state(careerId = 1L, source = EXAMS, yearMonth = "*", refreshedAt = 300L))
        dao.upsertState(state(careerId = 1L, source = LESSONS, yearMonth = "2026-07", refreshedAt = 400L))

        assertThat(dao.getState(1L, LESSONS, "2026-06")!!.lastRefreshedAtMs).isEqualTo(100L)
        assertThat(dao.getState(2L, LESSONS, "2026-06")!!.lastRefreshedAtMs).isEqualTo(200L)
        assertThat(dao.getState(1L, EXAMS, "*")!!.lastRefreshedAtMs).isEqualTo(300L)
        assertThat(dao.getState(1L, LESSONS, "2026-07")!!.lastRefreshedAtMs).isEqualTo(400L)
    }

    @Test
    fun `observeState emits null first then the stamp after an upsert`() = runTest {
        dao.observeState(careerId = 1L, source = LESSONS, yearMonth = "2026-06").test {
            assertThat(awaitItem()).isNull()

            dao.upsertState(state(careerId = 1L, source = LESSONS, yearMonth = "2026-06", refreshedAt = 7_000L))

            assertThat(awaitItem()!!.lastRefreshedAtMs).isEqualTo(7_000L)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteStatesForSource drops every scope of one source but keeps other sources`() = runTest {
        dao.upsertState(state(careerId = 1L, source = LESSONS, yearMonth = "2026-06", refreshedAt = 1L))
        dao.upsertState(state(careerId = 1L, source = LESSONS, yearMonth = "2026-07", refreshedAt = 2L))
        dao.upsertState(state(careerId = 1L, source = EXAMS, yearMonth = "*", refreshedAt = 3L))

        dao.deleteStatesForSource(careerId = 1L, source = LESSONS)

        assertThat(dao.getState(1L, LESSONS, "2026-06")).isNull()
        assertThat(dao.getState(1L, LESSONS, "2026-07")).isNull()
        assertThat(dao.getState(1L, EXAMS, "*")).isNotNull()
    }

    @Test
    fun `deleteStatesForSource of one career leaves another career's stamps intact`() = runTest {
        dao.upsertState(state(careerId = 1L, source = LESSONS, yearMonth = "2026-06", refreshedAt = 1L))
        dao.upsertState(state(careerId = 2L, source = LESSONS, yearMonth = "2026-06", refreshedAt = 2L))

        dao.deleteStatesForSource(careerId = 1L, source = LESSONS)

        assertThat(dao.getState(1L, LESSONS, "2026-06")).isNull()
        assertThat(dao.getState(2L, LESSONS, "2026-06")).isNotNull()
    }

    @Test
    fun `deleteForAccount wipes the stamps of every career of that account only`() = runTest {
        db.accountDao().upsertAccount(account("acc-1"))
        db.accountDao().upsertAccount(account("acc-2"))
        db.accountDao().replaceCareers("acc-1", listOf(career(11L, "acc-1"), career(12L, "acc-1")))
        db.accountDao().replaceCareers("acc-2", listOf(career(21L, "acc-2")))
        dao.upsertState(state(careerId = 11L, source = LESSONS, yearMonth = "2026-06", refreshedAt = 1L))
        dao.upsertState(state(careerId = 12L, source = EXAMS, yearMonth = "*", refreshedAt = 2L))
        dao.upsertState(state(careerId = 21L, source = LESSONS, yearMonth = "2026-06", refreshedAt = 3L))

        dao.deleteForAccount("acc-1")

        assertThat(dao.getState(11L, LESSONS, "2026-06")).isNull()
        assertThat(dao.getState(12L, EXAMS, "*")).isNull()
        assertThat(dao.getState(21L, LESSONS, "2026-06")).isNotNull()
    }

    private fun state(
        careerId: Long,
        source: String,
        yearMonth: String,
        refreshedAt: Long,
    ) = CalendarSyncStateEntity(
        careerId = careerId,
        source = source,
        yearMonth = yearMonth,
        lastRefreshedAtMs = refreshedAt,
    )

    private fun account(id: String) = AccountEntity(
        id = id,
        username = "mario.rossi",
        displayName = "Mario Rossi",
        recordUserId = "rec-$id",
        personId = 7L,
        fiscalCode = null,
        selectedCareerId = 0L,
        lmsUserId = 11,
        lmsUsername = "mario.rossi",
        lmsLocale = "it",
        lmsIsSiteAdmin = false,
        lmsMaxUploadBytes = 0L,
        lmsStorageQuotaBytes = 0L,
        createdAtEpochMillis = 0L,
        lastUsedAtEpochMillis = 0L,
        lastSyncedAtEpochMillis = 0L,
    )

    private fun career(id: Long, accountId: String) = CareerEntity(
        id = id,
        accountId = accountId,
        enrollmentTraitId = 9_999L,
        programId = 1_000L,
        easyStaffProgramCode = "E3201Q",
        academicYearEnrollmentId = 2024L,
        studentNumber = "123456",
        description = "Informatica",
        academicYear = 2024,
        status = "Active",
    )

    private companion object {
        const val LESSONS = "Lessons"
        const val EXAMS = "Exams"
    }
}
