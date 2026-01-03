package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
data class InstantMessage(
    val toUserId: Int,
    val text: String
)

@Serializable
class ElearningSendInstantMessagesRequest(
    private val messages: List<InstantMessage>
) : ElearningRequest<ElearningSendInstantMessagesResponse> {
    override val functionName = "core_message_send_instant_messages"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        messages.forEachIndexed { index, msg ->
            formData.append("messages[$index][touserid]", msg.toUserId.toString())
            formData.append("messages[$index][text]", msg.text)
            formData.append("messages[$index][textformat]", "1")
        }
    }
}
