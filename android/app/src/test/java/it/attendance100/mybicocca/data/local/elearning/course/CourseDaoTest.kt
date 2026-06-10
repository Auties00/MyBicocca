package it.attendance100.mybicocca.data.local.elearning.course

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
 * Behaviour coverage for [CourseDao] against a real in-memory Room database (Robolectric).
 * Exercises the account-scoped enrolled list and its `sort_order, full_name` ordering, the
 * device-local favourite/hidden flag UPDATEs that survive a refresh, the structure-replace
 * transactions that swap only one course's sections+modules and one course's staff while leaving
 * other courses intact, the whole-account enrolled swap, completion upserts (never replaced), and
 * the full sign-out wipe.
 *
 * None of the course tables carry a foreign key; every row is keyed by a plain `account_id` String,
 * so rows insert directly with no parent account.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CourseDaoTest {

    private lateinit var db: MyBicoccaDatabase
    private lateinit var dao: CourseDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MyBicoccaDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.elearningCourseDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `observeEnrolled orders by sort order then full name and is account scoped`() = runTest {
        dao.upsertEnrolled(
            listOf(
                enrolled("acc-1", courseId = 1, fullName = "Zeta", sortOrder = 1),
                enrolled("acc-1", courseId = 2, fullName = "Alfa", sortOrder = 0),
                enrolled("acc-1", courseId = 3, fullName = "Beta", sortOrder = 0),
                enrolled("acc-2", courseId = 9, fullName = "Other", sortOrder = 0),
            )
        )

        val rows = dao.observeEnrolled("acc-1").first()

        assertThat(rows.map { it.courseId }).containsExactly(2, 3, 1).inOrder()
    }

    @Test
    fun `replaceEnrolled swaps the whole account list and re-emits`() = runTest {
        dao.observeEnrolled("acc-1").test {
            assertThat(awaitItem()).isEmpty()

            dao.replaceEnrolled("acc-1", listOf(enrolled("acc-1", courseId = 1, sortOrder = 0)))
            assertThat(awaitItem().map { it.courseId }).containsExactly(1)

            dao.replaceEnrolled(
                "acc-1",
                listOf(enrolled("acc-1", courseId = 5, sortOrder = 0), enrolled("acc-1", courseId = 6, sortOrder = 1)),
            )
            assertThat(awaitItem().map { it.courseId }).containsExactly(5, 6).inOrder()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `replaceEnrolled with an empty list clears only the targeted account`() = runTest {
        dao.upsertEnrolled(listOf(enrolled("acc-1", courseId = 1, sortOrder = 0)))
        dao.upsertEnrolled(listOf(enrolled("acc-2", courseId = 2, sortOrder = 0)))

        dao.replaceEnrolled("acc-1", emptyList())

        assertThat(dao.observeEnrolled("acc-1").first()).isEmpty()
        assertThat(dao.observeEnrolled("acc-2").first().map { it.courseId }).containsExactly(2)
    }

    @Test
    fun `setFavourite and setHidden flip the device-local flags for the target course only`() = runTest {
        dao.upsertEnrolled(
            listOf(
                enrolled("acc-1", courseId = 1, sortOrder = 0),
                enrolled("acc-1", courseId = 2, sortOrder = 1),
            )
        )

        dao.setFavourite("acc-1", courseId = 1, favourite = true)
        dao.setHidden("acc-1", courseId = 2, hidden = true)

        val byId = dao.observeEnrolled("acc-1").first().associateBy { it.courseId }
        assertThat(byId.getValue(1).isFavourite).isTrue()
        assertThat(byId.getValue(1).hidden).isFalse()
        assertThat(byId.getValue(2).hidden).isTrue()
        assertThat(byId.getValue(2).isFavourite).isFalse()
    }

    @Test
    fun `observeEnrolledOne streams a single course and null when absent`() = runTest {
        dao.upsertEnrolled(listOf(enrolled("acc-1", courseId = 1, sortOrder = 0)))

        assertThat(dao.observeEnrolledOne("acc-1", 1).first()!!.courseId).isEqualTo(1)
        assertThat(dao.observeEnrolledOne("acc-1", 999).first()).isNull()
    }

    @Test
    fun `observeSections orders by section number`() = runTest {
        dao.upsertSections(
            listOf(
                section("acc-1", courseId = 1, sectionId = 30, sectionNumber = 2),
                section("acc-1", courseId = 1, sectionId = 10, sectionNumber = 0),
                section("acc-1", courseId = 1, sectionId = 20, sectionNumber = 1),
            )
        )

        val rows = dao.observeSections("acc-1", 1).first()

        assertThat(rows.map { it.sectionId }).containsExactly(10, 20, 30).inOrder()
    }

    @Test
    fun `observeModules orders by section id then sort order`() = runTest {
        dao.upsertModules(
            listOf(
                module("acc-1", courseId = 1, cmId = 1, sectionId = 20, sortOrder = 0),
                module("acc-1", courseId = 1, cmId = 2, sectionId = 10, sortOrder = 1),
                module("acc-1", courseId = 1, cmId = 3, sectionId = 10, sortOrder = 0),
            )
        )

        val rows = dao.observeModules("acc-1", 1).first()

        assertThat(rows.map { it.cmId }).containsExactly(3, 2, 1).inOrder()
    }

    @Test
    fun `replaceCourseStructure swaps only the target course leaving other courses intact`() = runTest {
        dao.upsertSections(listOf(section("acc-1", courseId = 1, sectionId = 10, sectionNumber = 0)))
        dao.upsertModules(listOf(module("acc-1", courseId = 1, cmId = 1, sectionId = 10, sortOrder = 0)))
        dao.upsertSections(listOf(section("acc-1", courseId = 2, sectionId = 99, sectionNumber = 0)))
        dao.upsertModules(listOf(module("acc-1", courseId = 2, cmId = 99, sectionId = 99, sortOrder = 0)))

        dao.replaceCourseStructure(
            "acc-1",
            courseId = 1,
            sections = listOf(section("acc-1", courseId = 1, sectionId = 50, sectionNumber = 0)),
            modules = listOf(module("acc-1", courseId = 1, cmId = 50, sectionId = 50, sortOrder = 0)),
        )

        assertThat(dao.observeSections("acc-1", 1).first().map { it.sectionId }).containsExactly(50)
        assertThat(dao.observeModules("acc-1", 1).first().map { it.cmId }).containsExactly(50)
        assertThat(dao.observeSections("acc-1", 2).first().map { it.sectionId }).containsExactly(99)
        assertThat(dao.observeModules("acc-1", 2).first().map { it.cmId }).containsExactly(99)
    }

    @Test
    fun `replaceCourseStaff orders by row index and swaps only the target course`() = runTest {
        dao.upsertStaff(listOf(staff("acc-1", courseId = 2, rowIndex = 0, fullName = "Keep")))

        dao.replaceCourseStaff(
            "acc-1",
            courseId = 1,
            staff = listOf(
                staff("acc-1", courseId = 1, rowIndex = 1, fullName = "B"),
                staff("acc-1", courseId = 1, rowIndex = 0, fullName = "A"),
            ),
        )

        assertThat(dao.observeStaff("acc-1", 1).first().map { it.fullName })
            .containsExactly("A", "B").inOrder()
        assertThat(dao.observeStaff("acc-1", 2).first().map { it.fullName }).containsExactly("Keep")
    }

    @Test
    fun `upsertSyllabus round-trips and deleteSyllabusForCourse removes only the target course`() = runTest {
        dao.upsertSyllabus(syllabus("acc-1", courseId = 1))
        dao.upsertSyllabus(syllabus("acc-1", courseId = 2))

        dao.deleteSyllabusForCourse("acc-1", 1)

        assertThat(dao.observeSyllabus("acc-1", 1).first()).isNull()
        assertThat(dao.observeSyllabus("acc-1", 2).first()!!.courseId).isEqualTo(2)
    }

    @Test
    fun `upsertCompletion overwrites the same cm and never erases sibling rows`() = runTest {
        dao.upsertCompletionAll(
            listOf(
                completion("acc-1", courseId = 1, cmId = 10, completed = false),
                completion("acc-1", courseId = 1, cmId = 20, completed = false),
            )
        )

        dao.upsertCompletion(completion("acc-1", courseId = 1, cmId = 10, completed = true))

        val byCm = dao.observeCompletion("acc-1", 1).first().associateBy { it.cmId }
        assertThat(byCm.getValue(10).isCompleted).isTrue()
        assertThat(byCm.getValue(20).isCompleted).isFalse()
    }

    @Test
    fun `clearAllForAccount wipes every course table for the account but spares other accounts`() = runTest {
        dao.upsertEnrolled(listOf(enrolled("acc-1", courseId = 1, sortOrder = 0)))
        dao.upsertSections(listOf(section("acc-1", courseId = 1, sectionId = 10, sectionNumber = 0)))
        dao.upsertModules(listOf(module("acc-1", courseId = 1, cmId = 1, sectionId = 10, sortOrder = 0)))
        dao.upsertStaff(listOf(staff("acc-1", courseId = 1, rowIndex = 0, fullName = "T")))
        dao.upsertSyllabus(syllabus("acc-1", courseId = 1))
        dao.upsertCompletion(completion("acc-1", courseId = 1, cmId = 1, completed = true))
        dao.upsertEnrolled(listOf(enrolled("acc-2", courseId = 2, sortOrder = 0)))

        dao.clearAllForAccount("acc-1")

        assertThat(dao.observeEnrolled("acc-1").first()).isEmpty()
        assertThat(dao.observeSections("acc-1", 1).first()).isEmpty()
        assertThat(dao.observeModules("acc-1", 1).first()).isEmpty()
        assertThat(dao.observeStaff("acc-1", 1).first()).isEmpty()
        assertThat(dao.observeSyllabus("acc-1", 1).first()).isNull()
        assertThat(dao.observeCompletion("acc-1", 1).first()).isEmpty()
        assertThat(dao.observeEnrolled("acc-2").first().map { it.courseId }).containsExactly(2)
    }

    private fun enrolled(
        accountId: String,
        courseId: Int,
        fullName: String = "Course $courseId",
        sortOrder: Int,
    ) = EnrolledCourseEntity(
        accountId = accountId,
        courseId = courseId,
        shortName = "C$courseId",
        fullName = fullName,
        displayName = fullName,
        idNumber = "2024-1-EC$courseId",
        summary = null,
        courseImageUrl = null,
        format = "topics",
        language = "it",
        categoryId = null,
        progress = null,
        completed = false,
        completionEnabled = false,
        startDateMs = null,
        endDateMs = null,
        lastAccessMs = null,
        isFavourite = false,
        hidden = false,
        sortOrder = sortOrder,
    )

    private fun section(
        accountId: String,
        courseId: Int,
        sectionId: Int,
        sectionNumber: Int,
    ) = CourseSectionEntity(
        accountId = accountId,
        courseId = courseId,
        sectionId = sectionId,
        sectionNumber = sectionNumber,
        name = "Section $sectionNumber",
        summary = null,
        visible = true,
        component = null,
        itemId = null,
    )

    private fun module(
        accountId: String,
        courseId: Int,
        cmId: Int,
        sectionId: Int,
        sortOrder: Int,
    ) = CourseModuleEntity(
        accountId = accountId,
        courseId = courseId,
        cmId = cmId,
        sectionId = sectionId,
        sortOrder = sortOrder,
        instanceId = null,
        name = "Module $cmId",
        modName = "resource",
        typeLabel = null,
        description = null,
        url = null,
        iconUrl = null,
        visible = true,
        accessible = true,
        availabilityInfo = null,
        onCoursePage = true,
        indent = 0,
        afterLink = null,
        linkedSectionId = null,
        datesJson = null,
        contentsJson = null,
    )

    private fun staff(
        accountId: String,
        courseId: Int,
        rowIndex: Int,
        fullName: String,
    ) = CourseStaffEntity(
        accountId = accountId,
        courseId = courseId,
        rowIndex = rowIndex,
        userId = null,
        fullName = fullName,
        roleRaw = "Docente",
        initials = null,
        email = null,
        profileUrl = null,
    )

    private fun syllabus(accountId: String, courseId: Int) = CourseSyllabusEntity(
        accountId = accountId,
        courseId = courseId,
        language = "it",
        exportPdfUrl = null,
        fieldsJson = "{}",
    )

    private fun completion(
        accountId: String,
        courseId: Int,
        cmId: Int,
        completed: Boolean,
    ) = ActivityCompletionEntity(
        accountId = accountId,
        courseId = courseId,
        cmId = cmId,
        isCompleted = completed,
        completedAtMs = if (completed) 1_000L else null,
        isManual = true,
        isAutomatic = false,
        isTracked = true,
    )
}
