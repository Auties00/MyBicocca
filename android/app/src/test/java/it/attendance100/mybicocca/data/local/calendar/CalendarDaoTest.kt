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
 * Behaviour coverage for [CalendarDao] against a real in-memory Room database (Robolectric).
 * Exercises the career-scoped inclusive-range observe query (ordered by date then start time),
 * the per-source splice-replace transaction (which clears only the targeted source's rows within
 * a date window and leaves other sources and out-of-range rows intact), the whole-career
 * per-source replace, and the account-wide wipe that resolves careers through the careers table.
 *
 * `career_id` is a plain column with no foreign key, so most rows insert without a parent; only
 * [CalendarDao.deleteForAccount] joins the careers table and therefore needs real account/career
 * parents seeded via the account DAO.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CalendarDaoTest {

    private lateinit var db: MyBicoccaDatabase
    private lateinit var dao: CalendarDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MyBicoccaDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.calendarDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `observeInRange returns only rows of the given career within the inclusive date range`() = runTest {
        dao.upsertAll(
            listOf(
                event("a", careerId = 1L, date = "2026-06-09"),
                event("b", careerId = 1L, date = "2026-06-10"),
                event("c", careerId = 1L, date = "2026-06-15"),
                event("d", careerId = 1L, date = "2026-06-16"),
                event("other-career", careerId = 2L, date = "2026-06-12"),
            )
        )

        val inRange = dao.observeInRange(careerId = 1L, startIso = "2026-06-10", endIso = "2026-06-15").first()

        assertThat(inRange.map { it.id }).containsExactly("b", "c").inOrder()
    }

    @Test
    fun `observeInRange treats the start and end bounds as inclusive`() = runTest {
        dao.upsertAll(
            listOf(
                event("start", careerId = 1L, date = "2026-06-10"),
                event("end", careerId = 1L, date = "2026-06-15"),
            )
        )

        val inRange = dao.observeInRange(careerId = 1L, startIso = "2026-06-10", endIso = "2026-06-15").first()

        assertThat(inRange.map { it.id }).containsExactly("start", "end")
    }

    @Test
    fun `observeInRange orders by date then start time`() = runTest {
        dao.upsertAll(
            listOf(
                event("late-same-day", careerId = 1L, date = "2026-06-10", startTime = "14:00"),
                event("early-same-day", careerId = 1L, date = "2026-06-10", startTime = "09:00"),
                event("next-day", careerId = 1L, date = "2026-06-11", startTime = "08:00"),
            )
        )

        val ordered = dao.observeInRange(careerId = 1L, startIso = "2026-06-01", endIso = "2026-06-30").first()

        assertThat(ordered.map { it.id })
            .containsExactly("early-same-day", "late-same-day", "next-day").inOrder()
    }

    @Test
    fun `observeInRange emits again after a write`() = runTest {
        dao.observeInRange(careerId = 1L, startIso = "2026-06-01", endIso = "2026-06-30").test {
            assertThat(awaitItem()).isEmpty()

            dao.upsertAll(listOf(event("fresh", careerId = 1L, date = "2026-06-12")))

            assertThat(awaitItem().map { it.id }).containsExactly("fresh")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `replaceSource clears only the targeted source within the range and inserts the fresh rows`() = runTest {
        dao.upsertAll(
            listOf(
                event("lesson-old", careerId = 1L, date = "2026-06-12", source = LESSONS),
                event("lesson-out-of-range", careerId = 1L, date = "2026-07-01", source = LESSONS),
                event("exam-in-range", careerId = 1L, date = "2026-06-12", source = EXAMS),
            )
        )

        dao.replaceSource(
            careerId = 1L,
            source = LESSONS,
            startIso = "2026-06-01",
            endIso = "2026-06-30",
            rows = listOf(event("lesson-new", careerId = 1L, date = "2026-06-20", source = LESSONS)),
        )

        val all = dao.observeInRange(careerId = 1L, startIso = "2026-01-01", endIso = "2026-12-31").first()
        assertThat(all.map { it.id })
            .containsExactly("lesson-new", "lesson-out-of-range", "exam-in-range")
    }

    @Test
    fun `replaceSource with empty rows deletes the slice without inserting`() = runTest {
        dao.upsertAll(
            listOf(
                event("lesson-in-range", careerId = 1L, date = "2026-06-12", source = LESSONS),
                event("lesson-out-of-range", careerId = 1L, date = "2026-07-01", source = LESSONS),
            )
        )

        dao.replaceSource(
            careerId = 1L,
            source = LESSONS,
            startIso = "2026-06-01",
            endIso = "2026-06-30",
            rows = emptyList(),
        )

        val all = dao.observeInRange(careerId = 1L, startIso = "2026-01-01", endIso = "2026-12-31").first()
        assertThat(all.map { it.id }).containsExactly("lesson-out-of-range")
    }

    @Test
    fun `replaceSource leaves another career's rows of the same source untouched`() = runTest {
        dao.upsertAll(
            listOf(
                event("mine", careerId = 1L, date = "2026-06-12", source = LESSONS),
                event("theirs", careerId = 2L, date = "2026-06-12", source = LESSONS),
            )
        )

        dao.replaceSource(
            careerId = 1L,
            source = LESSONS,
            startIso = "2026-06-01",
            endIso = "2026-06-30",
            rows = emptyList(),
        )

        val otherCareer = dao.observeInRange(careerId = 2L, startIso = "2026-06-01", endIso = "2026-06-30").first()
        assertThat(otherCareer.map { it.id }).containsExactly("theirs")
    }

    @Test
    fun `replaceSourceAll wipes the whole career source regardless of date and inserts the fresh set`() = runTest {
        dao.upsertAll(
            listOf(
                event("exam-jan", careerId = 1L, date = "2026-01-10", source = EXAMS),
                event("exam-dec", careerId = 1L, date = "2026-12-20", source = EXAMS),
                event("lesson-keep", careerId = 1L, date = "2026-06-12", source = LESSONS),
            )
        )

        dao.replaceSourceAll(
            careerId = 1L,
            source = EXAMS,
            rows = listOf(event("exam-new", careerId = 1L, date = "2026-09-15", source = EXAMS)),
        )

        val all = dao.observeInRange(careerId = 1L, startIso = "2026-01-01", endIso = "2026-12-31").first()
        assertThat(all.map { it.id }).containsExactly("lesson-keep", "exam-new")
    }

    @Test
    fun `replaceSourceAll with empty rows clears the source entirely`() = runTest {
        dao.upsertAll(
            listOf(
                event("exam-1", careerId = 1L, date = "2026-03-10", source = EXAMS),
                event("exam-2", careerId = 1L, date = "2026-08-20", source = EXAMS),
            )
        )

        dao.replaceSourceAll(careerId = 1L, source = EXAMS, rows = emptyList())

        val all = dao.observeInRange(careerId = 1L, startIso = "2026-01-01", endIso = "2026-12-31").first()
        assertThat(all).isEmpty()
    }

    @Test
    fun `deleteBySourceAndRange removes only the matching source rows in the window`() = runTest {
        dao.upsertAll(
            listOf(
                event("hit", careerId = 1L, date = "2026-06-12", source = LESSONS),
                event("wrong-source", careerId = 1L, date = "2026-06-12", source = EXAMS),
                event("out-of-range", careerId = 1L, date = "2026-08-01", source = LESSONS),
            )
        )

        dao.deleteBySourceAndRange(
            careerId = 1L,
            source = LESSONS,
            startIso = "2026-06-01",
            endIso = "2026-06-30",
        )

        val all = dao.observeInRange(careerId = 1L, startIso = "2026-01-01", endIso = "2026-12-31").first()
        assertThat(all.map { it.id }).containsExactly("wrong-source", "out-of-range")
    }

    @Test
    fun `deleteBySource removes every row of one source across all dates`() = runTest {
        dao.upsertAll(
            listOf(
                event("lesson-a", careerId = 1L, date = "2026-02-01", source = LESSONS),
                event("lesson-b", careerId = 1L, date = "2026-11-01", source = LESSONS),
                event("exam-keep", careerId = 1L, date = "2026-06-01", source = EXAMS),
            )
        )

        dao.deleteBySource(careerId = 1L, source = LESSONS)

        val all = dao.observeInRange(careerId = 1L, startIso = "2026-01-01", endIso = "2026-12-31").first()
        assertThat(all.map { it.id }).containsExactly("exam-keep")
    }

    @Test
    fun `deleteForAccount wipes the calendar rows of every career of that account only`() = runTest {
        db.accountDao().upsertAccount(account("acc-1"))
        db.accountDao().upsertAccount(account("acc-2"))
        db.accountDao().replaceCareers("acc-1", listOf(career(11L, "acc-1"), career(12L, "acc-1")))
        db.accountDao().replaceCareers("acc-2", listOf(career(21L, "acc-2")))
        dao.upsertAll(
            listOf(
                event("acc1-career11", careerId = 11L, date = "2026-06-01"),
                event("acc1-career12", careerId = 12L, date = "2026-06-02"),
                event("acc2-career21", careerId = 21L, date = "2026-06-03"),
            )
        )

        dao.deleteForAccount("acc-1")

        assertThat(dao.observeInRange(11L, "2026-01-01", "2026-12-31").first()).isEmpty()
        assertThat(dao.observeInRange(12L, "2026-01-01", "2026-12-31").first()).isEmpty()
        assertThat(dao.observeInRange(21L, "2026-01-01", "2026-12-31").first().map { it.id })
            .containsExactly("acc2-career21")
    }

    @Test
    fun `upsertAll overwrites a row sharing the composite key`() = runTest {
        dao.upsertAll(listOf(event("evt", careerId = 1L, date = "2026-06-10", title = "Old")))

        dao.upsertAll(listOf(event("evt", careerId = 1L, date = "2026-06-10", title = "New")))

        val rows = dao.observeInRange(1L, "2026-06-10", "2026-06-10").first()
        assertThat(rows).hasSize(1)
        assertThat(rows.single().title).isEqualTo("New")
    }

    @Test
    fun `upsertAll keeps rows that share an id across different careers`() = runTest {
        dao.upsertAll(
            listOf(
                event("shared", careerId = 1L, date = "2026-06-10"),
                event("shared", careerId = 2L, date = "2026-06-10"),
            )
        )

        assertThat(dao.observeInRange(1L, "2026-06-10", "2026-06-10").first()).hasSize(1)
        assertThat(dao.observeInRange(2L, "2026-06-10", "2026-06-10").first()).hasSize(1)
    }

    private fun event(
        id: String,
        careerId: Long,
        date: String,
        startTime: String = "10:00",
        source: String = LESSONS,
        title: String = "Evento",
    ) = CalendarEventEntity(
        id = id,
        careerId = careerId,
        source = source,
        discriminator = CalendarEventDiscriminator.LESSON,
        date = date,
        startTime = startTime,
        endTime = "12:00",
        title = title,
        shortLabel = null,
        room = null,
        building = null,
        mapsUrl = null,
        status = "Confirmed",
        notes = null,
        activityCode = null,
        subjectCode = null,
        teachersCsv = null,
        cfu = null,
        examTypeLabel = null,
        bookingPosition = null,
        bookedAt = null,
        cancellableUntil = null,
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
