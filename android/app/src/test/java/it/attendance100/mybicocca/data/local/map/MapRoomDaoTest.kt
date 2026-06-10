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
 * Behaviour coverage for [MapRoomDao] against a real in-memory Room database (Robolectric).
 * Exercises the per-building scoping ([MapRoomDao.observeForBuilding]), the `ORDER BY name` clause,
 * the composite primary key (`code` is only unique within a building so the same room code may
 * coexist across buildings), and the `replaceForBuilding` splice transaction — which must delete
 * only the targeted building's rows and leave other buildings' rooms intact.
 *
 * map_rooms has no foreign key to map_buildings (building_code is a plain column), so rows insert
 * without a parent building present.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MapRoomDaoTest {

    private lateinit var db: MyBicoccaDatabase
    private lateinit var dao: MapRoomDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MyBicoccaDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.mapRoomDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `observeForBuilding returns only that building's rooms ordered by name`() = runTest {
        dao.upsertAll(
            listOf(
                room("R02", "U01", "Aula Beta"),
                room("R01", "U01", "Aula Alfa"),
                room("R01", "U02", "Aula Gamma"),
            ),
        )

        val rooms = dao.observeForBuilding("U01").first()

        assertThat(rooms.map { it.name }).containsExactly("Aula Alfa", "Aula Beta").inOrder()
    }

    @Test
    fun `composite key lets the same room code coexist across buildings`() = runTest {
        dao.upsertAll(
            listOf(
                room("R01", "U01", "Aula in U1"),
                room("R01", "U02", "Aula in U2"),
            ),
        )

        assertThat(dao.observeForBuilding("U01").first().single().name).isEqualTo("Aula in U1")
        assertThat(dao.observeForBuilding("U02").first().single().name).isEqualTo("Aula in U2")
    }

    @Test
    fun `upsert on the same composite key replaces the row in place`() = runTest {
        dao.upsertAll(listOf(room("R01", "U01", "Old", floor = 0)))

        dao.upsertAll(listOf(room("R01", "U01", "New", floor = 2)))

        val stored = dao.observeForBuilding("U01").first()
        assertThat(stored).hasSize(1)
        assertThat(stored.single().name).isEqualTo("New")
        assertThat(stored.single().floor).isEqualTo(2)
    }

    @Test
    fun `observeAll streams rooms across all buildings ordered by name`() = runTest {
        dao.upsertAll(
            listOf(
                room("R01", "U02", "Zeta"),
                room("R01", "U01", "Alfa"),
            ),
        )

        assertThat(dao.observeAll().first().map { it.name }).containsExactly("Alfa", "Zeta").inOrder()
    }

    @Test
    fun `deleteForBuilding removes only the targeted building's rooms`() = runTest {
        dao.upsertAll(
            listOf(
                room("R01", "U01", "Alfa"),
                room("R01", "U02", "Gamma"),
            ),
        )

        dao.deleteForBuilding("U01")

        assertThat(dao.observeForBuilding("U01").first()).isEmpty()
        assertThat(dao.observeForBuilding("U02").first().map { it.name }).containsExactly("Gamma")
    }

    @Test
    fun `replaceForBuilding swaps the targeted slice and leaves other buildings intact`() = runTest {
        dao.upsertAll(
            listOf(
                room("R01", "U01", "Old A"),
                room("R02", "U01", "Old B"),
                room("R01", "U02", "Other"),
            ),
        )

        dao.replaceForBuilding("U01", listOf(room("R09", "U01", "Fresh")))

        assertThat(dao.observeForBuilding("U01").first().map { it.name }).containsExactly("Fresh")
        assertThat(dao.observeForBuilding("U02").first().map { it.name }).containsExactly("Other")
    }

    @Test
    fun `replaceForBuilding with an empty list clears the building without touching others`() = runTest {
        dao.upsertAll(
            listOf(
                room("R01", "U01", "Doomed"),
                room("R01", "U02", "Kept"),
            ),
        )

        dao.replaceForBuilding("U01", emptyList())

        assertThat(dao.observeForBuilding("U01").first()).isEmpty()
        assertThat(dao.observeForBuilding("U02").first().map { it.name }).containsExactly("Kept")
    }

    @Test
    fun `observeForBuilding re-emits after replaceForBuilding`() = runTest {
        dao.upsertAll(listOf(room("R01", "U01", "Alfa")))

        dao.observeForBuilding("U01").test {
            assertThat(awaitItem().map { it.name }).containsExactly("Alfa")

            dao.replaceForBuilding("U01", listOf(room("R02", "U01", "Beta")))
            assertThat(awaitItem().map { it.name }).containsExactly("Beta")
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun room(
        code: String,
        buildingCode: String,
        name: String,
        capacity: Int? = 30,
        floor: Int? = 0,
    ) = MapRoomEntity(
        code = code,
        buildingCode = buildingCode,
        name = name,
        capacity = capacity,
        floor = floor,
    )
}
