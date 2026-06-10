package it.attendance100.mybicocca.data.local.elearning.grade

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
 * Behaviour coverage for [GradeDao] against a real in-memory Room database (Robolectric).
 * Exercises the per-course grade items in `sort_order` gradebook order, the cross-course overview
 * sorted by `course_name`, the two `replace*` transactions (per-course item swap leaves other
 * courses intact; whole-account overview swap), and `clearAllForAccount` wiping both tables for
 * one account while sparing another.
 *
 * Both tables key on a plain `account_id` String with no foreign key, so rows insert with no
 * parent account.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GradeDaoTest {

    private lateinit var db: MyBicoccaDatabase
    private lateinit var dao: GradeDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MyBicoccaDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.elearningGradeDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `observeCourseGradeItems returns items in gradebook sort order`() = runTest {
        dao.upsertItems(
            listOf(
                item("acc-1", courseId = 5, itemId = 30L, sortOrder = 2),
                item("acc-1", courseId = 5, itemId = 10L, sortOrder = 0),
                item("acc-1", courseId = 5, itemId = 20L, sortOrder = 1),
            )
        )

        val rows = dao.observeCourseGradeItems("acc-1", 5).first()

        assertThat(rows.map { it.itemId }).containsExactly(10L, 20L, 30L).inOrder()
    }

    @Test
    fun `observeCourseGradeItems is account and course scoped`() = runTest {
        dao.upsertItems(
            listOf(
                item("acc-1", courseId = 5, itemId = 1L, sortOrder = 0),
                item("acc-1", courseId = 6, itemId = 2L, sortOrder = 0),
                item("acc-2", courseId = 5, itemId = 3L, sortOrder = 0),
            )
        )

        assertThat(dao.observeCourseGradeItems("acc-1", 5).first().map { it.itemId }).containsExactly(1L)
    }

    @Test
    fun `observeAllCourseGrades sorts overviews by course name`() = runTest {
        dao.upsertOverviews(
            listOf(
                overview("acc-1", courseId = 1, courseName = "Sistemi"),
                overview("acc-1", courseId = 2, courseName = "Analisi"),
                overview("acc-1", courseId = 3, courseName = "Programmazione"),
            )
        )

        val rows = dao.observeAllCourseGrades("acc-1").first()

        assertThat(rows.map { it.courseName }).containsExactly("Analisi", "Programmazione", "Sistemi").inOrder()
    }

    @Test
    fun `replaceCourseGradeItems swaps only the target course and re-emits`() = runTest {
        dao.upsertItems(listOf(item("acc-1", courseId = 6, itemId = 99L, sortOrder = 0)))

        dao.observeCourseGradeItems("acc-1", 5).test {
            assertThat(awaitItem()).isEmpty()

            dao.replaceCourseGradeItems("acc-1", 5, listOf(item("acc-1", courseId = 5, itemId = 1L, sortOrder = 0)))
            assertThat(awaitItem().map { it.itemId }).containsExactly(1L)

            dao.replaceCourseGradeItems("acc-1", 5, listOf(item("acc-1", courseId = 5, itemId = 2L, sortOrder = 0)))
            assertThat(awaitItem().map { it.itemId }).containsExactly(2L)
            cancelAndIgnoreRemainingEvents()
        }

        assertThat(dao.observeCourseGradeItems("acc-1", 6).first().map { it.itemId }).containsExactly(99L)
    }

    @Test
    fun `replaceAllCourseGrades swaps the whole account overview set and re-emits`() = runTest {
        dao.observeAllCourseGrades("acc-1").test {
            assertThat(awaitItem()).isEmpty()

            dao.replaceAllCourseGrades("acc-1", listOf(overview("acc-1", courseId = 1, courseName = "Alfa")))
            assertThat(awaitItem().map { it.courseId }).containsExactly(1)

            dao.replaceAllCourseGrades(
                "acc-1",
                listOf(
                    overview("acc-1", courseId = 2, courseName = "Beta"),
                    overview("acc-1", courseId = 3, courseName = "Gamma"),
                ),
            )
            assertThat(awaitItem().map { it.courseId }).containsExactly(2, 3)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `replaceAllCourseGrades leaves another account's overviews intact`() = runTest {
        dao.upsertOverviews(listOf(overview("acc-2", courseId = 9, courseName = "Altro")))

        dao.replaceAllCourseGrades("acc-1", listOf(overview("acc-1", courseId = 1, courseName = "Alfa")))

        assertThat(dao.observeAllCourseGrades("acc-2").first().map { it.courseId }).containsExactly(9)
    }

    @Test
    fun `clearAllForAccount wipes items and overviews for the account but spares other accounts`() = runTest {
        dao.upsertItems(listOf(item("acc-1", courseId = 5, itemId = 1L, sortOrder = 0)))
        dao.upsertOverviews(listOf(overview("acc-1", courseId = 5, courseName = "Mio")))
        dao.upsertItems(listOf(item("acc-2", courseId = 5, itemId = 2L, sortOrder = 0)))
        dao.upsertOverviews(listOf(overview("acc-2", courseId = 5, courseName = "Altro")))

        dao.clearAllForAccount("acc-1")

        assertThat(dao.observeCourseGradeItems("acc-1", 5).first()).isEmpty()
        assertThat(dao.observeAllCourseGrades("acc-1").first()).isEmpty()
        assertThat(dao.observeCourseGradeItems("acc-2", 5).first().map { it.itemId }).containsExactly(2L)
        assertThat(dao.observeAllCourseGrades("acc-2").first().map { it.courseId }).containsExactly(5)
    }

    private fun item(
        accountId: String,
        courseId: Int,
        itemId: Long,
        sortOrder: Int,
    ) = GradeItemEntity(
        accountId = accountId,
        courseId = courseId,
        itemId = itemId,
        name = "Voce $itemId",
        typeRaw = "mod",
        activityType = "assign",
        grade = 27.0,
        maxGrade = 30.0,
        percentage = 90.0,
        gradeFormatted = "27,00",
        feedback = null,
        gradedAtMs = 1_000L,
        sortOrder = sortOrder,
    )

    private fun overview(
        accountId: String,
        courseId: Int,
        courseName: String,
    ) = CourseGradeOverviewEntity(
        accountId = accountId,
        courseId = courseId,
        courseName = courseName,
        grade = 28.0,
        maxGrade = 30.0,
        gradeFormatted = "28,00",
    )
}
