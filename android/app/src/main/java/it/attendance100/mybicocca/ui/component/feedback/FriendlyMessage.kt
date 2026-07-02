package it.attendance100.mybicocca.ui.component.feedback

import it.attendance100.mybicocca.R
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Shared user-readable error copy for error/empty states across the app. Must not leak
 * internals (no class names, no stack traces) — it renders in release builds.
 */
fun Throwable.friendlyMessage(): Int = when (this) {
    is UnknownHostException,
    is ConnectException -> R.string.error_network_unavailable_full

    is SocketTimeoutException -> R.string.error_network_timeout_full
    is IOException -> R.string.error_network_generic_full
    else -> R.string.error_unexpected
}
