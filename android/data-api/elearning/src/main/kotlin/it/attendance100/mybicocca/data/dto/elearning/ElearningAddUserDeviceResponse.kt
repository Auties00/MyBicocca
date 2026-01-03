package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElearningAddUserDeviceResponse(
    override val items: List<ElearningDeviceResult>
) : ElearningListResponse<ElearningDeviceResult>

@Serializable
data class ElearningDeviceResult(
    @SerialName("id")
    val id: Int? = null,
    @SerialName("pushid")
    val pushId: String? = null,
    @SerialName("warnings")
    val warnings: List<ElearningWarning> = emptyList()
)
