package it.attendance100.mybicocca.codegen.resources

import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import kotlinx.coroutines.sync.Semaphore
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
import kotlin.random.Random

/** A page as fetched: its HTML, and the URL that answered, after redirects, which its links resolve against. */
internal class Page(val url: String, val html: String) {
    val document: Document by lazy { Jsoup.parse(html, url) }
}

internal class FetchException(message: String) : Exception(message)

/**
 * Fetches pages of a website, at most [concurrency] at a time, trying each again a few times
 * with growing pauses when it fails.
 *
 * @param freshSessions Whether every request starts a session of its own, carrying the cookies
 *   only across its own redirects: some servers serve a stripped page to a session that already
 *   had one.
 */
internal class Fetcher(
    concurrency: Int,
    private val headers: Map<String, String>,
    private val freshSessions: Boolean = false,
    private val attempts: Int = 4,
    private val backoffSeconds: Double = 1.5,
) {
    private val client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(15))
        .followRedirects(if (freshSessions) HttpClient.Redirect.NEVER else HttpClient.Redirect.NORMAL)
        .build()
    private val permits = Semaphore(concurrency)

    /** The pages that kept failing. */
    val failed = ConcurrentLinkedQueue<String>()

    /** The page at [url]; with [mustContain], a page without that text counts as a failure. */
    suspend fun get(url: String, mustContain: String? = null): Page {
        val errors = mutableListOf<String>()
        for (attempt in 1..attempts) {
            try {
                val page = permits.withPermit { fetch(url) }
                if (mustContain == null || mustContain in page.html) return page
                errors += "the page lacks '$mustContain' (${page.html.length} characters)"
            } catch (e: Exception) {
                errors += "${e::class.simpleName}: ${e.message}"
            }
            if (attempt < attempts) {
                // Pauses outside the permit, so that other requests go on meanwhile.
                val pause = backoffSeconds * (1 shl (attempt - 1))
                delay(((pause + Random.nextDouble(0.0, pause * 0.25)) * 1000).toLong())
            }
        }
        failed += url
        throw FetchException("failed after $attempts attempts: $url\n" + errors.withIndex().joinToString("\n") { (i, e) -> "    [${i + 1}] $e" })
    }

    private suspend fun fetch(url: String): Page {
        var target = URI(url)
        val cookies = linkedMapOf<String, String>()
        repeat(MAX_REDIRECTS) {
            val request = HttpRequest.newBuilder(target).timeout(Duration.ofSeconds(30)).GET().apply {
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
