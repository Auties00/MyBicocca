package it.attendance100.mybicocca.data.local.account

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Behaviour coverage for [AccountDao] against a real in-memory Room database (Robolectric).
 * Exercises the account/career relation, the cascading foreign key (deleting an account removes
 * its careers), the `last_used_at DESC` ordering of the account list, and the selected-career
 * update — the queries the account switcher and multi-career flows depend on.
 *
 * Wave 2 (Android-runtime) reference test: Robolectric drives the actual Room-generated `_Impl`,
 * pinned to a Robolectric-supported SDK because the module compiles against a newer one.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AccountDaoTest {

    private lateinit var db: MyBicoccaDatabase
    private lateinit var dao: AccountDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MyBicoccaDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.accountDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `upsertAccount then getById returns the stored account with no careers`() = runTest {
        dao.upsertAccount(account("acc-1"))

        val stored = dao.getById("acc-1")

        assertThat(stored).isNotNull()
        assertThat(stored!!.account.username).isEqualTo("mario.rossi")
        assertThat(stored.careers).isEmpty()
    }

    @Test
    fun `replaceCareers attaches careers to the account via the relation`() = runTest {
        dao.upsertAccount(account("acc-1"))

        dao.replaceCareers("acc-1", listOf(career(101L, "acc-1"), career(102L, "acc-1")))

        val stored = dao.getById("acc-1")
        assertThat(stored!!.careers.map { it.id }).containsExactly(101L, 102L)
    }

    @Test
    fun `replaceCareers swaps the previous career set wholesale`() = runTest {
        dao.upsertAccount(account("acc-1"))
        dao.replaceCareers("acc-1", listOf(career(101L, "acc-1")))

        dao.replaceCareers("acc-1", listOf(career(202L, "acc-1")))

        val stored = dao.getById("acc-1")
        assertThat(stored!!.careers.map { it.id }).containsExactly(202L)
    }

    @Test
    fun `deleting an account cascades to its careers`() = runTest {
        dao.upsertAccount(account("acc-1"))
        dao.replaceCareers("acc-1", listOf(career(101L, "acc-1")))

        dao.delete("acc-1")

        assertThat(dao.getById("acc-1")).isNull()
        assertThat(dao.observeAll().first()).isEmpty()
    }

    @Test
    fun `updateSelectedCareer changes the selected career and bumps last used`() = runTest {
        dao.upsertAccount(account("acc-1", selectedCareerId = 101L, lastUsedAt = 1_000L))

        dao.updateSelectedCareer("acc-1", careerId = 202L, timestamp = 5_000L)

        val stored = dao.getById("acc-1")!!.account
        assertThat(stored.selectedCareerId).isEqualTo(202L)
        assertThat(stored.lastUsedAtEpochMillis).isEqualTo(5_000L)
    }

    @Test
    fun `observeAll orders accounts by last used descending and emits on change`() = runTest {
        dao.upsertAccount(account("old", lastUsedAt = 1_000L))
        dao.upsertAccount(account("recent", lastUsedAt = 9_000L))

        dao.observeAll().test {
            assertThat(awaitItem().map { it.account.id }).containsExactly("recent", "old").inOrder()

            dao.updateLastUsed("old", timestamp = 10_000L)
            assertThat(awaitItem().map { it.account.id }).containsExactly("old", "recent").inOrder()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun account(
        id: String,
        selectedCareerId: Long = 0L,
        lastUsedAt: Long = 0L,
    ) = AccountEntity(
        id = id,
        username = "mario.rossi",
        displayName = "Mario Rossi",
        recordUserId = "rec-$id",
        personId = 7L,
        fiscalCode = null,
        selectedCareerId = selectedCareerId,
        lmsUserId = 11,
        lmsUsername = "mario.rossi",
        lmsLocale = "it",
        lmsIsSiteAdmin = false,
        lmsMaxUploadBytes = 0L,
        lmsStorageQuotaBytes = 0L,
        createdAtEpochMillis = 0L,
        lastUsedAtEpochMillis = lastUsedAt,
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
