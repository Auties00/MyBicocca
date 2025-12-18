package it.attendance100.mybicocca.domain.model

import java.time.*

sealed interface ElearningActivity {
	val id: Int
	val name: String
	val courseId: Int
	val isVisible: Boolean

	data class Assignment(
		override val id: Int,
		override val name: String,
		override val courseId: Int,
		override val isVisible: Boolean,
		val dueDate: Instant?,
		val allowSubmissionsFromDate: Instant?,
		val cutoffDate: Instant?,
		val status: String? = null,
	) : ElearningActivity

	data class Quiz(
		override val id: Int,
		override val name: String,
		override val courseId: Int,
		override val isVisible: Boolean,
		val timeOpen: Instant?,
		val timeClose: Instant?,
		val attempts: Int? = null,
	) : ElearningActivity

	data class Forum(
		override val id: Int,
		override val name: String,
		override val courseId: Int,
		override val isVisible: Boolean,
		val type: String,
		val unreadCount: Int = 0,
	) : ElearningActivity

	data class Resource(
		override val id: Int,
		override val name: String,
		override val courseId: Int,
		override val isVisible: Boolean,
		val fileUrl: String?,
		val fileType: String?,
	) : ElearningActivity
}
