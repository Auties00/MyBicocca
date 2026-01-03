package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.Serializable

@Serializable
data class ElearningDeleteConversationsResponse(
    override val items: List<ElearningWarning>
) : ElearningListResponse<ElearningWarning> {
    val warnings: List<ElearningWarning> get() = items
}
