package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElearningRemoveUserDeviceResponse(
    @SerialName("removed")
    val removed: Boolean,
    @SerialName("warnings")
    val warnings: List<ElearningWarning> = emptyList()
) : ElearningResponse
