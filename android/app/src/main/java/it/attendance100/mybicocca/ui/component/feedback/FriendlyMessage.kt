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
    is ConnectException -> "Rete non disponibile. Controlla la connessione e riprova."
    is SocketTimeoutException -> "Timeout di rete. Riprova tra un momento."
    is IOException -> "Errore di rete. Riprova tra un momento."
    else -> "Si è verificato un errore imprevisto."
}
