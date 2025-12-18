package it.attendance100.mybicocca.data.dao

import androidx.lifecycle.*
import androidx.room.*
import it.attendance100.mybicocca.domain.model.*

@Dao
interface RegistryDao {
	// --- Internships ---
	@Query("SELECT * FROM internships")
	fun observeInternships(): LiveData<List<Internship>>


	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertInternships(internships: List<Internship>)


	@Query("DELETE FROM internships")
	suspend fun clearInternships()


	// --- Questionnaires ---
	@Query("SELECT * FROM questionnaires")
	fun observeQuestionnaires(): LiveData<List<Questionnaire>>


	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertQuestionnaires(questionnaires: List<Questionnaire>)


	@Query("DELETE FROM questionnaires")
	suspend fun clearQuestionnaires()
}
