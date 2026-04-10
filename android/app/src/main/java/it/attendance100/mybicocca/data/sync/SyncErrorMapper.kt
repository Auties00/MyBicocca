package it.attendance100.mybicocca.data.sync

import kotlinx.coroutines.TimeoutCancellationException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object SyncErrorMapper {
    fun toMessage(throwable: Throwable, isOnline: Boolean): String {
        val cause = throwable.rootCause()
        val statusCode = extractStatusCode(cause)

        return when {
            !isOnline || cause.isOfflineError() ->
                "Sei offline. Sto mostrando gli ultimi dati salvati, se disponibili."

            cause is TimeoutCancellationException || cause is SocketTimeoutException ->
                "La richiesta è scaduta. I servizi di Ateneo stanno rispondendo troppo lentamente."

            cause.hasClassName("AuthenticationException") ->
                "La sessione è scaduta. Effettua di nuovo l'accesso per aggiornare i dati."

            statusCode == 401 ->
                "La sessione è scaduta. Effettua di nuovo l'accesso per aggiornare i dati."

            statusCode in 500..599 ->
                "I servizi di Ateneo non sono disponibili in questo momento. Riprova tra poco."

            statusCode != null ->
                "La richiesta non è andata a buon fine (codice $statusCode). Riprova tra poco."

            cause.hasClassName("HtmlParsingException") ->
                "I dati ricevuti dal servizio non sono nel formato atteso. Riprova piu tardi."

            cause is IllegalStateException && !cause.message.isNullOrBlank() ->
                cause.message!!

            else ->
                cause.localizedMessage?.takeIf { it.isNotBlank() }
                    ?: "Impossibile aggiornare i dati in questo momento."
        }
    }

    private fun Throwable.isOfflineError(): Boolean {
        if (this is UnknownHostException || this is ConnectException) return true
        return javaClass.simpleName == "UnresolvedAddressException"
    }

    private fun Throwable.hasClassName(simpleName: String): Boolean =
        javaClass.simpleName == simpleName || javaClass.name.endsWith(".$simpleName")

    private fun extractStatusCode(throwable: Throwable): Int? {
        val getterCode = runCatching {
            throwable.javaClass.methods
                .firstOrNull { it.name == "getStatusCode" && it.parameterCount == 0 }
                ?.invoke(throwable)
                ?.let { value -> (value as? Number)?.toInt() }
        }.getOrNull()

        if (getterCode != null) return getterCode

        return runCatching {
            val field = throwable.javaClass.getDeclaredField("statusCode")
            field.isAccessible = true
            (field.get(throwable) as? Number)?.toInt()
        }.getOrNull()
    }

    private fun Throwable.rootCause(): Throwable =
        generateSequence(this) { current -> current.cause }.last()
}
