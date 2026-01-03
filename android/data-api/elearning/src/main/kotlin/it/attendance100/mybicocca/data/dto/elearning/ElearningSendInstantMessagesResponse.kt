package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElearningSendInstantMessagesResponse(
    override val items: List<ElearningSentMessage>
) : ElearningListResponse<ElearningSentMessage>

@Serializable
data class ElearningSentMessage(
    @SerialName("msgid")
    val msgId: Int? = null,
    @SerialName("clientmsgid")
    val clientMsgId: String? = null,
    @SerialName("errormessage")
    val errorMessage: String? = null
)
