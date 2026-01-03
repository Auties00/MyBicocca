package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningGetForumDiscussionsRequest(
    private val forumId: Int,
    private val sortOrder: ElearningSortOrder,
    private val page: Int,
    private val perPage: Int,
    private val groupId: Int
) : ElearningRequest<ElearningGetForumDiscussionsResponse> {
    override val functionName = "mod_forum_get_forum_discussions"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("forumid", forumId.toString())
        formData.append("sortorder", sortOrder.id.toString())
        formData.append("page", page.toString())
        formData.append("perpage", perPage.toString())
        formData.append("groupid", groupId.toString())
    }
}
