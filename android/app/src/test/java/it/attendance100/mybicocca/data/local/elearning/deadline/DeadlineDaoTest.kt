package it.attendance100.mybicocca.data.local.elearning.deadline

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
 * Behaviour coverage for [DeadlineDao] against a real in-memory Room database (Robolectric).
 * Exercises the soonest-first `due_at_ms` ordering, account scoping of the observed stream, the
 * upsert overwrite of a same-key (account_id, event_id) row, and the `replaceForAccount`
 * transaction that swaps only one account's deadlines while leaving another account's untouched.
 *
 * The table has no foreign key; rows key on a plain `account_id` String, so they insert with no
 * parent account row.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DeadlineDaoTest {

    private lateinit var db: MyBicoccaDatabase
    private lateinit var dao: DeadlineDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MyBicoccaDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.elearningDeadlineDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `observeForAccount returns the account's deadlines soonest first`() = runTest {
        dao.upsert(
            listOf(
                deadline("acc-1", eventId = 1, dueAtMs = 3_000L),
                deadline("acc-1", eventId = 2, dueAtMs = 1_000L),
                deadline("acc-1", eventId = 3, dueAtMs = 2_000L),
            )
        )

        val rows = dao.observeForAccount("acc-1").first()

        assertThat(rows.map { it.eventId }).containsExactly(2, 3, 1).inOrder()
    }

    @Test
    fun `observeForAccount excludes other accounts`() = runTest {
        dao.upsert(listOf(deadline("acc-1", eventId = 1, dueAtMs = 1_000L)))
        dao.upsert(listOf(deadline("acc-2", eventId = 2, dueAtMs = 1_000L)))

        assertThat(dao.observeForAccount("acc-1").first().map { it.eventId }).containsExactly(1)
        assertThat(dao.observeForAccount("acc-2").first().map { it.eventId }).containsExactly(2)
    }

    @Test
    fun `upsert overwrites a deadline sharing the account and event id`() = runTest {
        dao.upsert(listOf(deadline("acc-1", eventId = 1, dueAtMs = 1_000L, title = "Vecchio")))

        dao.upsert(listOf(deadline("acc-1", eventId = 1, dueAtMs = 5_000L, title = "Nuovo")))

        val rows = dao.observeForAccount("acc-1").first()
        assertThat(rows).hasSize(1)
        assertThat(rows.single().title).isEqualTo("Nuovo")
        assertThat(rows.single().dueAtMs).isEqualTo(5_000L)
    }

    @Test
    fun `replaceForAccount swaps the whole account slice and re-emits`() = runTest {
        dao.observeForAccount("acc-1").test {
            assertThat(awaitItem()).isEmpty()

            dao.replaceForAccount(
                "acc-1",
                listOf(deadline("acc-1", eventId = 1, dueAtMs = 1_000L, kind = DeadlineEntity.Kind.ASSIGNMENT)),
            )
            assertThat(awaitItem().map { it.eventId }).containsExactly(1)

            dao.replaceForAccount(
                "acc-1",
                listOf(deadline("acc-1", eventId = 2, dueAtMs = 2_000L, kind = DeadlineEntity.Kind.QUIZ)),
            )
            assertThat(awaitItem().single().eventId).isEqualTo(2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `replaceForAccount leaves another account's deadlines intact`() = runTest {
        dao.upsert(listOf(deadline("acc-2", eventId = 99, dueAtMs = 1_000L)))

        dao.replaceForAccount("acc-1", listOf(deadline("acc-1", eventId = 1, dueAtMs = 1_000L)))

        assertThat(dao.observeForAccount("acc-2").first().map { it.eventId }).containsExactly(99)
    }

    @Test
    fun `replaceForAccount with an empty list clears the account`() = runTest {
        dao.upsert(listOf(deadline("acc-1", eventId = 1, dueAtMs = 1_000L)))

        dao.replaceForAccount("acc-1", emptyList())

        assertThat(dao.observeForAccount("acc-1").first()).isEmpty()
    }

    private fun deadline(
        accountId: String,
        eventId: Int,
        dueAtMs: Long,
        courseId: Int = 5,
        kind: String = DeadlineEntity.Kind.ASSIGNMENT,
        instanceId: Int = eventId * 10,
        title: String = "Scadenza $eventId",
    ) = DeadlineEntity(
        accountId = accountId,
        eventId = eventId,
        courseId = courseId,
        kind = kind,
        instanceId = instanceId,
        title = title,
        dueAtMs = dueAtMs,
    )
}
