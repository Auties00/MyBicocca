package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningDeleteConversationsRequest(
    private val userId: Int,
    private val conversationIds: List<Int>
) : ElearningRequest<ElearningDeleteConversationsResponse> {
    override val functionName = "core_message_delete_conversations_by_id"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("userid", userId.toString())
        conversationIds.forEachIndexed { index, id ->
            formData.append("conversationids[$index]", id.toString())
        }
    }
}
