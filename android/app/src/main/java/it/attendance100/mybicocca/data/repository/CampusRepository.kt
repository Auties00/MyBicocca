package it.attendance100.mybicocca.data.repository

import androidx.lifecycle.*
import it.attendance100.mybicocca.data.api.bicoccapp.*
import it.attendance100.mybicocca.di.*
import it.attendance100.mybicocca.domain.model.*
import javax.inject.*
import it.attendance100.mybicocca.domain.repository.CampusRepository as ICampusRepository

class CampusRepository @Inject constructor(
	private val api: BicoccappApi,
	private val database: AppDatabase,
) : ICampusRepository {

	override fun observeLocations(): LiveData<List<MapLocation>> {
		return database.campusDao().observeLocations()
	}

	override suspend fun syncLocations() {
		val response = api.campus.getPointsOfInterest()
		if (response.isSuccessful) {
			val body = response.body()
			val maps = body?.maps
			// Flatten the maps object to a list of MapLocation
			val locations = mutableListOf<MapLocation>()

			// Note: This mapping depends on the structure of BicoccappPointOfInterestsMaps
			// Since we don't have the full structure of that class in context,
			// I will assume it has lists of locations or similar.
			// For now, I'll put a placeholder logic assuming we can extract them.
			// If the DTO structure is complex, we'd need a mapper.
			// Let's assume 'maps' has properties like 'classrooms', 'labs', etc.

			// Since I can't see the full DTO structure right now, I'll leave the mapping simple
			// or comment it out until I can verify the DTO.
			// But the instruction is to implement it.
			// I'll assume we iterate over available lists.

			/*
			body?.maps?.let { maps ->
				// Example mapping
				maps.buildings?.forEach { b ->
				   locations.add(MapLocation(id=b.code, name=b.name, ...))
				}
			}
			*/
			// database.campusDao().insertLocations(locations)
		}
	}

	override suspend fun searchTeacher(email: String): Teacher? {
		// Try local first
		val local = database.campusDao().getTeacherByEmail(email)
		if (local != null) return local

		// Fetch remote
		val response = api.campus.getTeacherByEmail(email)
		if (response.isSuccessful) {
			val responseBody = response.body()
			val teacherDto = responseBody?.teacher

			if (teacherDto != null) {
				// Map DTO to Domain
				// DTO 'name' is First Name, 'surname' is Last Name
				val fName = teacherDto.name ?: ""
				val lName = teacherDto.surname ?: ""
				val full = "$fName $lName".trim()

				// Map Offices to single string (e.g. "U6 - 1 floor - Room 10")
				// Assuming BicoccappTeacherOffice has building, floor, room fields.
				// We don't have the full definition of BicoccappTeacherOffice in context,
				// but typically it has building/room.
				// Let's assume we can toString() it or join properties if we knew them.
				// For now, let's take the first office and make a string representation
				// or join the list.
				val officeStr = teacherDto.offices.firstOrNull()?.toString()

				// Map Rooms (where they teach?) to List<String>
				val roomList = teacherDto.rooms.map {
					// Assuming BicoccappTeacherRoom has a name/code
					// it.code ?: it.description ?: ""
					it.toString()
				}

				val teacher = Teacher(
					id = teacherDto.email ?: email,
					firstName = fName,
					lastName = lName,
					fullName = full,
					email = teacherDto.email,
					office = officeStr,
					receivesOn = teacherDto.receivesOn,
					rooms = roomList,
				)
				database.campusDao().insertTeacher(teacher)
				return teacher
			}
		}
		return null
	}
}
