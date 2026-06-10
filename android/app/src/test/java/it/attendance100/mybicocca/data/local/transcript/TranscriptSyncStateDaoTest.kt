package it.attendance100.mybicocca.data.local.transcript

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.local.account.AccountEntity
import it.attendance100.mybicocca.data.local.account.CareerEntity
import it.attendance100.mybicocca.data.local.account.MyBicoccaDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Behaviour coverage for [TranscriptSyncStateDao] against a real in-memory Room database
 * (Robolectric). Exercises the per-career freshness stamp/get round-trip, the upsert overwrite of
 * the last-refresh timestamp, and the account-wide purge that resolves careers through the
 * `careers` table on sign-out.
 *
 * Wave 2 (Android-runtime) test: Robolectric drives the actual Room-generated `_Impl`, pinned to a
 * Robolectric-supported SDK because the module compiles against a newer one. The sync-state table
 * has no foreign-key constraint on `career_id`, so a stamp inserts without a parent; the
 * account-wide delete needs real `accounts`/`careers` parents because it joins through `careers`.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TranscriptSyncStateDaoTest {

    private lateinit var db: MyBicoccaDatabase
    private lateinit var dao: TranscriptSyncStateDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MyBicoccaDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.transcriptSyncStateDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `getState returns null before any stamp`() = runTest {
        assertThat(dao.getState(10L)).isNull()
    }

    @Test
    fun `upsertState then getState round-trips the freshness stamp`() = runTest {
        dao.upsertState(TranscriptSyncStateEntity(careerId = 10L, lastRefreshedAtMs = 1_700_000L))

        val stored = dao.getState(10L)

        assertThat(stored).isNotNull()
        assertThat(stored!!.lastRefreshedAtMs).isEqualTo(1_700_000L)
    }

    @Test
    fun `upsertState overwrites the timestamp for the same career`() = runTest {
        dao.upsertState(TranscriptSyncStateEntity(careerId = 10L, lastRefreshedAtMs = 1_000L))

        dao.upsertState(TranscriptSyncStateEntity(careerId = 10L, lastRefreshedAtMs = 9_000L))

        assertThat(dao.getState(10L)!!.lastRefreshedAtMs).isEqualTo(9_000L)
    }

    @Test
    fun `getState is scoped per career`() = runTest {
        dao.upsertState(TranscriptSyncStateEntity(careerId = 10L, lastRefreshedAtMs = 1_000L))
        dao.upsertState(TranscriptSyncStateEntity(careerId = 20L, lastRefreshedAtMs = 2_000L))

        assertThat(dao.getState(10L)!!.lastRefreshedAtMs).isEqualTo(1_000L)
        assertThat(dao.getState(20L)!!.lastRefreshedAtMs).isEqualTo(2_000L)
    }

    @Test
    fun `deleteForAccount purges every career belonging to the account`() = runTest {
        seedAccountWithCareers(accountId = "acc-1", careerIds = listOf(10L, 11L))
        seedAccountWithCareers(accountId = "acc-2", careerIds = listOf(20L))
        dao.upsertState(TranscriptSyncStateEntity(careerId = 10L, lastRefreshedAtMs = 1_000L))
        dao.upsertState(TranscriptSyncStateEntity(careerId = 11L, lastRefreshedAtMs = 2_000L))
        dao.upsertState(TranscriptSyncStateEntity(careerId = 20L, lastRefreshedAtMs = 3_000L))

        dao.deleteForAccount("acc-1")

        assertThat(dao.getState(10L)).isNull()
        assertThat(dao.getState(11L)).isNull()
        assertThat(dao.getState(20L)).isNotNull()
    }

    private suspend fun seedAccountWithCareers(accountId: String, careerIds: List<Long>) {
        db.accountDao().upsertAccount(account(accountId))
        db.accountDao().replaceCareers(accountId, careerIds.map { career(it, accountId) })
    }

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
}
