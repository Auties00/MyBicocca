package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningGetForumDiscussionPostsRequest(
    private val discussionId: Int,
    private val includeInlineAttachments: Boolean
) : ElearningRequest<ElearningGetForumDiscussionPostsResponse> {
    override val functionName = "mod_forum_get_discussion_posts"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("discussionid", discussionId.toString())
        if (includeInlineAttachments) formData.append("includeinlineattachments", "1")
    }
}
