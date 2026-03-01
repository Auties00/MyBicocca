package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Permission levels declared in ESSE3 OpenAPI specifications (`kind` field).
 */
enum class Esse3PermissionLevel {
    ANY,
    AUTHENTICATED_USER,
    TEACHER,
    PROVISIONAL_ENROLLED_STUDENT,
    REGISTERED_USER,
    ADMIN_OFFICE,
    ADMIN_OFFICE_ADMIN,
    EXTERNAL_SUBJECT,
    STUDENT,
    TECHNICAL_USER,
    UNKNOWN
}

/**
 * Standard error response returned by the ESSE3 REST API.
 */
@Serializable
data class Esse3ErrorResponse(
    @SerialName("statusCode")
    val statusCode: Int? = null,

    @SerialName("retCode")
    val returnCode: Int? = null,

    @SerialName("retErrMsg")
    val errorMessage: String = "",

    @SerialName("errDetails")
    val errorDetails: List<Esse3ErrorDetail> = emptyList()
)

/**
 * Individual error detail within an ESSE3 error response.
 */
@Serializable
data class Esse3ErrorDetail(
    @SerialName("errorType")
    val errorType: String,

    @SerialName("value")
    val value: String? = null,

    @SerialName("rawValue")
    val rawValue: String? = null
)
