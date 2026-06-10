package it.attendance100.mybicocca.data.repository

import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import it.attendance100.mybicocca.core.time.StalePolicy
import it.attendance100.mybicocca.data.local.map.BuildingCatalogDto
import it.attendance100.mybicocca.data.local.map.BuildingCatalogSource
import it.attendance100.mybicocca.data.local.map.MapBuildingDao
import it.attendance100.mybicocca.data.local.map.MapBuildingEntity
import it.attendance100.mybicocca.data.local.map.MapRoomDao
import it.attendance100.mybicocca.data.local.map.MapRoomEntity
import it.attendance100.mybicocca.data.local.map.MapRoomSyncStateDao
import it.attendance100.mybicocca.data.local.map.MapRoomSyncStateEntity
import it.attendance100.mybicocca.data.remote.easystaff.api.EasyStaffApi
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffBookingStatus
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffRoom
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffRoomDetails
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffRoomOccupationEvent
import it.attendance100.mybicocca.domain.model.map.BuildingCode
import it.attendance100.mybicocca.domain.model.map.MapRoom
import it.attendance100.mybicocca.domain.model.map.RoomCode
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Map repository policy: the catalog re-seed guarded against an empty read, the EasyStaff room
 * sync (TTL skip, FILTER_ALLOW_ALL filtering, floor join from the showcase, write-through plus
 * sync-state stamp), and the day-schedule read that keeps only confirmed bookings.
 */
class MapRepositoryImplTest {

    private val easyStaffApi: EasyStaffApi = mockk(relaxed = true)
    private val buildingCatalog: BuildingCatalogSource = mockk(relaxed = true)
    private val buildingDao: MapBuildingDao = mockk(relaxed = true)
    private val roomDao: MapRoomDao = mockk(relaxed = true)
    private val syncStateDao: MapRoomSyncStateDao = mockk(relaxed = true)
    private val stalePolicy = StalePolicy(defaultTtlMs = 60_000L)

    private val buildingCode = BuildingCode("U06")

    private lateinit var repository: MapRepositoryImpl

    @Before
    fun setUp() {
        repository = MapRepositoryImpl(
            easyStaffApi = easyStaffApi,
            buildingCatalog = buildingCatalog,
            buildingDao = buildingDao,
            roomDao = roomDao,
            syncStateDao = syncStateDao,
            stalePolicy = stalePolicy,
        )
    }

    @Test
    fun `refreshBuildings seeds the catalog into the building table`() = runTest {
        coEvery { buildingCatalog.load() } returns listOf(
            BuildingCatalogDto(code = "U01", name = "Atlas", latitude = 45.0, longitude = 9.0),
        )
        val written = slot<List<MapBuildingEntity>>()
        coEvery { buildingDao.upsertAll(capture(written)) } just Runs

        repository.refreshBuildings()

        assertThat(written.captured).hasSize(1)
        assertThat(written.captured.single().code).isEqualTo("U01")
    }

    @Test
    fun `refreshBuildings skips the write when the catalog read yields nothing`() = runTest {
        coEvery { buildingCatalog.load() } returns emptyList()

        repository.refreshBuildings()

        coVerify(exactly = 0) { buildingDao.upsertAll(any()) }
    }

    @Test
    fun `refreshBuildings keeps the table when the catalog read fails`() = runTest {
        coEvery { buildingCatalog.load() } throws RuntimeException("asset missing")

        repository.refreshBuildings()

        coVerify(exactly = 0) { buildingDao.upsertAll(any()) }
    }

    @Test
    fun `refreshRooms skips the fetch while the cached copy is fresh`() = runTest {
        coEvery { syncStateDao.getState("U06") } returns
            MapRoomSyncStateEntity(buildingCode = "U06", lastRefreshedAtMs = System.currentTimeMillis())

        repository.refreshRooms(buildingCode)

        coVerify(exactly = 0) { easyStaffApi.buildings.getRooms(any(), any()) }
        coVerify(exactly = 0) { roomDao.replaceForBuilding(any(), any()) }
    }

