package it.attendance100.mybicocca.domain.repository

import androidx.lifecycle.*
import it.attendance100.mybicocca.domain.model.*

interface RegistryRepository {
	fun observeExams(): LiveData<List<Exam>>
	fun observeInternships(): LiveData<List<Internship>>
	fun observeQuestionnaires(): LiveData<List<Questionnaire>>

	suspend fun syncExams()
	suspend fun syncInternships()
	suspend fun syncQuestionnaires()
}