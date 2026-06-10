package it.attendance100.mybicocca.data.local.elearning.assignment

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.local.account.MyBicoccaDatabase
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningJson
import it.attendance100.mybicocca.data.mapper.elearning.SubmissionStatusJson
import it.attendance100.mybicocca.data.mapper.elearning.toEntity
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAssignment
import it.attendance100.mybicocca.domain.model.account.AccountId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Behaviour coverage for [AssignmentDao] against a real in-memory Room database (Robolectric).
 * Exercises the per-course `due_date_ms IS NULL, due_date_ms, name` ordering that floats undated
 * assignments last, the account-wide search stream, the `replaceForCourse` transaction that swaps
 * only one course's rows, and a full round-trip of the JSON [submission_status_json] column built
 * through the production `toEntity` mapper (not a hand-written discriminator) so the persisted
 * envelope is the real serializer output.
 *
 * The table keys on a plain `account_id` String with no foreign key; rows insert with no parent.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AssignmentDaoTest {

    private val account = AccountId("acc-1")

    private lateinit var db: MyBicoccaDatabase
    private lateinit var dao: AssignmentDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MyBicoccaDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.elearningAssignmentDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `observeForCourse orders dated soonest first then undated last by name`() = runTest {
        dao.upsertAll(
            listOf(
                entity(assignmentId = 1, courseId = 5, name = "Bravo", dueDateMs = 2_000L),
                entity(assignmentId = 2, courseId = 5, name = "Alfa", dueDateMs = 1_000L),
                entity(assignmentId = 3, courseId = 5, name = "Zeta", dueDateMs = null),
                entity(assignmentId = 4, courseId = 5, name = "Yankee", dueDateMs = null),
            )
        )

        val rows = dao.observeForCourse("acc-1", 5).first()

        assertThat(rows.map { it.assignmentId }).containsExactly(2, 1, 4, 3).inOrder()
    }

    @Test
    fun `observeForCourse is course scoped`() = runTest {
        dao.upsertAll(
            listOf(
                entity(assignmentId = 1, courseId = 5, dueDateMs = 1_000L),
                entity(assignmentId = 2, courseId = 6, dueDateMs = 1_000L),
            )
        )

        assertThat(dao.observeForCourse("acc-1", 5).first().map { it.assignmentId }).containsExactly(1)
        assertThat(dao.observeForCourse("acc-1", 6).first().map { it.assignmentId }).containsExactly(2)
    }

    @Test
    fun `observeForAccount streams every course's assignments for the search index`() = runTest {
        dao.upsertAll(
            listOf(
                entity(assignmentId = 1, courseId = 5, dueDateMs = 1_000L),
                entity(assignmentId = 2, courseId = 6, dueDateMs = 1_000L),
            )
        )
        dao.upsertAll(listOf(entity(accountId = "acc-2", assignmentId = 9, courseId = 7, dueDateMs = 1_000L)))

        assertThat(dao.observeForAccount("acc-1").first().map { it.assignmentId })
            .containsExactly(1, 2)
    }

    @Test
    fun `observe streams a single assignment and null when absent`() = runTest {
        dao.upsert(entity(assignmentId = 1, courseId = 5, dueDateMs = 1_000L))

        assertThat(dao.observe("acc-1", 1).first()!!.assignmentId).isEqualTo(1)
        assertThat(dao.observe("acc-1", 404).first()).isNull()
    }

    @Test
    fun `replaceForCourse swaps only the target course and re-emits`() = runTest {
        dao.upsertAll(listOf(entity(assignmentId = 99, courseId = 6, dueDateMs = 1_000L)))

        dao.observeForCourse("acc-1", 5).test {
            assertThat(awaitItem()).isEmpty()

            dao.replaceForCourse("acc-1", 5, listOf(entity(assignmentId = 1, courseId = 5, dueDateMs = 1_000L)))
            assertThat(awaitItem().map { it.assignmentId }).containsExactly(1)

            dao.replaceForCourse("acc-1", 5, listOf(entity(assignmentId = 2, courseId = 5, dueDateMs = 1_000L)))
            assertThat(awaitItem().map { it.assignmentId }).containsExactly(2)
            cancelAndIgnoreRemainingEvents()
        }

        assertThat(dao.observeForCourse("acc-1", 6).first().map { it.assignmentId }).containsExactly(99)
    }

    @Test
    fun `deleteForAccount clears only the targeted account`() = runTest {
        dao.upsertAll(listOf(entity(assignmentId = 1, courseId = 5, dueDateMs = 1_000L)))
        dao.upsertAll(listOf(entity(accountId = "acc-2", assignmentId = 2, courseId = 5, dueDateMs = 1_000L)))

        dao.deleteForAccount("acc-1")

        assertThat(dao.observeForAccount("acc-1").first()).isEmpty()
        assertThat(dao.observeForAccount("acc-2").first().map { it.assignmentId }).containsExactly(2)
    }

    @Test
    fun `submission status json column round-trips through the real serializer`() = runTest {
        val status = SubmissionStatusJson.Graded(
            submittedAtMs = 5_000_000L,
            grade = 27.0,
            maxGrade = 30.0,
            feedback = "ottimo",
        )
        dao.upsert(entity(assignmentId = 1, courseId = 5, dueDateMs = 1_000L, status = status))

        val stored = dao.observe("acc-1", 1).first()!!
        val decoded = ElearningJson.decodeFromString(SubmissionStatusJson.serializer(), stored.submissionStatusJson)

        assertThat(decoded).isInstanceOf(SubmissionStatusJson.Graded::class.java)
        decoded as SubmissionStatusJson.Graded
        assertThat(decoded.grade).isEqualTo(27.0)
        assertThat(decoded.feedback).isEqualTo("ottimo")
        assertThat(stored.submissionStatusJson)
            .isEqualTo(ElearningJson.encodeToString(SubmissionStatusJson.serializer(), status))
    }

    private fun entity(
        accountId: String = "acc-1",
        assignmentId: Int,
        courseId: Int,
        name: String = "Compito $assignmentId",
        dueDateMs: Long?,
        status: SubmissionStatusJson = SubmissionStatusJson.NotSubmitted,
    ): AssignmentEntity {
        val dto = ElearningAssignment(
            id = assignmentId,
            courseModuleId = assignmentId + 1000,
            courseId = courseId,
            name = name,
            introduction = "<p>intro</p>",
            dueDateTimestamp = dueDateMs?.let { it / 1000L },
            submissionDraftsEnabled = 1,
            requireSubmissionStatement = 0,
        )
        return dto.toEntity(AccountId(accountId), status, wsToken = "WSTOKEN")
    }
}
