package it.attendance100.mybicocca.data.remote.esse3.api

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * How the generated [Esse3Api] facade talks to Esse3: the JSON settings and the HTTP client
 * shared by every module.
 */
internal object Esse3Runtime {
    /**
     * Lenient on input, because Esse3's responses don't always match its specs: unknown keys
     * are ignored and invalid values fall back to the field's default.
     */
    val json = Json {
        coerceInputValues = true
        encodeDefaults = true
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
    }

    fun createHttpClient(json: Json, config: HttpClientConfig<*>.() -> Unit): HttpClient = HttpClient {
        config()

        install(ContentNegotiation) {
            json(json)
        }

        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }

        install(HttpCache)

        followRedirects = true
    }
}
