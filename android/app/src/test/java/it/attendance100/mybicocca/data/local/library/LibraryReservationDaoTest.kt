package it.attendance100.mybicocca.data.local.library

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
 * Behaviour coverage for [LibraryReservationDao] against a real in-memory Room database
 * (Robolectric). The Affluences "my reservations" cache is device-local (not account-scoped) and
 * keyed by the server reservation id. Exercises the upsert round-trip, the
 * `ORDER BY start_epoch_seconds` listing, the keyed delete (cancel), the whole-set `clear`, and the
 * `replaceAll` transaction that mirrors the server by clearing and rewriting the whole cache.
 *
 * library_reservation has no foreign keys, so rows insert directly with no parent.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LibraryReservationDaoTest {

    private lateinit var db: MyBicoccaDatabase
    private lateinit var dao: LibraryReservationDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MyBicoccaDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.libraryReservationDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `upsertAll then observeAll returns the stored reservations`() = runTest {
        dao.upsertAll(listOf(reservation(1, start = 1_000L), reservation(2, start = 2_000L)))

        val stored = dao.observeAll().first()

        assertThat(stored.map { it.reservationId }).containsExactly(1, 2)
        assertThat(stored.first { it.reservationId == 1 }.seatName).isEqualTo("Posto 1")
    }

    @Test
    fun `count reflects the number of stored reservations`() = runTest {
        assertThat(dao.count()).isEqualTo(0)

        dao.upsertAll(listOf(reservation(1), reservation(2)))

        assertThat(dao.count()).isEqualTo(2)
    }

    @Test
    fun `upsert on existing id replaces the row in place`() = runTest {
        dao.upsertAll(listOf(reservation(1, seatName = "Posto A")))

        dao.upsertAll(listOf(reservation(1, seatName = "Posto B")))

        val stored = dao.observeAll().first()
        assertThat(stored).hasSize(1)
        assertThat(stored.single().seatName).isEqualTo("Posto B")
    }

    @Test
    fun `observeAll orders reservations by start time ascending`() = runTest {
        dao.upsertAll(
            listOf(
                reservation(3, start = 9_000L),
                reservation(1, start = 1_000L),
                reservation(2, start = 5_000L),
            ),
        )

        val ids = dao.observeAll().first().map { it.reservationId }

        assertThat(ids).containsExactly(1, 2, 3).inOrder()
    }

    @Test
    fun `delete removes only the targeted reservation`() = runTest {
        dao.upsertAll(listOf(reservation(1), reservation(2)))

        dao.delete(1)

        assertThat(dao.observeAll().first().map { it.reservationId }).containsExactly(2)
    }

    @Test
    fun `clear removes every reservation`() = runTest {
        dao.upsertAll(listOf(reservation(1), reservation(2)))

        dao.clear()

        assertThat(dao.observeAll().first()).isEmpty()
        assertThat(dao.count()).isEqualTo(0)
    }

    @Test
    fun `replaceAll swaps the whole cache for the fresh server set`() = runTest {
        dao.upsertAll(listOf(reservation(1), reservation(2)))

        dao.replaceAll(listOf(reservation(3), reservation(4)))

        assertThat(dao.observeAll().first().map { it.reservationId }).containsExactly(3, 4)
    }

    @Test
    fun `observeAll re-emits on insert and on cancel`() = runTest {
        dao.observeAll().test {
            assertThat(awaitItem()).isEmpty()

            dao.upsertAll(listOf(reservation(1)))
            assertThat(awaitItem().map { it.reservationId }).containsExactly(1)

            dao.delete(1)
            assertThat(awaitItem()).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun reservation(
        id: Int,
        start: Long = 1_000L,
        seatName: String = "Posto $id",
    ) = LibraryReservationEntity(
        reservationId = id,
        libraryName = "Biblioteca di Ateneo",
        librarySecondaryName = "Sede Centrale",
        seatName = seatName,
        startEpochSeconds = start,
        endEpochSeconds = start + 3_600L,
        note = null,
        reservationCode = "ABC-$id",
        cancellationToken = "tok-$id",
        state = "CONFIRMED",
    )
}
