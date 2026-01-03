package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningUnblockUserRequest(
    private val userId: Int,
    private val unblockedUserId: Int
) : ElearningRequest<ElearningUnblockUserResponse> {
    override val functionName = "core_message_unblock_user"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("userid", userId.toString())
        formData.append("unblockeduserid", unblockedUserId.toString())
    }
}
