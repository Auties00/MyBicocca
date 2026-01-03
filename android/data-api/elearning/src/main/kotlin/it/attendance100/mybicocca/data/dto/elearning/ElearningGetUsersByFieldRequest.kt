package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningGetUsersByFieldRequest(
    private val field: String,
    private val values: List<String>
) : ElearningRequest<ElearningGetUsersByFieldResponse> {
    override val functionName = "core_user_get_users_by_field"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("field", field)
        values.forEachIndexed { index, value ->
            formData.append("values[$index]", value)
        }
    }

    companion object {
        fun byId(userIds: List<Int>) = ElearningGetUsersByFieldRequest(
            "id",
            userIds.map { it.toString() }
        )

        fun byUsername(usernames: List<String>) = ElearningGetUsersByFieldRequest(
            "username",
            usernames
        )

        fun byEmail(emails: List<String>) = ElearningGetUsersByFieldRequest(
            "email",
            emails
        )

        fun byIdNumber(idNumbers: List<String>) = ElearningGetUsersByFieldRequest(
            "idnumber",
            idNumbers
        )
    }
}
