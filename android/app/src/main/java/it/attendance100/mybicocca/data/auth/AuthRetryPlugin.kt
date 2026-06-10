package it.attendance100.mybicocca.data.auth

import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.HttpStatusCode
import io.ktor.util.AttributeKey
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * Configuration for the AuthRetry plugin: [refresh] re-establishes the session after an HTTP
 * 401, [onReauthRequired] is invoked with the cause when the refresh itself fails.
 */
class AuthRetryConfig {
    var refresh: suspend () -> Unit = {}
    var onReauthRequired: (Throwable) -> Unit = {}
}

/**
 * Coroutine-context marker exempting requests issued from inside the refresh callback (e.g. a
 * login call) from the retry path, which would otherwise recurse forever.
 */
private class AuthRefreshContext : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<AuthRefreshContext>
}

/** Per-request flag so a single failing call retries at most once. */
private val retryAttempted = AttributeKey<Unit>("authRetryAttempted")

/**
 * Ktor client plugin that retries a request once after an HTTP 401: it runs the configured
 * refresh and replays the original call. When the refresh throws, the failure is reported
 * through `onReauthRequired` and the original 401 response is returned to the caller.
 */
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
