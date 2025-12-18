package it.attendance100.mybicocca.data.dao

import androidx.lifecycle.*
import androidx.room.*
import it.attendance100.mybicocca.domain.model.*

@Dao
interface ElearningDao {
	// --- Courses ---
	@Query("SELECT * FROM elearning_courses ORDER BY start_date DESC")
	fun observeCourses(): LiveData<List<ElearningCourse>>


	// Helper for filtered queries (implementing partial selector logic here)
	@Query("SELECT * FROM elearning_courses WHERE is_favorite = 1 ORDER BY start_date DESC")
	fun observeFavoriteCourses(): LiveData<List<ElearningCourse>>


	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertCourses(courses: List<ElearningCourse>)


	@Query("DELETE FROM elearning_courses")
	suspend fun clearCourses()


	// --- Conversations ---
	@Query("SELECT * FROM conversations ORDER BY last_message_date DESC")
	fun observeConversations(): LiveData<List<Conversation>>


	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertConversations(conversations: List<Conversation>)


	// --- Messages ---
	@Query("SELECT * FROM messages WHERE conversation_id = :conversationId ORDER BY time_created ASC")
	fun observeMessages(conversationId: Int): LiveData<List<Message>>


	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertMessages(messages: List<Message>)
}
