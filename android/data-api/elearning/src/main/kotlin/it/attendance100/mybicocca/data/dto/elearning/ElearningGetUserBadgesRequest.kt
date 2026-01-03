package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningGetUserBadgesRequest(
    private val userId: Int? = null,
    private val courseId: Int? = null,
    private val page: Int = 0,
    private val perPage: Int = 0,
    private val search: String? = null,
    private val onlyPublic: Boolean = false
) : ElearningRequest<ElearningGetUserBadgesResponse> {
    override val functionName = "core_badges_get_user_badges"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        userId?.let { formData.append("userid", it.toString()) }
        courseId?.let { formData.append("courseid", it.toString()) }
        formData.append("page", page.toString())
        formData.append("perpage", perPage.toString())
        search?.let { formData.append("search", it) }
        if (onlyPublic) formData.append("onlypublic", "1")
    }
}
