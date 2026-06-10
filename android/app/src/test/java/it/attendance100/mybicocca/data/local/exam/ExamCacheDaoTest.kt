package it.attendance100.mybicocca.data.local.exam

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
 * Behaviour coverage for [ExamCacheDao] against a real in-memory Room database (Robolectric).
 * The exam mirrors (booked exams, future calls, published results) are each replaced wholesale
 * per career: the splice-replace transaction must wipe only the targeted career slice and
 * re-insert the fresh rows, and the `ORDER BY cache_order` reads must return them in the stored
 * order regardless of insert order. The three cache tables are independent, plain `career_id`
 * Long columns with no foreign keys, so no parent account/career rows are needed.
 *
 * Wave 2 (Android-runtime) test: Robolectric drives the actual Room-generated `_Impl`, pinned to
 * a Robolectric-supported SDK because the module compiles against a newer one.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExamCacheDaoTest {

    private lateinit var db: MyBicoccaDatabase
    private lateinit var dao: ExamCacheDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MyBicoccaDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.examCacheDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `replaceBookings stores rows and getBookings returns them ordered by cache order`() = runTest {
        dao.replaceBookings(
            careerId = 1L,
            rows = listOf(
                booking(1L, callId = 30, cacheOrder = 2),
                booking(1L, callId = 10, cacheOrder = 0),
                booking(1L, callId = 20, cacheOrder = 1),
            ),
        )

        val stored = dao.getBookings(1L)

        assertThat(stored.map { it.callId }).containsExactly(10, 20, 30).inOrder()
        assertThat(stored.map { it.cacheOrder }).containsExactly(0, 1, 2).inOrder()
    }

    @Test
    fun `replaceBookings wipes the prior career slice before inserting the fresh rows`() = runTest {
        dao.replaceBookings(1L, listOf(booking(1L, callId = 10, cacheOrder = 0), booking(1L, callId = 11, cacheOrder = 1)))

        dao.replaceBookings(1L, listOf(booking(1L, callId = 99, cacheOrder = 0)))

        val stored = dao.getBookings(1L)
        assertThat(stored.map { it.callId }).containsExactly(99)
    }

    @Test
    fun `replaceBookings leaves other careers untouched`() = runTest {
        dao.replaceBookings(1L, listOf(booking(1L, callId = 10, cacheOrder = 0)))
        dao.replaceBookings(2L, listOf(booking(2L, callId = 20, cacheOrder = 0)))

        dao.replaceBookings(1L, listOf(booking(1L, callId = 11, cacheOrder = 0)))

        assertThat(dao.getBookings(1L).map { it.callId }).containsExactly(11)
        assertThat(dao.getBookings(2L).map { it.callId }).containsExactly(20)
    }

    @Test
    fun `getBookings round-trips every scalar field`() = runTest {
        val row = booking(1L, callId = 5, cacheOrder = 0).copy(
            applicationListId = 777L,
            studentId = 42L,
            activityChoiceId = 99L,
            activityDescription = "Analisi Matematica I",
            examCallDescription = "Appello straordinario",
            examType = "S",
            callType = "P",
            examDateTime = "2026-01-15T09:30:00",
            classroomDescription = "Aula U6-01",
            buildingDescription = "U6",
            credits = 8.0f,
            examModeDescription = "Scritto",
            position = 3,
            bookingDate = "2025-12-20",
            cancellableUntil = "2026-01-13",
            studentNote = "nessuna",
            gradeKind = "numeric",
            gradeValue = 28,
            outcomePublished = true,
            publishedNote = "ottimo",
        )

        dao.replaceBookings(1L, listOf(row))

        assertThat(dao.getBookings(1L).single()).isEqualTo(row)
    }

    @Test
    fun `replaceCalls replaces only the targeted career and preserves order`() = runTest {
        dao.replaceCalls(1L, listOf(call(1L, callId = 2, cacheOrder = 1), call(1L, callId = 1, cacheOrder = 0)))
        dao.replaceCalls(2L, listOf(call(2L, callId = 9, cacheOrder = 0)))

        dao.replaceCalls(1L, listOf(call(1L, callId = 5, cacheOrder = 0)))

        assertThat(dao.getCalls(1L).map { it.callId }).containsExactly(5)
        assertThat(dao.getCalls(2L).map { it.callId }).containsExactly(9)
    }

    @Test
    fun `getCalls returns rows ordered by cache order`() = runTest {
        dao.replaceCalls(
            careerId = 7L,
            rows = listOf(
                call(7L, callId = 3, cacheOrder = 2),
                call(7L, callId = 1, cacheOrder = 0),
                call(7L, callId = 2, cacheOrder = 1),
            ),
        )

        assertThat(dao.getCalls(7L).map { it.callId }).containsExactly(1, 2, 3).inOrder()
    }

    @Test
    fun `replaceResults replaces only the targeted career and preserves order`() = runTest {
        dao.replaceResults(1L, listOf(result(1L, callId = 1, cacheOrder = 0), result(1L, callId = 2, cacheOrder = 1)))
        dao.replaceResults(2L, listOf(result(2L, callId = 8, cacheOrder = 0)))

        dao.replaceResults(1L, listOf(result(1L, callId = 4, cacheOrder = 1), result(1L, callId = 3, cacheOrder = 0)))

        assertThat(dao.getResults(1L).map { it.callId }).containsExactly(3, 4).inOrder()
        assertThat(dao.getResults(2L).map { it.callId }).containsExactly(8)
    }

    @Test
    fun `getResults returns empty for an unknown career`() = runTest {
        dao.replaceResults(1L, listOf(result(1L, callId = 1, cacheOrder = 0)))

        assertThat(dao.getResults(999L)).isEmpty()
    }

    private fun booking(careerId: Long, callId: Int, cacheOrder: Int) = BookedExamEntity(
        careerId = careerId,
        courseOfStudyId = 1000L,
        activityId = 2000L,
        callId = callId,
        cacheOrder = cacheOrder,
        applicationListId = null,
        studentId = null,
        activityChoiceId = null,
        activityDescription = null,
        examCallDescription = null,
        examType = "S",
        callType = "P",
        examDateTime = null,
        classroomDescription = null,
        buildingDescription = null,
        credits = null,
        examModeDescription = null,
        position = null,
        bookingDate = null,
        cancellableUntil = null,
        studentNote = null,
        gradeKind = "unknown",
        gradeValue = null,
        outcomePublished = false,
        publishedNote = null,
    )

    private fun call(careerId: Long, callId: Int, cacheOrder: Int) = ExamCallEntity(
        careerId = careerId,
        courseOfStudyId = 1000L,
        activityId = 2000L,
        callId = callId,
        cacheOrder = cacheOrder,
        examCallId = null,
        activityChoiceId = null,
        activityCode = null,
        activityDescription = null,
        courseOfStudyDescription = null,
        callDescription = null,
        callDate = null,
        callTime = null,
        windowOpensAt = null,
        windowClosesAt = null,
        enrolledNumber = null,
        state = null,
        stateDescription = null,
        callType = "P",
        examType = "S",
        isReserved = false,
        matId = null,
        notes = null,
        presidentId = null,
        presidentName = null,
        presidentSurname = null,
        bookingTypeDescription = null,
    )

    private fun result(careerId: Long, callId: Int, cacheOrder: Int) = ExamResultEntity(
        careerId = careerId,
        courseOfStudyId = 1000L,
        activityId = 2000L,
        callId = callId,
        cacheOrder = cacheOrder,
        applicationListId = null,
        publicationId = null,
        activityDescription = null,
        examDateTime = null,
        gradeKind = "numeric",
        gradeValue = 30,
        acknowledgment = "NotRequired",
        publishedNote = null,
        acknowledgmentDeadline = null,
    )
}
