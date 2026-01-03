package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElearningGetConversationMessagesResponse(
    @SerialName("id")
    val id: Int,
    @SerialName("members")
    val members: List<ElearningConversationMember> = emptyList(),
    @SerialName("messages")
    val messages: List<ElearningMessageItem> = emptyList()
) : ElearningResponse
