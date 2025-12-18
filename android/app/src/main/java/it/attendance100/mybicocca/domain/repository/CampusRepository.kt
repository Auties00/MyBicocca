package it.attendance100.mybicocca.domain.repository

import androidx.lifecycle.*
import it.attendance100.mybicocca.domain.model.*

interface CampusRepository {
	fun observeLocations(): LiveData<List<MapLocation>>
	suspend fun syncLocations()

	suspend fun searchTeacher(email: String): Teacher?
}
