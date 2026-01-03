package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningMarkAllConversationMessagesAsReadRequest(
    private val userId: Int,
    private val conversationId: Int
) : ElearningRequest<ElearningMarkAsReadResponse> {
    override val functionName = "core_message_mark_all_conversation_messages_as_read"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("userid", userId.toString())
        formData.append("conversationid", conversationId.toString())
    }
}
