package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.Serializable

@Serializable
data class ElearningGetForumsResponse(
    override val items: List<ElearningForum>
) : ElearningListResponse<ElearningForum> {
    val forums: List<ElearningForum> get() = items
}
