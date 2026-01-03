package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningGetConversationMessagesRequest(
    private val currentUserId: Int,
    private val conversationId: Int,
    private val limitFrom: Int = 0,
    private val limitNum: Int = 20,
    private val newestFirst: Boolean = true,
    private val timeFrom: Long = 0
) : ElearningRequest<ElearningGetConversationMessagesResponse> {
    override val functionName = "core_message_get_conversation_messages"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("currentuserid", currentUserId.toString())
        formData.append("convid", conversationId.toString())
        formData.append("limitfrom", limitFrom.toString())
        formData.append("limitnum", limitNum.toString())
        if (newestFirst) formData.append("newest", "1")
        if (timeFrom > 0) formData.append("timefrom", timeFrom.toString())
    }
}
