package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

/**
 * Conversation type filter.
 */
@Serializable
sealed class ConversationType(val value: Int) {
    data object Individual : ConversationType(1)
    data object Group : ConversationType(2)
    data object Self : ConversationType(3)
}

@Serializable
class ElearningGetConversationsRequest(
    private val userId: Int,
    private val limitFrom: Int = 0,
    private val limitNum: Int = 20,
    private val type: ConversationType? = null,
    private val favourites: Boolean? = null,
    private val mergeSelf: Boolean = false
) : ElearningRequest<ElearningGetConversationsResponse> {
    override val functionName = "core_message_get_conversations"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("userid", userId.toString())
        formData.append("limitfrom", limitFrom.toString())
        formData.append("limitnum", limitNum.toString())
        type?.let { formData.append("type", it.value.toString()) }
        favourites?.let { formData.append("favourites", if (it) "1" else "0") }
        if (mergeSelf) formData.append("mergeself", "1")
    }
}
