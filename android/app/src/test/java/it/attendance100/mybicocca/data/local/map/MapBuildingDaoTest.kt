package it.attendance100.mybicocca.data.local.map

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
 * Behaviour coverage for [MapBuildingDao] against a real in-memory Room database (Robolectric).
 * Exercises the upsert round-trip, the count surface, and the deliberate sort clause
 * (`ORDER BY code GLOB 'U[0-9]*' DESC, code`) that keeps the numbered sites (U01..U38) ahead of the
 * non-numbered ones (U-BG, U-MB) while still sorting numerically within the numbered set.
 *
 * The map_buildings table is a shared catalog with no foreign keys, so rows insert directly.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MapBuildingDaoTest {

    private lateinit var db: MyBicoccaDatabase
    private lateinit var dao: MapBuildingDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MyBicoccaDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.mapBuildingDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `upsertAll then observeAll returns the stored buildings`() = runTest {
        dao.upsertAll(listOf(building("U01", "Edificio U1"), building("U02", "Edificio U2")))

        val stored = dao.observeAll().first()

        assertThat(stored.map { it.code }).containsExactly("U01", "U02")
        assertThat(stored.first { it.code == "U01" }.name).isEqualTo("Edificio U1")
    }

    @Test
    fun `upsert on existing code replaces the row in place`() = runTest {
        dao.upsertAll(listOf(building("U01", "Old")))

        dao.upsertAll(listOf(building("U01", "New")))

        val stored = dao.observeAll().first()
        assertThat(stored).hasSize(1)
        assertThat(stored.single().name).isEqualTo("New")
    }

    @Test
    fun `count reflects the number of stored buildings`() = runTest {
        assertThat(dao.count()).isEqualTo(0)

        dao.upsertAll(listOf(building("U01"), building("U02"), building("U-BG")))

        assertThat(dao.count()).isEqualTo(3)
    }

    @Test
    fun `observeAll sorts numbered sites numerically and keeps non-numbered ones last`() = runTest {
        dao.upsertAll(
            listOf(
                building("U-MB"),
                building("U02"),
                building("U10"),
                building("U-BG"),
                building("U01"),
            ),
        )

        val codes = dao.observeAll().first().map { it.code }

        assertThat(codes).containsExactly("U01", "U02", "U10", "U-BG", "U-MB").inOrder()
    }

    @Test
    fun `observeAll re-emits after an upsert`() = runTest {
        dao.observeAll().test {
            assertThat(awaitItem()).isEmpty()

            dao.upsertAll(listOf(building("U01")))
            assertThat(awaitItem().map { it.code }).containsExactly("U01")
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun building(
        code: String,
        name: String = "Edificio $code",
    ) = MapBuildingEntity(
        code = code,
        name = name,
        latitude = 45.5,
        longitude = 9.2,
        category = "DEPARTMENT",
        address = "Piazza dell'Ateneo Nuovo 1",
        city = "Milano",
    )
}
