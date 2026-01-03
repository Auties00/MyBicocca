package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElearningUnblockUserResponse(
    @SerialName("warnings")
    val warnings: List<ElearningWarning> = emptyList()
) : ElearningResponse