    @Test
    fun `refreshRooms replaces rooms dropping the allow-all sentinel and joining floors`() = runTest {
        coEvery { syncStateDao.getState("U06") } returns null
        coEvery { easyStaffApi.buildings.getRooms(any(), any()) } returns listOf(
            EasyStaffRoom(code = "U6-22", name = "U6-22 con Podio", capacity = 80),
            EasyStaffRoom.FILTER_ALLOW_ALL,
        )
        coEvery { easyStaffApi.buildings.getRoomDetails(any(), any(), any()) } returns listOf(
            roomDetails(roomCode = "U6-22", floor = 1),
        )
        val written = slot<List<MapRoomEntity>>()
        coEvery { roomDao.replaceForBuilding("U06", capture(written)) } just Runs
        coEvery { syncStateDao.upsertState(any()) } just Runs

        repository.refreshRooms(buildingCode)

        assertThat(written.captured).hasSize(1)
        assertThat(written.captured.single().code).isEqualTo("U6-22")
        assertThat(written.captured.single().floor).isEqualTo(1)
        coVerify(exactly = 1) { syncStateDao.upsertState(any()) }
    }

    @Test
    fun `refreshRooms still maps rooms when floor resolution fails`() = runTest {
        coEvery { syncStateDao.getState("U06") } returns null
        coEvery { easyStaffApi.buildings.getRooms(any(), any()) } returns listOf(
            EasyStaffRoom(code = "U6-01", name = "U6-01", capacity = 40),
        )
        coEvery { easyStaffApi.buildings.getRoomDetails(any(), any(), any()) } throws RuntimeException("parse error")
        val written = slot<List<MapRoomEntity>>()
        coEvery { roomDao.replaceForBuilding("U06", capture(written)) } just Runs
        coEvery { syncStateDao.upsertState(any()) } just Runs

        repository.refreshRooms(buildingCode)

        assertThat(written.captured).hasSize(1)
        assertThat(written.captured.single().floor).isNull()
    }

    @Test
    fun `loadDaySchedule keeps only confirmed bookings`() = runTest {
        coEvery { easyStaffApi.buildings.getBuildingOccupation(any(), any(), any(), any()) } returns listOf(
            occupationEvent("1", EasyStaffBookingStatus.CONFIRMED),
            occupationEvent("2", EasyStaffBookingStatus.CANCELLED),
        )

        val schedule = repository.loadDaySchedule(buildingCode, LocalDate.of(2026, 6, 15))

        assertThat(schedule).hasSize(1)
        assertThat(schedule.single().title).isEqualTo("Lezione 1")
    }

    @Test
    fun `loadRoomDetail maps the single showcase card`() = runTest {
        coEvery { easyStaffApi.buildings.getRoomDetails(any(), any(), any()) } returns listOf(
            roomDetails(roomCode = "U6-22", floor = 2),
        )

        val room = MapRoom(
            code = RoomCode("U6-22"),
            buildingCode = buildingCode,
            name = "U6-22",
            capacity = 80,
            floor = null,
        )
        val detail = repository.loadRoomDetail(room)

        assertThat(detail).isNotNull()
        assertThat(detail!!.floor).isEqualTo(2)
    }

    private fun roomDetails(roomCode: String, floor: Int?) = EasyStaffRoomDetails(
        roomCode = roomCode,
        roomName = roomCode,
        name = "Sede Centrale",
        address = null,
        googleMapsLink = null,
        interactive360Link = null,
        description = null,
        capacity = 80,
        roomType = null,
        floor = floor,
        isAccessible = false,
        accessibilityNotes = null,
        isInclusionValidated = false,
        equipment = emptyList(),
    )

    private fun occupationEvent(id: String, status: EasyStaffBookingStatus) = EasyStaffRoomOccupationEvent(
        id = id,
        title = "Lezione $id",
        date = LocalDate.of(2026, 6, 15),
        startDateTime = LocalDateTime.of(2026, 6, 15, 9, 0),
        endDateTime = LocalDateTime.of(2026, 6, 15, 11, 0),
        roomName = "U6-22",
        roomCode = "U6-22",
        buildingName = "U06",
        buildingCode = "U06",
        facultyId = "F1",
        status = status,
        eventType = "Lezione",
        isUniversityEvent = false,
        teachersList = emptyList(),
    )
}
