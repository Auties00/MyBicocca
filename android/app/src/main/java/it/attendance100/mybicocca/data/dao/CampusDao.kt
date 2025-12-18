package it.attendance100.mybicocca.data.dao

import androidx.lifecycle.*
import androidx.room.*
import it.attendance100.mybicocca.domain.model.*

@Dao
interface CampusDao {
	@Query("SELECT * FROM map_locations ORDER BY name ASC")
	fun observeLocations(): LiveData<List<MapLocation>>


	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertLocations(locations: List<MapLocation>)


	@Query("DELETE FROM map_locations")
	suspend fun clearLocations()


	// Teacher caching is optional, but we'll add basic support
	@Query("SELECT * FROM teachers WHERE email = :email LIMIT 1")
	suspend fun getTeacherByEmail(email: String): Teacher?


	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertTeacher(teacher: Teacher)
}
