package it.attendance100.mybicocca.codegen.resources

import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

/** A page as fetched: its HTML, and the URL that answered, after redirects, which its links resolve against. */
internal class Page(val url: String, val html: String) {
    val document: Document by lazy { Jsoup.parse(html, url) }
}

internal class FetchException(message: String) : Exception(message)

/**
 * Fetches pages of a website, at most [concurrency] at a time, trying each again with growing
 * pauses when it fails.
 *
 * @param freshSessions Whether every request starts a session of its own, carrying the cookies
 *   only across its own redirects: some servers serve a stripped page to a session that already
 *   had one.
 * @param timeoutSeconds How long a request may wait for its response before it is tried again.
 * @param requestsPerSecond How many requests may start each second, for a server that answers
 *   a burst quickly but slows down for everyone when it lasts; no limit when null.
 */
internal class Fetcher(
    concurrency: Int,
    private val headers: Map<String, String>,
    private val freshSessions: Boolean = false,
    private val attempts: Int = 4,
    private val backoffSeconds: Double = 1.5,
    private val maxPauseSeconds: Double = 30.0,
    private val timeoutSeconds: Long = 30,
    requestsPerSecond: Double? = null,
) {
    private val client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(minOf(timeoutSeconds, 15)))
        .followRedirects(if (freshSessions) HttpClient.Redirect.NEVER else HttpClient.Redirect.NORMAL)
        .build()
    private val permits = Semaphore(concurrency)
    private val interval = requestsPerSecond?.let { (1_000_000_000 / it).toLong() } ?: 0L
    private val pacing = Mutex()
    private var nextStart = 0L
    private val requests = AtomicInteger()
    private val incomplete = AtomicInteger()
    private val errors = AtomicInteger()

    /** The pages that kept failing. */
    val failed = ConcurrentLinkedQueue<String>()

    /** How the requests went so far. */
    fun stats(): String = "${requests.get()} requests, ${incomplete.get()} incomplete, ${errors.get()} failed"

    /**
     * The page at [url]. A page that isn't [complete] counts as a failure: [lacking] says what
     * such a page lacks.
     */
    suspend fun get(url: String, lacking: String? = null, complete: (Page) -> Boolean = { true }): Page {
        val problems = mutableListOf<String>()
        for (attempt in 1..attempts) {
            try {
                val page = permits.withPermit {
                    pace()
                    requests.incrementAndGet()
                    fetch(url)
                }
                if (complete(page)) return page
                incomplete.incrementAndGet()
                problems += "the page lacks ${lacking ?: "its content"} (${page.html.length} characters)"
            } catch (e: Exception) {
                errors.incrementAndGet()
                problems += "${e::class.simpleName}: ${e.message}"
            }
            if (attempt < attempts) {
                // Pauses outside the permit, so that other requests go on meanwhile.
                val pause = minOf(backoffSeconds * (1 shl minOf(attempt - 1, 16)), maxPauseSeconds)
                delay(((pause + Random.nextDouble(0.0, pause * 0.25)) * 1000).toLong())
            }
        }
        failed += url
        throw FetchException("failed after $attempts attempts: $url\n" + problems.distinct().joinToString("\n") { "    $it" })
    }

    /** Waits for the next request's turn, when requests are limited per second. */
    private suspend fun pace() {
        if (interval == 0L) return
        val wait = pacing.withLock {
            val now = System.nanoTime()
            val start = maxOf(now, nextStart)
            nextStart = start + interval
            start - now
        }
        if (wait > 0) delay(wait / 1_000_000)
    }

    private suspend fun fetch(url: String): Page {
        var target = URI(url)
        val cookies = linkedMapOf<String, String>()
        repeat(MAX_REDIRECTS) {
            val request = HttpRequest.newBuilder(target).timeout(Duration.ofSeconds(timeoutSeconds)).GET().apply {
                headers.forEach { (name, value) -> header(name, value) }
                if (cookies.isNotEmpty()) header("Cookie", cookies.entries.joinToString("; ") { (name, value) -> "$name=$value" })
            }.build()
            val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).await()
            val status = response.statusCode()
            if (freshSessions && status in 300..399) {
                for (setCookie in response.headers().allValues("Set-Cookie")) {
                    val pair = setCookie.substringBefore(';')
                    cookies[pair.substringBefore('=').trim()] = pair.substringAfter('=', "").trim()
                }
                val location = response.headers().firstValue("Location").orElseThrow { FetchException("redirect without a location from $target") }
                target = target.resolve(location)
                return@repeat
            }
            if (status !in 200..299) throw FetchException("status $status from $target")
            return Page(response.uri().toString(), response.body())
        }
        throw FetchException("more than $MAX_REDIRECTS redirects from $url")
    }

    private companion object {
        const val MAX_REDIRECTS = 10
    }
}

/**
 * The text of [element] as BeautifulSoup's `get_text(strip=True)` reads it: each of its text
 * pieces with surrounding whitespace removed, joined with nothing. Empty for no element.
 */
internal fun strippedText(element: Element?): String {
    if (element == null) return ""
    val pieces = mutableListOf<String>()
    fun walk(node: org.jsoup.nodes.Node) {
        when (node) {
            is TextNode -> node.wholeText.trim().takeIf { it.isNotEmpty() }?.let(pieces::add)
            else -> node.childNodes().forEach(::walk)
        }
    }
    walk(element)
    return pieces.joinToString("")
}

/** The value of query parameter [name] of [url], if it has one. */
internal fun queryParameter(url: String, name: String): String? =
    runCatching { URI(url).rawQuery }.getOrNull()?.split('&')
        ?.map { it.substringBefore('=') to java.net.URLDecoder.decode(it.substringAfter('=', ""), Charsets.UTF_8) }
        ?.firstOrNull { it.first == name }?.second
