package it.attendance100.mybicocca.data.local.appointment

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
 * Behaviour coverage for [AppointmentReservationDao] against a real in-memory Room database
 * (Robolectric). The Portale Planning store is device-local (not account-scoped) and keyed by the
 * portal reservation code. Exercises the upsert round-trip, the `ORDER BY start_epoch_seconds`
 * listing, the keyed delete (cancel), and the observe re-emission on insert/cancel.
 *
 * appointment_reservation has no foreign keys, so rows insert directly with no parent.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppointmentReservationDaoTest {

    private lateinit var db: MyBicoccaDatabase
    private lateinit var dao: AppointmentReservationDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MyBicoccaDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.appointmentReservationDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `upsert then getAll returns the stored reservation`() = runTest {
        dao.upsert(reservation("RES-1", start = 1_000L))

        val all = dao.getAll()

        assertThat(all).hasSize(1)
        assertThat(all.single().code).isEqualTo("RES-1")
        assertThat(all.single().email).isEqualTo("mario.rossi@campus.unimib.it")
    }

    @Test
    fun `upsert on existing code replaces the row in place`() = runTest {
        dao.upsert(reservation("RES-1", serviceName = "Old"))

        dao.upsert(reservation("RES-1", serviceName = "New"))

        val all = dao.getAll()
        assertThat(all).hasSize(1)
        assertThat(all.single().serviceName).isEqualTo("New")
    }

    @Test
    fun `observeAll orders reservations by start time ascending`() = runTest {
        dao.upsert(reservation("LATE", start = 9_000L))
        dao.upsert(reservation("EARLY", start = 1_000L))
        dao.upsert(reservation("MID", start = 5_000L))

        val codes = dao.observeAll().first().map { it.code }

        assertThat(codes).containsExactly("EARLY", "MID", "LATE").inOrder()
    }

    @Test
    fun `delete removes only the targeted reservation`() = runTest {
        dao.upsert(reservation("RES-1"))
        dao.upsert(reservation("RES-2"))

        dao.delete("RES-1")

        assertThat(dao.getAll().map { it.code }).containsExactly("RES-2")
    }

    @Test
    fun `observeAll re-emits on insert and on cancel`() = runTest {
        dao.observeAll().test {
            assertThat(awaitItem()).isEmpty()

            dao.upsert(reservation("RES-1"))
            assertThat(awaitItem().map { it.code }).containsExactly("RES-1")

            dao.delete("RES-1")
            assertThat(awaitItem()).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun reservation(
        code: String,
        start: Long = 1_000L,
        serviceName: String = "Sportello",
    ) = AppointmentReservationEntity(
        code = code,
        email = "mario.rossi@campus.unimib.it",
        entryId = 42,
        serviceId = 7,
        serviceName = serviceName,
        serviceGroup = "Segreteria",
        areaName = "U06",
        areaAddress = "Piazza dell'Ateneo Nuovo 1",
        startEpochSeconds = start,
        endEpochSeconds = start + 1_800L,
        qrCodeDataUrl = null,
        webConferenceUrl = null,
        createdAt = 0L,
    )
}
