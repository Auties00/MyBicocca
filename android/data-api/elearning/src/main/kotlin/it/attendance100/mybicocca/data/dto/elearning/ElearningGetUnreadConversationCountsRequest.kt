package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningGetUnreadConversationCountsRequest(
    private val userId: Int
) : ElearningRequest<ElearningGetUnreadConversationCountsResponse> {
    override val functionName = "core_message_get_unread_conversation_counts"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("userid", userId.toString())
    }
}
