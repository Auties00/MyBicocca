package it.attendance100.mybicocca.ui.component.feedback

import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Shared user-readable error copy for error/empty states across the app. Must not leak
 * internals (no class names, no stack traces) — it renders in release builds.
 */
fun Throwable.friendlyMessage(): String = when (this) {
    is UnknownHostException,
    is ConnectException -> "error_network_unavailable_full"

    is SocketTimeoutException -> "error_network_timeout_full"
    is IOException -> "error_network_generic_full"
    else -> "error_unexpected"
}
