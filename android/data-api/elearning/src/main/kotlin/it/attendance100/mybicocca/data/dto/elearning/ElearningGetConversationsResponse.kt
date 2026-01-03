package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElearningGetConversationsResponse(
    @SerialName("conversations")
    val conversations: List<ElearningConversation> = emptyList()
) : ElearningResponse
