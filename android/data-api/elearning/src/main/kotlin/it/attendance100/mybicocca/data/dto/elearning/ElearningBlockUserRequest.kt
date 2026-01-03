package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningBlockUserRequest(
    private val userId: Int,
    private val blockedUserId: Int
) : ElearningRequest<ElearningBlockUserResponse> {
    override val functionName = "core_message_block_user"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("userid", userId.toString())
        formData.append("blockeduserid", blockedUserId.toString())
    }
}
