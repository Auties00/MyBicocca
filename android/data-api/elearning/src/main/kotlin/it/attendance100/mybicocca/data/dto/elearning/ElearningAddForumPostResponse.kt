package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElearningAddForumPostResponse(
    @SerialName("postid")
    val postId: Int,
    @SerialName("warnings")
    val warnings: List<ElearningWarning> = emptyList(),
    @SerialName("post")
    val post: ElearningForumPost? = null,
    @SerialName("messages")
    val messages: List<ElearningForumMessage>? = null
) : ElearningResponse

@Serializable
data class ElearningForumMessage(
    @SerialName("type")
    val type: String? = null,
    @SerialName("message")
    val message: String? = null
)
