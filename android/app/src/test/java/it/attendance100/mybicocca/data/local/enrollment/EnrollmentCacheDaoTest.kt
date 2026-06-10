package it.attendance100.mybicocca.data.local.enrollment

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
 * Behaviour coverage for [EnrollmentCacheDao] against a real in-memory Room database (Robolectric).
 * The annual-enrollment history is the offline mirror of a career's year-by-year timeline, replaced
 * wholesale per career: the splice-replace transaction must wipe only the targeted career slice and
 * re-insert the fresh rows, and the `ORDER BY cache_order` read must return them in the stored
 * most-recent-first order regardless of insert order. The table keys on a plain `career_id` Long
 * with no foreign key, so no parent account/career rows are required.
 *
 * Wave 2 (Android-runtime) test: Robolectric drives the actual Room-generated `_Impl`, pinned to
 * a Robolectric-supported SDK because the module compiles against a newer one.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class EnrollmentCacheDaoTest {

    private lateinit var db: MyBicoccaDatabase
    private lateinit var dao: EnrollmentCacheDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MyBicoccaDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.enrollmentCacheDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `replaceYears stores rows and getYears returns them ordered by cache order`() = runTest {
        dao.replaceYears(
            careerId = 1L,
            rows = listOf(
                enrollment(1L, enrollmentId = 2022L, cacheOrder = 2),
                enrollment(1L, enrollmentId = 2024L, cacheOrder = 0),
                enrollment(1L, enrollmentId = 2023L, cacheOrder = 1),
            ),
        )

        val stored = dao.getYears(1L)

        assertThat(stored.map { it.enrollmentId }).containsExactly(2024L, 2023L, 2022L).inOrder()
        assertThat(stored.map { it.cacheOrder }).containsExactly(0, 1, 2).inOrder()
    }

    @Test
    fun `replaceYears wipes the prior career slice before inserting the fresh rows`() = runTest {
        dao.replaceYears(
            careerId = 1L,
            rows = listOf(
                enrollment(1L, enrollmentId = 2022L, cacheOrder = 0),
                enrollment(1L, enrollmentId = 2023L, cacheOrder = 1),
            ),
        )

        dao.replaceYears(1L, listOf(enrollment(1L, enrollmentId = 2024L, cacheOrder = 0)))

        assertThat(dao.getYears(1L).map { it.enrollmentId }).containsExactly(2024L)
    }

    @Test
    fun `replaceYears leaves other careers untouched`() = runTest {
        dao.replaceYears(1L, listOf(enrollment(1L, enrollmentId = 2024L, cacheOrder = 0)))
        dao.replaceYears(2L, listOf(enrollment(2L, enrollmentId = 2020L, cacheOrder = 0)))

        dao.replaceYears(1L, listOf(enrollment(1L, enrollmentId = 2025L, cacheOrder = 0)))

        assertThat(dao.getYears(1L).map { it.enrollmentId }).containsExactly(2025L)
        assertThat(dao.getYears(2L).map { it.enrollmentId }).containsExactly(2020L)
    }

    @Test
    fun `getYears is empty for an unknown career`() = runTest {
        dao.replaceYears(1L, listOf(enrollment(1L, enrollmentId = 2024L, cacheOrder = 0)))

        assertThat(dao.getYears(999L)).isEmpty()
    }

    @Test
    fun `getYears round-trips the flattened part-time and suspension columns`() = runTest {
        val row = enrollment(1L, enrollmentId = 2024L, cacheOrder = 0).copy(
            type = "FullTime",
            status = "Active",
            hasPartTime = true,
            partTimeCredits = 30,
            partTimeExtraCredits = 6,
            partTimeLocked = true,
            hasSuspension = true,
            suspensionReasonCode = "MAT",
        )

        dao.replaceYears(1L, listOf(row))

        assertThat(dao.getYears(1L).single()).isEqualTo(row)
    }

    private fun enrollment(careerId: Long, enrollmentId: Long, cacheOrder: Int) = AnnualEnrollmentEntity(
        careerId = careerId,
        enrollmentId = enrollmentId,
        cacheOrder = cacheOrder,
        academicYear = 2024,
        courseYear = 1,
        outOfCourseYears = 0,
        type = "FullTime",
        typeDescription = null,
        status = "Active",
        statusReasonCode = null,
        conditional = false,
        reconstructed = false,
        hasPartTime = false,
        partTimeCredits = null,
        partTimeExtraCredits = null,
        partTimeLocked = null,
        hasSuspension = false,
        suspensionReasonCode = null,
        awaitingDegree = false,
        degreeAwardDate = null,
        studentTypeDescription = null,
        exemptionDescription = null,
        incomeBandId = null,
        canteenBandId = null,
        meritBandId = null,
        meritNote = null,
        enrollmentNote = null,
        disabilityPercentage = null,
        disabilityTypeDescription = null,
        courseDescription = null,
        courseTypeDescription = null,
        degreeClassCode = null,
        degreeClassDescription = null,
        orientationDescription = null,
        addressDescription = null,
        studyOrderDescription = null,
        minimumCredits = null,
        courseDuration = null,
        teachingLanguage = null,
        regulationCode = null,
        universityDescription = null,
        siteDescription = null,
        enrollmentDate = null,
        insertionDate = null,
        modificationDate = null,
    )
}
