package it.attendance100.mybicocca.domain.repository

import androidx.lifecycle.*
import it.attendance100.mybicocca.domain.model.*

interface ElearningRepository {
	fun observeCourses(selector: ElearningCourseSelector): LiveData<List<ElearningCourse>>
	fun observeConversations(): LiveData<List<Conversation>>
	fun observeMessages(conversationId: Int): LiveData<List<Message>>
    
    suspend fun syncCourses()
	suspend fun syncConversations()
	suspend fun syncMessages(conversationId: Int)
}