package it.attendance100.mybicocca.data.local.elearning.badge

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
 * Behaviour coverage for [BadgeDao] against a real in-memory Room database (Robolectric).
 * Exercises the badge cache: newest-first ordering with name as tiebreaker, account and course
 * scoping, and the `replaceForCourse` splice — crucially that the null `courseId` scope (site-wide
 * badges) matches via the `IS :courseId` comparison and that swapping one scope leaves the others
 * intact. No foreign key is declared, so rows insert without a parent account.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BadgeDaoTest {

    private lateinit var db: MyBicoccaDatabase
    private lateinit var dao: BadgeDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MyBicoccaDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.elearningBadgeDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `observeAll orders by issued time descending then name`() = runTest {
        dao.upsert(
            listOf(
                badge(badgeId = 1, name = "Bravo", issuedAtMs = 1_000L),
                badge(badgeId = 2, name = "Alpha", issuedAtMs = 9_000L),
                badge(badgeId = 3, name = "Charlie", issuedAtMs = 9_000L),
            ),
        )

        val ordered = dao.observeAll("acc-1").first()

        assertThat(ordered.map { it.name }).containsExactly("Alpha", "Charlie", "Bravo").inOrder()
    }

    @Test
    fun `observeAll is scoped to the account`() = runTest {
        dao.upsert(
            listOf(
                badge(badgeId = 1, accountId = "acc-1"),
                badge(badgeId = 2, accountId = "acc-2"),
            ),
        )

        assertThat(dao.observeAll("acc-1").first().map { it.badgeId }).containsExactly(1)
    }

    @Test
    fun `observeForCourse returns only that course's badges`() = runTest {
        dao.upsert(
            listOf(
                badge(badgeId = 1, courseId = 100),
                badge(badgeId = 2, courseId = 200),
                badge(badgeId = 3, courseId = null),
            ),
        )

        assertThat(dao.observeForCourse("acc-1", courseId = 100).first().map { it.badgeId })
            .containsExactly(1)
    }

    @Test
    fun `replaceForCourse swaps a course scope and leaves the site-wide and other course scopes intact`() = runTest {
        dao.upsert(
            listOf(
                badge(badgeId = 1, courseId = 100),
                badge(badgeId = 2, courseId = 200),
                badge(badgeId = 3, courseId = null),
            ),
        )

        dao.replaceForCourse("acc-1", courseId = 100, rows = listOf(badge(badgeId = 9, courseId = 100)))

        assertThat(dao.observeForCourse("acc-1", 100).first().map { it.badgeId }).containsExactly(9)
        assertThat(dao.observeForCourse("acc-1", 200).first().map { it.badgeId }).containsExactly(2)
        assertThat(dao.observeAll("acc-1").first().map { it.badgeId }).containsExactly(9, 2, 3)
    }

    @Test
    fun `replaceForCourse with null courseId swaps only the site-wide badges`() = runTest {
        dao.upsert(
            listOf(
                badge(badgeId = 1, courseId = 100),
                badge(badgeId = 2, courseId = null),
                badge(badgeId = 3, courseId = null),
            ),
        )

        dao.replaceForCourse("acc-1", courseId = null, rows = listOf(badge(badgeId = 9, courseId = null)))

        val all = dao.observeAll("acc-1").first()
        assertThat(all.map { it.badgeId }).containsExactly(1, 9)
        assertThat(all.filter { it.courseId == null }.map { it.badgeId }).containsExactly(9)
    }

    @Test
    fun `replaceForCourse with empty list clears the targeted scope`() = runTest {
        dao.upsert(listOf(badge(badgeId = 1, courseId = 100)))

        dao.replaceForCourse("acc-1", courseId = 100, rows = emptyList())

        assertThat(dao.observeForCourse("acc-1", 100).first()).isEmpty()
    }

    @Test
    fun `deleteForAccount empties only the targeted account`() = runTest {
        dao.upsert(
            listOf(
                badge(badgeId = 1, accountId = "acc-1"),
                badge(badgeId = 2, accountId = "acc-2"),
            ),
        )

        dao.deleteForAccount("acc-1")

        assertThat(dao.observeAll("acc-1").first()).isEmpty()
        assertThat(dao.observeAll("acc-2").first()).hasSize(1)
    }

    @Test
    fun `observeAll re-emits after a replace`() = runTest {
        dao.upsert(listOf(badge(badgeId = 1, courseId = 100, name = "Original")))

        dao.observeAll("acc-1").test {
            assertThat(awaitItem().single().name).isEqualTo("Original")

            dao.replaceForCourse("acc-1", courseId = 100, rows = listOf(badge(badgeId = 2, courseId = 100, name = "Fresh")))
            assertThat(awaitItem().single().name).isEqualTo("Fresh")
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun badge(
        badgeId: Int,
        accountId: String = "acc-1",
        courseId: Int? = 100,
        name: String = "Badge $badgeId",
        issuedAtMs: Long? = 1_000L,
    ) = BadgeEntity(
        accountId = accountId,
        badgeId = badgeId,
        name = name,
        description = null,
        imageUrl = null,
        issuedAtMs = issuedAtMs,
        courseId = courseId,
    )
}
