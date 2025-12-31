package it.attendance100.mybicocca.domain.repository

import androidx.lifecycle.LiveData
import it.attendance100.mybicocca.domain.model.MapLocation
import it.attendance100.mybicocca.domain.model.Teacher

interface CampusRepository {
	fun observeLocations(): LiveData<List<MapLocation>>
	suspend fun syncLocations(): Boolean

	suspend fun searchTeacher(email: String): Teacher?
}
