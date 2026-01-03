package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningAddForumPostRequest(
    private val postId: Int,
    private val subject: String,
    private val message: String,
    private val options: List<DiscussionOption>? = null
) : ElearningRequest<ElearningAddForumPostResponse> {
    override val functionName = "mod_forum_add_discussion_post"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("postid", postId.toString())
        formData.append("subject", subject)
        formData.append("message", message)
        options?.forEachIndexed { index, option ->
            formData.append("options[$index][name]", option.name)
            formData.append("options[$index][value]", option.value)
        }
    }
}
