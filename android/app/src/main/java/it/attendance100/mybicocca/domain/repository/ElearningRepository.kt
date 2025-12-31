package it.attendance100.mybicocca.domain.repository

import androidx.lifecycle.LiveData
import it.attendance100.mybicocca.domain.model.Conversation
import it.attendance100.mybicocca.domain.model.ElearningCourse
import it.attendance100.mybicocca.domain.model.ElearningCourseSelector
import it.attendance100.mybicocca.domain.model.Message

interface ElearningRepository {
	fun observeCourses(selector: ElearningCourseSelector): LiveData<List<ElearningCourse>>
	fun observeConversations(): LiveData<List<Conversation>>
	fun observeMessages(conversationId: Int): LiveData<List<Message>>
    
    suspend fun syncCourses(): Boolean
	suspend fun syncConversations(): Boolean
	suspend fun syncMessages(conversationId: Int): Boolean
}