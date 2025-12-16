package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class SentInstantMessage(
    @SerializedName("msgid") val msgId: Int? = null,
    @SerializedName("clientmsgid") val clientMsgId: String? = null,
    @SerializedName("errormessage") val errorMessage: String? = null
)