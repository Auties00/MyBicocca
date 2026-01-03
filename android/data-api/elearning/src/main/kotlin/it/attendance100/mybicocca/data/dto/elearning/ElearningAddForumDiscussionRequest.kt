package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningAddForumDiscussionRequest(
    private val forumId: Int,
    private val subject: String,
    private val message: String,
    private val groupId: Int? = null,
    private val options: List<DiscussionOption>? = null
) : ElearningRequest<ElearningAddForumDiscussionResponse> {
    override val functionName = "mod_forum_add_discussion"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("forumid", forumId.toString())
        formData.append("subject", subject)
        formData.append("message", message)
        groupId?.let { formData.append("groupid", it.toString()) }
        options?.forEachIndexed { index, option ->
            formData.append("options[$index][name]", option.name)
            formData.append("options[$index][value]", option.value)
        }
    }
}

@Serializable
data class DiscussionOption(
    val name: String,
    val value: String
)
