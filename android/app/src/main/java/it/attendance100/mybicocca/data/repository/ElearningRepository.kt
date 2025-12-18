package it.attendance100.mybicocca.data.repository

import androidx.lifecycle.*
import it.attendance100.mybicocca.data.api.elearning.*
import it.attendance100.mybicocca.data.dto.elearning.*
import it.attendance100.mybicocca.di.*
import it.attendance100.mybicocca.domain.model.*
import it.attendance100.mybicocca.domain.model.Conversation
import it.attendance100.mybicocca.manager.*
import javax.inject.*
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

    override suspend fun syncCourses() {
	    val userId = storageManager.userPersonId // Assuming this maps to Moodle User ID, otherwise we need a separate ID
	    // Note: Moodle User ID might differ from Person ID. Usually we get it from a 'SiteInfo' call after login.
	    // For now using userPersonId as placeholder.
	    val request = GetUsersCoursesRequest(userid = userId)

	    val response = api.course.getUsersCourses(request)
	    if (response.isSuccessful) {
		    val dtoList = response.body() ?: emptyList()
		    val domainList = dtoList.map { dto ->
			    ElearningCourse(
				    id = dto.id ?: 0,
				    fullName = dto.fullName ?: "",
				    shortName = dto.shortName ?: "",
				    summary = dto.summary,
				    startDate = java.time.Instant.ofEpochSecond(dto.startDate?.toLong() ?: 0),
				    endDate = java.time.Instant.ofEpochSecond(dto.endDate?.toLong() ?: 0),
				    isVisible = dto.visible == 1,
				    progress = dto.progress?.toInt(),
				    isFavorite = dto.isFavourite == true,
			    )
		    }
		    database.elearningDao().insertCourses(domainList)
	    }
    }

	override suspend fun syncConversations() {
		val userId = storageManager.userPersonId
		val request = GetConversationsRequest(userId = userId)

		val response = api.message.getConversations(request)
		if (response.isSuccessful) {
			val convs = response.body()?.conversations ?: emptyList()
			val domainList = convs.map { dto ->
				// DTO members is List<ConversationMember>
				val otherMember = dto.members?.firstOrNull { it.id != userId }
					?: dto.members?.firstOrNull()

				Conversation(
					id = dto.id ?: 0,
					name = dto.name,
					otherUserId = otherMember?.id,
					otherUserName = otherMember?.fullName,
					otherUserAvatarUrl = otherMember?.profileImageUrl?.toString(),
					lastMessage = dto.messages?.lastOrNull()?.text,
					lastMessageDate = java.time.Instant.ofEpochSecond(dto.messages?.lastOrNull()?.timeCreated?.toLong() ?: 0),
					unreadCount = dto.unreadCount ?: 0,
					isFavorite = dto.isFavourite == true,
				)
			}
			database.elearningDao().insertConversations(domainList)
		}
	}

	override suspend fun syncMessages(conversationId: Int) {
		val userId = storageManager.userPersonId
		val request = GetConversationMessagesRequest(
			currentUserId = userId,
			convId = conversationId,
		)
		val response = api.message.getConversationMessages(request)
		if (response.isSuccessful) {
			val msgs = response.body()?.messages ?: emptyList()
			val domainList = msgs.map { dto ->
				Message(
					id = dto.id ?: 0,
					conversationId = conversationId,
					senderId = dto.userIdFrom ?: 0,
					senderName = null,
					text = dto.text ?: "",
					timeCreated = java.time.Instant.ofEpochSecond(dto.timeCreated?.toLong() ?: 0),
					isRead = false,
				)
			}
			database.elearningDao().insertMessages(domainList)
		}
    }
}