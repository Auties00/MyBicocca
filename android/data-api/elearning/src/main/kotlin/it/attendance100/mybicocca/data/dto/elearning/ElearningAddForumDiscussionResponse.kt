package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElearningAddForumDiscussionResponse(
    @SerialName("discussionid")
    val discussionId: Int,
    @SerialName("warnings")
    val warnings: List<ElearningWarning> = emptyList()
) : ElearningResponse
