package it.attendance100.mybicocca.data.auth

import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.HttpStatusCode
import io.ktor.util.AttributeKey
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class AuthRetryConfig {
    var refresh: suspend () -> Unit = {}
    var onReauthRequired: (Throwable) -> Unit = {}
}

// Coroutine-context marker so calls made *inside* refresh (e.g. auth.login())
// don't re-enter the retry path and recurse forever.
private class AuthRefreshContext : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<AuthRefreshContext>
}

// Per-request flag so a single failing call only retries once.
private val retryAttempted = AttributeKey<Unit>("authRetryAttempted")

val AuthRetry = createClientPlugin("AuthRetry", ::AuthRetryConfig) {
    val refresh = pluginConfig.refresh
    val onReauthRequired = pluginConfig.onReauthRequired

    on(Send) { request ->
        if (currentCoroutineContext()[AuthRefreshContext] != null) {
            return@on proceed(request)
        }
        val firstCall = proceed(request)
        if (firstCall.response.status != HttpStatusCode.Unauthorized) return@on firstCall
        if (request.attributes.contains(retryAttempted)) return@on firstCall
        request.attributes.put(retryAttempted, Unit)
        try {
            withContext(AuthRefreshContext()) { refresh() }
        } catch (cause: Throwable) {
            onReauthRequired(cause)
            return@on firstCall
        }
        proceed(request)
    }
}
