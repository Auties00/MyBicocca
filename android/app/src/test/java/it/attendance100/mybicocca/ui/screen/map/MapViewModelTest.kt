package it.attendance100.mybicocca.ui.screen.map

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.map.BuildingCategory
import it.attendance100.mybicocca.domain.model.map.BuildingCode
import it.attendance100.mybicocca.domain.model.map.GeoPoint
import it.attendance100.mybicocca.domain.model.map.MapBuilding
import it.attendance100.mybicocca.domain.model.map.MapRoom
import it.attendance100.mybicocca.domain.model.map.MapRoomDetail
import it.attendance100.mybicocca.domain.model.map.RoomCode
import it.attendance100.mybicocca.domain.model.map.RoomScheduleEntry
import it.attendance100.mybicocca.domain.usecase.map.LoadBuildingScheduleUseCase
import it.attendance100.mybicocca.domain.usecase.map.LoadRoomDetailUseCase
import it.attendance100.mybicocca.domain.usecase.map.ObserveBuildingsUseCase
import it.attendance100.mybicocca.domain.usecase.map.ObserveRoomsUseCase
import it.attendance100.mybicocca.domain.usecase.map.RefreshBuildingsUseCase
import it.attendance100.mybicocca.domain.usecase.map.RefreshRoomsUseCase
import it.attendance100.mybicocca.testing.MainDispatcherRule
import it.attendance100.mybicocca.ui.screen.map.state.MapOneShotEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Coverage for the map tab ViewModel: category filtering narrows the pins, building selection
 * resolves the catalog entry and streams its rooms, the day schedule and room detail streams
 * survive failures (null-not-throw), a failed room refresh sets [SyncStatus.Failed] and fires a
 * one-shot event without clearing data, and the category toggle is additive/idempotent.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val codeU01 = BuildingCode("U01")
    private val codeU06 = BuildingCode("U06")

    private val teachingBuilding = MapBuilding(
        code = codeU01,
        name = "Edificio U1",
        point = GeoPoint(45.516, 9.212),
        category = BuildingCategory.TEACHING,
        address = "Piazza dell'Ateneo Nuovo 1",
        city = "Milano",
    )

    private val libraryBuilding = MapBuilding(
        code = codeU06,
        name = "Edificio U6",
        point = GeoPoint(45.524, 9.221),
        category = BuildingCategory.LIBRARY,
        address = null,
        city = null,
    )

    private val catalog = listOf(teachingBuilding, libraryBuilding)

    private val roomA = MapRoom(
        code = RoomCode("U06-22"),
        buildingCode = codeU06,
        name = "Aula 22",
        capacity = 80,
        floor = 2,
    )

    private val roomB = MapRoom(
        code = RoomCode("U06-23"),
        buildingCode = codeU06,
        name = "Aula 23",
        capacity = null,
        floor = null,
    )

    private val observeBuildings: ObserveBuildingsUseCase = mockk()
    private val observeRooms: ObserveRoomsUseCase = mockk()
    private val refreshBuildings: RefreshBuildingsUseCase = mockk(relaxed = true)
    private val refreshRooms: RefreshRoomsUseCase = mockk(relaxed = true)
    private val loadRoomDetail: LoadRoomDetailUseCase = mockk(relaxed = true)
    private val loadBuildingSchedule: LoadBuildingScheduleUseCase = mockk(relaxed = true)

    private fun viewModel(
        buildings: Loadable<List<MapBuilding>> = Loadable.Loaded(catalog),
        rooms: List<MapRoom> = listOf(roomA, roomB),
    ): MapViewModel {
        every { observeBuildings() } returns flowOf(buildings)
        every { observeRooms(any()) } returns flowOf(Loadable.Loaded(rooms))
        return MapViewModel(
            observeBuildings,
            observeRooms,
            refreshBuildings,
            refreshRooms,
            loadRoomDetail,
            loadBuildingSchedule,
        )
    }

    @Test
    fun `refreshBuildings is seeded once at construction`() = runTest {
        viewModel()

        coVerify(exactly = 1) { refreshBuildings() }
    }

    @Test
    fun `allBuildings mirrors the full catalog`() = runTest {
        val vm = viewModel()

        vm.allBuildings.test {
            assertThat(awaitItem()).isEqualTo(Loadable.Loaded(catalog))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `buildings with no category filter shows the whole catalog`() = runTest {
        val vm = viewModel()

        vm.buildings.test {
            val loaded = awaitItem() as Loadable.Loaded
            assertThat(loaded.value).containsExactly(teachingBuilding, libraryBuilding)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleCategory narrows the pins to the selected category`() = runTest {
        val vm = viewModel()

        vm.toggleCategory(BuildingCategory.LIBRARY)

        vm.buildings.test {
            val loaded = awaitItem() as Loadable.Loaded
            assertThat(loaded.value).containsExactly(libraryBuilding)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleCategory twice on the same category clears it`() = runTest {
        val vm = viewModel()

        vm.toggleCategory(BuildingCategory.LIBRARY)
        vm.toggleCategory(BuildingCategory.LIBRARY)

        assertThat(vm.categoryFilter.value).isEmpty()
    }

    @Test
    fun `toggleCategory accumulates multiple categories`() = runTest {
        val vm = viewModel()

        vm.toggleCategory(BuildingCategory.LIBRARY)
        vm.toggleCategory(BuildingCategory.TEACHING)

        assertThat(vm.categoryFilter.value)
            .containsExactly(BuildingCategory.LIBRARY, BuildingCategory.TEACHING)
        vm.buildings.test {
            val loaded = awaitItem() as Loadable.Loaded
            assertThat(loaded.value).containsExactly(teachingBuilding, libraryBuilding)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearCategories empties the filter`() = runTest {
        val vm = viewModel()
        vm.toggleCategory(BuildingCategory.LIBRARY)

        vm.clearCategories()

        assertThat(vm.categoryFilter.value).isEmpty()
    }

    @Test
    fun `selectBuilding resolves the catalog entry by code`() = runTest {
        val vm = viewModel()

        vm.selectBuilding(codeU06)

        vm.selectedBuilding.test {
            assertThat(awaitItem()).isEqualTo(libraryBuilding)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectBuilding triggers a room refresh for that building`() = runTest {
        val vm = viewModel()

        vm.selectBuilding(codeU06)

        coVerify { refreshRooms(codeU06) }
    }

    @Test
    fun `rooms streams the selected building's cached list`() = runTest {
        val vm = viewModel()

        vm.selectBuilding(codeU06)

        vm.rooms.test {
            val loaded = awaitItem() as Loadable.Loaded
            assertThat(loaded.value).containsExactly(roomA, roomB)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearSelection resets the building and room selection`() = runTest {
        val vm = viewModel()
        vm.selectBuilding(codeU06)
        vm.selectRoom(roomA)

        vm.clearSelection()

        assertThat(vm.selectedBuilding.value).isNull()
        assertThat(vm.selectedRoom.value).isNull()
    }

    @Test
    fun `selectRoom sets the selected room`() = runTest {
        val vm = viewModel()

        vm.selectRoom(roomA)

        assertThat(vm.selectedRoom.value).isEqualTo(roomA)
    }

    @Test
    fun `clearRoomSelection drops only the room`() = runTest {
        val vm = viewModel()
        vm.selectBuilding(codeU06)
        vm.selectRoom(roomA)

        vm.clearRoomSelection()

        assertThat(vm.selectedRoom.value).isNull()
        vm.selectedBuilding.test {
            assertThat(awaitItem()).isEqualTo(libraryBuilding)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selecting a building clears any prior room selection`() = runTest {
        val vm = viewModel()
        vm.selectRoom(roomA)

        vm.selectBuilding(codeU01)

        assertThat(vm.selectedRoom.value).isNull()
    }

    @Test
    fun `roomDetail loads the opened room's detail`() = runTest {
        val detail = MapRoomDetail(
            floor = 2,
            capacity = 80,
            isAccessible = true,
            accessibilityNotes = null,
            isInclusionValidated = false,
            roomType = "Aula",
            address = "Via X",
            description = null,
            equipment = listOf("Proiettore"),
        )
        coEvery { loadRoomDetail(roomA) } returns detail
        val vm = viewModel()

        vm.selectRoom(roomA)

        vm.roomDetail.test {
            assertThat(awaitItem()).isEqualTo(Loadable.Loaded(detail))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `roomDetail yields a null-loaded value when the fetch throws`() = runTest {
        coEvery { loadRoomDetail(roomA) } throws IOException("offline")
        val vm = viewModel()

        vm.selectRoom(roomA)

        vm.roomDetail.test {
            assertThat(awaitItem()).isEqualTo(Loadable.Loaded(null))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `daySchedule groups the loaded entries by room code`() = runTest {
        val entry = RoomScheduleEntry(
            roomCode = RoomCode("U06-22"),
            title = "Analisi I",
            kind = "Lezione",
            teacher = "Prof. Bianchi",
            start = LocalDateTime.of(2026, 6, 10, 9, 0),
            end = LocalDateTime.of(2026, 6, 10, 11, 0),
        )
        coEvery { loadBuildingSchedule(codeU06, any()) } returns listOf(entry)
        val vm = viewModel()

        vm.selectBuilding(codeU06)

        vm.daySchedule.test {
            val loaded = awaitItem() as Loadable.Loaded
            assertThat(loaded.value).containsExactly("U06-22", listOf(entry))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `daySchedule emits a null map when the fetch fails`() = runTest {
        coEvery { loadBuildingSchedule(any(), any()) } throws IOException("offline")
        val vm = viewModel()

        vm.selectBuilding(codeU06)

        vm.daySchedule.test {
            val loaded = awaitItem() as Loadable.Loaded
            assertThat(loaded.value).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a successful room refresh leaves syncStatus Idle`() = runTest {
        val vm = viewModel()

        vm.selectBuilding(codeU06)

        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Idle)
    }

    @Test
    fun `a failed room refresh sets syncStatus Failed`() = runTest {
        val cause = IOException("rooms down")
        coEvery { refreshRooms(codeU06) } throws cause
        val vm = viewModel()

        vm.selectBuilding(codeU06)

        val status = vm.syncStatus.value
        assertThat(status).isInstanceOf(SyncStatus.Failed::class.java)
        assertThat((status as SyncStatus.Failed).cause).isEqualTo(cause)
    }

    @Test
    fun `a failed room refresh emits a one-shot RefreshFailed event`() = runTest {
        val cause = IOException("rooms down")
        coEvery { refreshRooms(codeU06) } throws cause
        val vm = viewModel()

        vm.oneShotEvents.test {
            vm.selectBuilding(codeU06)
            val event = awaitItem()
            assertThat(event).isInstanceOf(MapOneShotEvent.RefreshFailed::class.java)
            assertThat((event as MapOneShotEvent.RefreshFailed).cause).isEqualTo(cause)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `retryRooms re-refreshes the currently selected building`() = runTest {
        val vm = viewModel()
        vm.selectBuilding(codeU06)

        vm.retryRooms()

        coVerify(exactly = 2) { refreshRooms(codeU06) }
    }

    @Test
    fun `retryRooms is a no-op when nothing is selected`() = runTest {
        val vm = viewModel()

        vm.retryRooms()

        coVerify(exactly = 0) { refreshRooms(any()) }
    }

    @Test
    fun `selectRoomByCode selects the building and opens the matching room`() = runTest {
        val rooms = MutableStateFlow<Loadable<List<MapRoom>>>(Loadable.Loaded(listOf(roomA, roomB)))
        every { observeBuildings() } returns flowOf(Loadable.Loaded(catalog))
        every { observeRooms(codeU06) } returns rooms
        val vm = MapViewModel(
            observeBuildings,
            observeRooms,
            refreshBuildings,
            refreshRooms,
            loadRoomDetail,
            loadBuildingSchedule,
        )

        vm.selectRoomByCode(codeU06, RoomCode("U06-23"))

        assertThat(vm.selectedRoom.value).isEqualTo(roomB)
        vm.selectedBuilding.test {
            assertThat(awaitItem()).isEqualTo(libraryBuilding)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
