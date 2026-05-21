package it.attendance100.mybicocca.data.remote.esse3.exception

import it.attendance100.mybicocca.data.remote.common.exception.ApiRequestException
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ErrorResponse
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PermissionLevel

/**
 * Raised when the ESSE3 server rejects a request body due to validation errors (HTTP 422).
 *
 * @property errorResponse The parsed error detail returned by the server, if available
 */
class Esse3Exception(
    val expectedPermissionLevels: Set<Esse3PermissionLevel>,
    val errorResponse: Esse3ErrorResponse
) : ApiRequestException(errorResponse.statusCode ?: -1, buildMessage(errorResponse, expectedPermissionLevels)) {
    companion object {
        private fun buildMessage(
            errorResponse: Esse3ErrorResponse,
            expectedPermissionLevels: Set<Esse3PermissionLevel>
        ): String {
            return buildString {
                appendLine("ESSE3 request failed")
                appendLine("  Status code: ${errorResponse.statusCode ?: "unknown"}")
                if (errorResponse.returnCode != null) {
                    appendLine("  Return code: ${errorResponse.returnCode}")
                }
                if (expectedPermissionLevels.isNotEmpty()) {
                    appendLine("  Expected permissions: ${expectedPermissionLevels.joinToString { it.name }}")
                }
                val msg = errorResponse.errorMessage
                if (msg.isNotBlank()) {
                    appendLine("  Message: $msg")
                }
                val details = errorResponse.errorDetails
                if (details.isNotEmpty()) {
                    appendLine("  Details:")
                    for (detail in details) {
                        append("    - ${detail.errorType}")
                        val value = detail.value ?: detail.rawValue
                        if (value != null) append(": $value")
                        appendLine()
                    }
                }
            }.trimEnd()
        }
    }
}
