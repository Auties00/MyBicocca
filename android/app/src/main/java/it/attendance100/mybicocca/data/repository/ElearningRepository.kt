package it.attendance100.mybicocca.data.repository

import androidx.lifecycle.LiveData
import it.attendance100.mybicocca.data.api.elearning.ElearningApi
import it.attendance100.mybicocca.di.AppDatabase
import it.attendance100.mybicocca.domain.model.Conversation
import it.attendance100.mybicocca.domain.model.ElearningCourse
import it.attendance100.mybicocca.domain.model.ElearningCourseSelector
import it.attendance100.mybicocca.domain.model.Message
import it.attendance100.mybicocca.manager.StorageManager
import javax.inject.Inject
import it.attendance100.mybicocca.domain.repository.ElearningRepository as IElearningRepository

class ElearningRepository @Inject constructor(
	private val api: ElearningApi,
	private val database: AppDatabase,
	private val storageManager: StorageManager,
) : IElearningRepository {

	override fun observeCourses(selector: ElearningCourseSelector): LiveData<List<ElearningCourse>> {
		return database.elearningDao().observeCourses()
	}

	override fun observeConversations(): LiveData<List<Conversation>> {
		return database.elearningDao().observeConversations()
	}

	override fun observeMessages(conversationId: Int): LiveData<List<Message>> {
		return database.elearningDao().observeMessages(conversationId)
    }

    override suspend fun syncCourses(): Boolean {
	    TODO()
    }

	override suspend fun syncConversations(): Boolean {
        TODO()
	}

	override suspend fun syncMessages(conversationId: Int): Boolean {
        TODO()
    }
}