package it.attendance100.mybicocca.data.local.transcript

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
 * Behaviour coverage for [TranscriptDao] against a real in-memory Room database (Robolectric).
 * Exercises the career-scoped row ordering (dateless rows last, then ISO date ascending, then id),
 * the SQL-side grade rollup and its null-folding on empty sets, the `replaceAll` transaction that
 * swaps a career's rows while leaving other careers untouched, and the account-wide purges that
 * resolve careers through the `careers` table.
 *
 * Wave 2 (Android-runtime) test: Robolectric drives the actual Room-generated `_Impl`, pinned to a
 * Robolectric-supported SDK because the module compiles against a newer one. The transcript tables
 * have no foreign-key constraint on `career_id`, so rows insert without a parent; the account-wide
 * deletes need real `accounts`/`careers` parents because they join through the `careers` table.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TranscriptDaoTest {

    private lateinit var db: MyBicoccaDatabase
    private lateinit var dao: TranscriptDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MyBicoccaDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.transcriptDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `observeRows orders dated rows ascending with dateless rows last`() = runTest {
        dao.upsertRows(
            listOf(
                row(id = 1L, careerId = 10L, examDate = "2024-06-15"),
                row(id = 2L, careerId = 10L, examDate = null),
                row(id = 3L, careerId = 10L, examDate = "2024-01-09"),
            ),
        )

        val rows = dao.observeRows(10L).first()

        assertThat(rows.map { it.id }).containsExactly(3L, 1L, 2L).inOrder()
    }

    @Test
    fun `observeRows breaks date ties by id ascending`() = runTest {
        dao.upsertRows(
            listOf(
                row(id = 5L, careerId = 10L, examDate = "2024-03-01"),
                row(id = 2L, careerId = 10L, examDate = "2024-03-01"),
            ),
        )

        val rows = dao.observeRows(10L).first()

        assertThat(rows.map { it.id }).containsExactly(2L, 5L).inOrder()
    }

    @Test
    fun `observeRows is scoped to its career`() = runTest {
        dao.upsertRows(
            listOf(
                row(id = 1L, careerId = 10L, examDate = "2024-06-15"),
                row(id = 1L, careerId = 20L, examDate = "2024-06-15"),
            ),
        )

        assertThat(dao.observeRows(10L).first().map { it.careerId }).containsExactly(10L)
        assertThat(dao.observeRows(20L).first().map { it.careerId }).containsExactly(20L)
    }

    @Test
    fun `observeRows re-emits after an upsert`() = runTest {
        dao.observeRows(10L).test {
            assertThat(awaitItem()).isEmpty()

            dao.upsertRows(listOf(row(id = 1L, careerId = 10L, examDate = "2024-06-15")))
            assertThat(awaitItem().map { it.id }).containsExactly(1L)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeGradeRollup aggregates only passed numerically graded rows`() = runTest {
        dao.upsertRows(
            listOf(
                row(id = 1L, careerId = 10L, state = "Passed", grade = 30, credits = 6f),
                row(id = 2L, careerId = 10L, state = "Passed", grade = 27, credits = 9f),
                row(id = 3L, careerId = 10L, state = "Passed", grade = null, credits = 6f),
                row(id = 4L, careerId = 10L, state = "Planned", grade = 28, credits = 6f),
            ),
        )

        val rollup = dao.observeGradeRollup(10L).first()!!

        assertThat(rollup.gradedExamCount).isEqualTo(2)
        assertThat(rollup.gradeSum).isEqualTo(57L)
        assertThat(rollup.weightedGradeSum).isEqualTo(30.0 * 6 + 27.0 * 9)
        assertThat(rollup.gradedCreditsSum).isEqualTo(15f)
    }

    @Test
    fun `observeGradeRollup folds nulls when no passed graded rows exist`() = runTest {
        dao.upsertRows(
            listOf(
                row(id = 1L, careerId = 10L, state = "Planned", grade = 30, credits = 6f),
                row(id = 2L, careerId = 10L, state = "Passed", grade = null, credits = 6f),
            ),
        )

        val rollup = dao.observeGradeRollup(10L).first()!!

        assertThat(rollup.gradedExamCount).isEqualTo(0)
        assertThat(rollup.gradeSum).isNull()
        assertThat(rollup.weightedGradeSum).isNull()
        assertThat(rollup.gradedCreditsSum).isNull()
    }

    @Test
    fun `getPassedActivityNames returns only passed rows for the career`() = runTest {
        dao.upsertRows(
            listOf(
                row(id = 1L, careerId = 10L, state = "Passed", activityName = "Analisi"),
                row(id = 2L, careerId = 10L, state = "Planned", activityName = "Fisica"),
                row(id = 3L, careerId = 20L, state = "Passed", activityName = "Chimica"),
            ),
        )

        val names = dao.getPassedActivityNames(10L)

        assertThat(names).containsExactly("Analisi")
    }

    @Test
    fun `upsertStats then observeStats round-trips the aggregate row`() = runTest {
        dao.upsertStats(stats(careerId = 10L, passedCredits = 120f))

        val stored = dao.observeStats(10L).first()

        assertThat(stored).isNotNull()
        assertThat(stored!!.passedCredits).isEqualTo(120f)
    }

    @Test
    fun `observeStats emits null for a career with no stats row`() = runTest {
        assertThat(dao.observeStats(99L).first()).isNull()
    }

    @Test
    fun `upsertStats replaces the existing row for the same career`() = runTest {
        dao.upsertStats(stats(careerId = 10L, passedCredits = 60f))

        dao.upsertStats(stats(careerId = 10L, passedCredits = 90f))

        assertThat(dao.observeStats(10L).first()!!.passedCredits).isEqualTo(90f)
    }

    @Test
    fun `replaceAll swaps the career rows and upserts stats`() = runTest {
        dao.upsertRows(listOf(row(id = 1L, careerId = 10L, examDate = "2024-01-01")))
        dao.upsertStats(stats(careerId = 10L, passedCredits = 30f))

        dao.replaceAll(
            careerId = 10L,
            rows = listOf(
                row(id = 2L, careerId = 10L, examDate = "2024-05-05"),
                row(id = 3L, careerId = 10L, examDate = "2024-06-06"),
            ),
            stats = stats(careerId = 10L, passedCredits = 75f),
        )

        assertThat(dao.observeRows(10L).first().map { it.id }).containsExactly(2L, 3L).inOrder()
        assertThat(dao.observeStats(10L).first()!!.passedCredits).isEqualTo(75f)
    }

    @Test
    fun `replaceAll leaves other careers untouched`() = runTest {
        dao.upsertRows(listOf(row(id = 1L, careerId = 20L, examDate = "2024-01-01")))
        dao.upsertStats(stats(careerId = 20L, passedCredits = 12f))

        dao.replaceAll(
            careerId = 10L,
            rows = listOf(row(id = 2L, careerId = 10L, examDate = "2024-05-05")),
            stats = stats(careerId = 10L, passedCredits = 33f),
        )

        assertThat(dao.observeRows(20L).first().map { it.id }).containsExactly(1L)
        assertThat(dao.observeStats(20L).first()!!.passedCredits).isEqualTo(12f)
    }

    @Test
    fun `replaceAll with an empty row list clears the career rows but keeps stats`() = runTest {
        dao.upsertRows(listOf(row(id = 1L, careerId = 10L, examDate = "2024-01-01")))

        dao.replaceAll(
            careerId = 10L,
            rows = emptyList(),
            stats = stats(careerId = 10L, passedCredits = 0f),
        )

        assertThat(dao.observeRows(10L).first()).isEmpty()
        assertThat(dao.observeStats(10L).first()).isNotNull()
    }

    @Test
    fun `deleteRows removes only the targeted career`() = runTest {
        dao.upsertRows(
            listOf(
                row(id = 1L, careerId = 10L, examDate = "2024-01-01"),
                row(id = 1L, careerId = 20L, examDate = "2024-01-01"),
            ),
        )

        dao.deleteRows(10L)

        assertThat(dao.observeRows(10L).first()).isEmpty()
        assertThat(dao.observeRows(20L).first().map { it.careerId }).containsExactly(20L)
    }

    @Test
    fun `deleteStats removes only the targeted career`() = runTest {
        dao.upsertStats(stats(careerId = 10L, passedCredits = 30f))
        dao.upsertStats(stats(careerId = 20L, passedCredits = 60f))

        dao.deleteStats(10L)

        assertThat(dao.observeStats(10L).first()).isNull()
        assertThat(dao.observeStats(20L).first()).isNotNull()
    }

    @Test
    fun `deleteRowsForAccount purges every career belonging to the account`() = runTest {
        seedAccountWithCareers(accountId = "acc-1", careerIds = listOf(10L, 11L))
        seedAccountWithCareers(accountId = "acc-2", careerIds = listOf(20L))
        dao.upsertRows(
            listOf(
                row(id = 1L, careerId = 10L, examDate = "2024-01-01"),
                row(id = 2L, careerId = 11L, examDate = "2024-01-01"),
                row(id = 3L, careerId = 20L, examDate = "2024-01-01"),
            ),
        )

        dao.deleteRowsForAccount("acc-1")

        assertThat(dao.observeRows(10L).first()).isEmpty()
        assertThat(dao.observeRows(11L).first()).isEmpty()
        assertThat(dao.observeRows(20L).first().map { it.id }).containsExactly(3L)
    }

    @Test
    fun `deleteStatsForAccount purges every career belonging to the account`() = runTest {
        seedAccountWithCareers(accountId = "acc-1", careerIds = listOf(10L, 11L))
        seedAccountWithCareers(accountId = "acc-2", careerIds = listOf(20L))
        dao.upsertStats(stats(careerId = 10L, passedCredits = 30f))
        dao.upsertStats(stats(careerId = 11L, passedCredits = 40f))
        dao.upsertStats(stats(careerId = 20L, passedCredits = 50f))

        dao.deleteStatsForAccount("acc-1")

        assertThat(dao.observeStats(10L).first()).isNull()
        assertThat(dao.observeStats(11L).first()).isNull()
        assertThat(dao.observeStats(20L).first()).isNotNull()
    }

    private suspend fun seedAccountWithCareers(accountId: String, careerIds: List<Long>) {
        db.accountDao().upsertAccount(account(accountId))
        db.accountDao().replaceCareers(accountId, careerIds.map { career(it, accountId) })
    }

    private fun row(
        id: Long,
        careerId: Long,
        activityCode: String? = "E3201",
        activityName: String = "Activity $id",
        courseYear: Int = 1,
        credits: Float = 6f,
        state: String = "Passed",
        grade: Int? = 30,
        cumLaude: Boolean = false,
        examDate: String? = null,
        academicYear: Int? = 2024,
        inStudyPlan: Boolean = true,
    ) = TranscriptRowEntity(
        id = id,
        careerId = careerId,
        activityCode = activityCode,
        activityName = activityName,
        courseYear = courseYear,
        credits = credits,
        state = state,
        grade = grade,
        cumLaude = cumLaude,
        examDate = examDate,
        academicYear = academicYear,
        inStudyPlan = inStudyPlan,
    )

    private fun stats(
        careerId: Long,
        passedCredits: Float,
    ) = TranscriptStatsEntity(
        careerId = careerId,
        passedCredits = passedCredits,
        totalCreditsRequired = 180f,
        arithmeticAverage = 27.5f,
        weightedAverage = 28.0f,
        passedExamCount = 5,
        plannedExamCount = 20,
        maxGrade = 30,
        cumLaudeAvailable = true,
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
}
