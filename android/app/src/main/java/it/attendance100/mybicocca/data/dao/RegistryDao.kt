package it.attendance100.mybicocca.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import it.attendance100.mybicocca.domain.model.Internship
import it.attendance100.mybicocca.domain.model.Questionnaire

@Dao
interface RegistryDao {
	@Query("SELECT * FROM internships")
	fun observeInternships(): LiveData<List<Internship>>


	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertInternships(internships: List<Internship>)


	@Query("DELETE FROM internships")
	suspend fun clearInternships()


	@Query("SELECT * FROM questionnaires")
	fun observeQuestionnaires(): LiveData<List<Questionnaire>>


	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertQuestionnaires(questionnaires: List<Questionnaire>)


	@Query("DELETE FROM questionnaires")
	suspend fun clearQuestionnaires()
}
