package __PACKAGE__

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.AttributeKey
import kotlinx.serialization.json.Json
import kotlin.reflect.KProperty

/**
 * What every request of an API's client carries, as its spec describes it.
 *
 * @property timeoutMillis How long a request may take; null for Ktor's default.
 * @property keepsCookies Whether the client stores the cookies servers set and sends them back.
 * @property caches Whether the client keeps responses in a [ResponseCache].
 * @property headers Headers sent with every request, unless the request sets them.
 * @property query Query parameters sent with every request, unless the request sets them.
 */
class RestClientOptions(
    val timeoutMillis: Long? = null,
    val keepsCookies: Boolean = false,
    val caches: Boolean = false,
    val headers: Map<String, String> = emptyMap(),
    val query: Map<String, String> = emptyMap(),
)

/**
 * The HTTP client of an API: its [options], [settings] and the caller's [config], which runs
 * last so that it can override them. Bodies are encoded with [json].
 */
fun createRestClient(
    json: Json,
    options: RestClientOptions,
    settings: ClientSettings,
    config: HttpClientConfig<*>.() -> Unit,
): HttpClient = HttpClient {
    configure(json, options, settings)
    config()
}

/** Configures a client as [createRestClient] does, before the caller's configuration. */
internal fun HttpClientConfig<*>.configure(json: Json, options: RestClientOptions, settings: ClientSettings) {
    options.timeoutMillis?.let { timeout ->
        install(HttpTimeout) {
            requestTimeoutMillis = timeout
            connectTimeoutMillis = timeout
            socketTimeoutMillis = timeout
        }
    }
    install(ContentNegotiation) {
        json(json)
    }
    if (options.keepsCookies) install(HttpCookies)
    if (options.caches) {
        install(createClientPlugin("ResponseCache") {
            client.attributes.put(ResponseCache.KEY, ResponseCache())
        })
    }
    if (options.headers.isNotEmpty()) {
        install(DefaultRequest) {
            options.headers.forEach { (name, value) -> headers.append(name, value) }
        }
    }
    // DefaultRequest ignores its query parameters for absolute URLs, which every request uses.
    if (options.query.isNotEmpty()) {
        install(createClientPlugin("DefaultQuery") {
            onRequest { request, _ ->
                options.query.forEach { (name, value) -> if (name !in request.url.parameters) request.url.parameters.append(name, value) }
            }
        })
    }
    if (settings.definitions.isNotEmpty()) {
        install(createClientPlugin("ClientSettings") {
            onRequest { request, _ ->
                for (setting in settings.definitions) {
                    val value = settings[setting.name]
                    when (setting.location) {
                        ClientSetting.Location.QUERY -> request.url.parameters[setting.wireName] = value
                        ClientSetting.Location.HEADER -> request.headers[setting.wireName] = value
                    }
                }
            }
        })
    }
    followRedirects = true
}

/** A response read whole: its body as text, and the URL that answered, after redirects. */
class Received(val url: String, val text: String)

/**
 * The responses a client keeps when its API's spec says `caches`, the same on every platform:
 * - a successful GET read whole, not a download, is kept when its `Cache-Control` has
 *   `max-age=N` and neither `no-store` nor `no-cache`, and answers the same URL for N seconds;
 * - any request other than a GET empties it once answered, whatever the answer, since it may
 *   have changed what the kept responses show;
 * - it keeps at most [capacity] responses, dropping the oldest first.
 *
 * The platform's own HTTP cache isn't used, since each platform's follows other rules.
 */
class ResponseCache(private val capacity: Int = 200, private val now: () -> Long = System::currentTimeMillis) {
    private class Entry(val received: Received, val expires: Long)

    private val entries = LinkedHashMap<String, Entry>()

    /** The response kept for [url], if it is still fresh. */
    fun get(url: String): Received? = synchronized(entries) {
        val entry = entries[url] ?: return null
        if (entry.expires > now()) return entry.received
        entries.remove(url)
        null
    }

    /** Keeps [received] for [url], if [cacheControl] allows it. */
    fun put(url: String, received: Received, cacheControl: String?) {
        val seconds = maxAge(cacheControl) ?: return
        synchronized(entries) {
            entries.remove(url)
            entries[url] = Entry(received, now() + seconds * 1000)
            while (entries.size > capacity) entries.remove(entries.keys.first())
        }
    }

    fun clear() = synchronized(entries) { entries.clear() }

    companion object {
        val KEY = AttributeKey<ResponseCache>("ResponseCache")

        /** How many seconds `Cache-Control` lets a response be kept: its `max-age`, unless it forbids keeping it. */
        fun maxAge(cacheControl: String?): Long? {
            val directives = cacheControl?.split(',')?.map { it.trim().lowercase() } ?: return null
            if ("no-store" in directives || "no-cache" in directives) return null
            return directives.firstOrNull { it.startsWith("max-age=") }?.removePrefix("max-age=")?.toLongOrNull()?.takeIf { it > 0 }
        }
    }
}

/**
 * A value sent with every request that the caller may change at any time.
 *
 * @property wireName Name of the query parameter or header.
 * @property default The value until the caller sets one.
 */
class ClientSetting(
    val name: String,
    val location: Location,
    val wireName: String,
    val default: String,
) {
    enum class Location { QUERY, HEADER }
}

/**
 * The current values of an API's [ClientSetting]s, shared by its client and its facade, whose
 * properties delegate to it by name.
 */
class ClientSettings(val definitions: List<ClientSetting>) {
    private val values = definitions.associate { it.name to it.default }.toMutableMap()

    operator fun get(name: String): String = synchronized(values) { values.getValue(name) }

    operator fun set(name: String, value: String) {
        synchronized(values) {
            require(name in values) { "Unknown setting '$name'" }
            values[name] = value
        }
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String = get(property.name)

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) = set(property.name, value)
}
