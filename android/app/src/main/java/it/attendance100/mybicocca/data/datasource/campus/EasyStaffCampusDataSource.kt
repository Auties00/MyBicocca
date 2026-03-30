package it.attendance100.mybicocca.data.datasource.campus

import it.attendance100.mybicocca.data.api.easystaff.EasyStaffApi
import it.attendance100.mybicocca.data.dto.easystaff.EasyStaffBuilding
import it.attendance100.mybicocca.data.dto.easystaff.EasyStaffRoom
import it.attendance100.mybicocca.data.dto.easystaff.Esse3RoomEquipment
import it.attendance100.mybicocca.data.model.campus.Building
import it.attendance100.mybicocca.data.model.campus.Room
import it.attendance100.mybicocca.data.model.campus.RoomDetails
import it.attendance100.mybicocca.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EasyStaffCampusDataSource @Inject constructor(
    private val easyStaffApi: EasyStaffApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    companion object {
        private val BUILDINGS = listOf(
            Building("U01", "ATLAS ex U1", 45.513338, 9.211242),
            Building("U02", "QUANTUM ex U2", 45.513491, 9.209881),
            Building("U03", "BIOS ex U3", 45.513689, 9.209893),
            Building("U04", "TELLUS ex U4", 45.514121, 9.208662),
            Building("U05", "RATIO ex U5", 45.511970, 9.213140),
            Building("U06", "AGORA' ex U6", 45.518432, 9.212670),
            Building("U07", "CIVITAS ex U7", 45.517400, 9.210911),
            Building("U08", "ASCLEPIO ex U8", 45.603860, 9.261042),
            Building("U09", "KOINE' ex U9", 45.511144, 9.209037),
            Building("U14", "ABACUS ex U14", 45.523655, 9.217478),
            Building("U16", "MAIEUTICA ex U16", 45.523997, 9.206441),
            Building("U18", "YGEIA ex U18", 45.604506, 9.260563),
            Building("U24", "ZIFERA ex U24", 45.523805, 9.219887),
            Building("U26", "AGON ex U26", 45.525039, 9.209400),
            Building("U28", "KYTOS ex U28", 45.605050, 9.261607),
            Building("U38", "VILLA SERENA ex U38", 45.602475, 9.260777),
            Building("U-MB", "Istituti Clinici Zucchi - Edificio Villa Maria (Carate Brianza)", 45.676820, 9.239580),
            Building("U-BG", "Sede Nini da Fano (Bergamo)", 45.691384, 9.634854),
            Building("U-VIVAIO BICOCCA", "VIVAIO BICOCCA", 45.508987, 9.209960),
        )
    }

    fun getBuildings(): List<Building> = BUILDINGS

    suspend fun getRooms(buildingCode: String): List<Room> = withContext(ioDispatcher) {
        val buildings = easyStaffApi.buildings.getBuildings()
        val building = buildings.firstOrNull { it.code == buildingCode } ?: return@withContext emptyList()

        easyStaffApi.buildings.getRooms(building)
            .filter { it != EasyStaffRoom.FILTER_ALLOW_ALL }
            .map { room ->
                Room(
                    code = room.code,
                    buildingCode = buildingCode,
                    name = room.name,
                    capacity = room.capacity,
                )
            }
    }

    suspend fun getRoomDetails(buildingCode: String): List<RoomDetails> = withContext(ioDispatcher) {
        val buildings = easyStaffApi.buildings.getBuildings()
        val building = buildings.firstOrNull { it.code == buildingCode }
            ?: return@withContext emptyList()

        easyStaffApi.buildings.getRoomDetails(listOf(building))
            .map { details ->
                RoomDetails(
                    name = details.name,
                    address = details.address,
                    googleMapsLink = details.googleMapsLink,
                    interactive360Link = details.interactive360Link,
                    description = details.description,
                    capacity = details.capacity,
                    roomType = details.roomType,
                    floor = details.floor,
                    isAccessible = details.isAccessible,
                    equipment = details.equipment.map { it.displayName() },
                )
            }
    }
}

private fun Esse3RoomEquipment.displayName(): String = when (this) {
    is Esse3RoomEquipment.FixedEquipment -> "Attrezzature fisse"
    is Esse3RoomEquipment.MobileEquipment -> "Attrezzature mobili"
    is Esse3RoomEquipment.TeacherDesk -> "Cattedra"
    is Esse3RoomEquipment.BlackoutWindows -> "Finestre oscurabili"
    is Esse3RoomEquipment.TieredSeating -> "Gradoni"
    is Esse3RoomEquipment.AccessibleTable -> "Tavolo accessibile"
    is Esse3RoomEquipment.Podium -> "Podio"
    is Esse3RoomEquipment.FlipChart -> "Lavagna a fogli"
    is Esse3RoomEquipment.Whiteboard -> "Lavagna a pennarelli"
    is Esse3RoomEquipment.SlateBlackboard -> "Lavagna in ardesia"
    is Esse3RoomEquipment.OverheadProjector -> "Lavagna luminosa"
    is Esse3RoomEquipment.SlidingBlackboard -> "Lavagna saliscendi"
    is Esse3RoomEquipment.AudioSystem -> "Impianto audio"
    is Esse3RoomEquipment.StandardVideoLessonKit -> "Kit videolezioni"
    is Esse3RoomEquipment.LcdPlasmaMonitor -> "Monitor LCD/Plasma"
    is Esse3RoomEquipment.Camera -> "Telecamera"
    is Esse3RoomEquipment.CeilingProjectorAndScreen -> "Proiettore e telo"
    is Esse3RoomEquipment.TeacherPc -> "PC cattedra"
    is Esse3RoomEquipment.WiredNetwork -> "Rete fissa"
    is Esse3RoomEquipment.WifiNetwork -> "Wi-Fi"
    is Esse3RoomEquipment.StudentPowerSocket -> "Presa elettrica studente"
    is Esse3RoomEquipment.RecordingSystem -> "Registrazione"
    is Esse3RoomEquipment.VideoConference -> "Videoconferenza"
    is Esse3RoomEquipment.WebConference -> "Webconference"
    is Esse3RoomEquipment.ExaminationBeds -> "Lettini"
    is Esse3RoomEquipment.FixedChairsWithDesks -> "Sedie fisse con banco"
    is Esse3RoomEquipment.FixedChairsWithTabletArms -> "Sedie fisse con ribaltina"
    is Esse3RoomEquipment.MobileChairsWithTabletArms -> "Sedie mobili con ribaltina"
    is Esse3RoomEquipment.MobileChairsWithoutTabletArms -> "Sedie mobili senza ribaltina"
    is Esse3RoomEquipment.Other -> name
}
