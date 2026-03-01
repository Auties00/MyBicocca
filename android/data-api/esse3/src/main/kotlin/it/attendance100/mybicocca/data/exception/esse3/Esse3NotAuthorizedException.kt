package it.attendance100.mybicocca.data.exception.esse3

import io.ktor.http.HttpStatusCode
import it.attendance100.mybicocca.data.common.exception.ApiRequestException
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel

/**
 * Raised when ESSE3 denies access due to insufficient profile permissions.
 *
 * @property expectedPermissionLevels The profile levels expected by the invoked endpoint
 * @property apiErrorMessage Original ESSE3 error message, if available
 */
class Esse3NotAuthorizedException(
    val expectedPermissionLevels: Set<Esse3PermissionLevel>,
    val apiErrorMessage: String? = null
) : ApiRequestException(HttpStatusCode.Forbidden.value, buildMessage(expectedPermissionLevels, apiErrorMessage)) {
    companion object {

        private fun buildMessage(
            expectedPermissionLevels: Set<Esse3PermissionLevel>,
            apiErrorMessage: String?
        ): String {
            val expected = expectedPermissionLevels
                .ifEmpty { setOf(Esse3PermissionLevel.UNKNOWN) }
                .joinToString("|") { it.name }

            return buildString {
                append("ESSE3 authorization failed. ")
                append(", expectedPermissionLevels=")
                append(expected)
                if (!apiErrorMessage.isNullOrBlank()) {
                    append(", apiErrorMessage=\"")
                    append(apiErrorMessage)
                    append('"')
                }
            }
        }
    }
}