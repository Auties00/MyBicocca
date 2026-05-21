package it.attendance100.mybicocca.data.auth

import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import it.attendance100.mybicocca.data.local.credentials.AccountCredentials
import it.attendance100.mybicocca.data.remote.esse3.api.Esse3Api
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Esse3ApiFactory @Inject constructor() {

    fun create(
        credentials: AccountCredentials,
        refresh: suspend () -> Unit,
        onReauthRequired: (Throwable) -> Unit,
    ): Esse3Api = Esse3Api {
        install(HttpTimeout) {
            connectTimeoutMillis = TIMEOUT_MS
            socketTimeoutMillis = TIMEOUT_MS
            requestTimeoutMillis = TIMEOUT_MS
        }
        defaultRequest {
            val token = "${credentials.username}:${credentials.password}"
                .toByteArray(Charsets.UTF_8)
                .let(Base64.getEncoder()::encodeToString)
            header("Authorization", "Basic $token")
        }
        install(AuthRetry) {
            this.refresh = refresh
            this.onReauthRequired = onReauthRequired
        }
    }

    private companion object {
        const val TIMEOUT_MS = 30_000L
    }
}
