package it.attendance100.mybicocca.data.repository

import androidx.lifecycle.*
import it.attendance100.mybicocca.data.api.esse3.*
import it.attendance100.mybicocca.di.*
import it.attendance100.mybicocca.domain.model.*
import javax.inject.*
import it.attendance100.mybicocca.domain.repository.RegistryRepository as IRegistryRepository

class RegistryRepository @Inject constructor(
	private val api: Esse3Api,
	private val database: AppDatabase,
) : IRegistryRepository {

	override fun observeExams(): LiveData<List<Exam>> {
		return database.userDao().getCareerStats().asLiveData().map {
			it?.passedExams ?: emptyList()
		}
	}

	override fun observeInternships(): LiveData<List<Internship>> {
		return database.registryDao().observeInternships()
	}

	override fun observeQuestionnaires(): LiveData<List<Questionnaire>> {
		return database.registryDao().observeQuestionnaires()
	}

	override suspend fun syncExams() {
		// Exams are synced via syncCareerStats in UserRepository usually.
		// But if we want to sync here:
		// val stats = api.career.getStats()
		// database.userDao().insertCareerStats(map(stats))
	}

	override suspend fun syncInternships() {
		val response = api.internship.getMyInternships()
		if (response.isSuccessful) {
			// HTML parsing required usually for Esse3, but assuming API returns JSON or we parse it
			// For now, placeholder logic
			// val list = parseInternships(response.body())
			// database.registryDao().insertInternships(list)
		}
	}

	override suspend fun syncQuestionnaires() {
		val response = api.questionnaire.getDidacticEvaluationQuestionnaires()
		if (response.isSuccessful) {
			// HTML parsing required
			// val list = parseQuestionnaires(response.body())
			// database.registryDao().insertQuestionnaires(list)
		}
	}
}