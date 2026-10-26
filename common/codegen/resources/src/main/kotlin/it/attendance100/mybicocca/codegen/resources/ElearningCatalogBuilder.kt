package it.attendance100.mybicocca.codegen.resources

import it.attendance100.mybicocca.codegen.core.ResourceBuilder
import it.attendance100.mybicocca.codegen.core.ResourceContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.jsoup.nodes.Element
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap

/**
 * The catalog of the courses published on the university's Moodle, https://elearning.unimib.it:
 * its areas, then their categories, nested as on the site, with the courses each lists. It walks
 * the homepage's areas and every category page below them, as the app's pages would.
 */
class ElearningCatalogBuilder : ResourceBuilder {
    override val id: String = "elearning-catalog"

    override fun build(context: ResourceContext): JsonObject = runBlocking {
        val fetcher = Fetcher(CONCURRENCY, HEADERS)
        val visited = ConcurrentHashMap.newKeySet<String>()

        suspend fun category(name: String, url: String): JsonObject {
            // A category reached again, through another parent, is listed there without its contents.
            if (!visited.add(url)) return node(name, url, emptyList(), emptyList())
            val contents = try {
                contents(fetcher.get(url))
            } catch (e: FetchException) {
                context.log("  ! ${e.message}")
                Contents("", emptyList(), emptyList())
            }
            val children = coroutineScope { contents.subcategories.map { (subName, subUrl) -> async { category(subName, subUrl) } }.awaitAll() }
            return node(contents.name.ifEmpty { name }, url, children, contents.courses)
        }

        context.log("Fetching the course areas of $BASE_URL")
        val home = fetcher.get(BASE_URL).document
        val areas = home.select("div.frontpage-box").map { section ->
            val categories = section.select("div.card .navigation-block").mapNotNull { item ->
                val link = item.selectFirst("a.card-img-overlay") ?: return@mapNotNull null
                val url = link.absUrl("href").ifEmpty { return@mapNotNull null }
                strippedText(link.selectFirst("h5.card-title")).ifEmpty { "Unknown Category" } to url
            }
            strippedText(section.selectFirst("h2.navigation-title")).ifEmpty { "Unknown Section" } to categories
        }
        context.log("Found ${areas.size} areas and ${areas.sumOf { it.second.size }} categories; walking the category tree")
        val built = areas.map { (name, categories) ->
            val nodes = coroutineScope { categories.map { (categoryName, url) -> async { category(categoryName, url) } }.awaitAll() }
            buildJsonObject {
                put("name", name)
                put("categories", JsonArray(nodes))
            }
        }
        if (fetcher.failed.isNotEmpty()) context.log("  ! ${fetcher.failed.size} categories kept failing: ${fetcher.failed.joinToString()}")
        buildJsonObject {
            put("fetched_at", Instant.now().truncatedTo(ChronoUnit.SECONDS).toString())
            put("areas", JsonArray(built))
        }
    }

    private class Contents(val name: String, val subcategories: List<Pair<String, String>>, val courses: List<JsonObject>)

    /** A category page: its name, its subcategories and its courses. */
    private fun contents(page: Page): Contents {
        val document = page.document
        val subcategories = document.select("div.subcategories .category > a.info").mapNotNull { link ->
            val nameTag = link.selectFirst("h3.categoryname")
            nameTag?.select(".sr-only")?.remove()
            val url = link.absUrl("href").ifEmpty { return@mapNotNull null }
            strippedText(nameTag).ifEmpty { "Unknown Category" } to url
        }
        // Whether a course needs an enrolment key isn't listed: only its own enrolment page says.
        val courses = document.select("div.coursebox").mapNotNull { box -> course(box) }
        return Contents(strippedText(document.selectFirst("h1")).ifEmpty { "Unknown Category" }, subcategories, courses)
    }

    private fun course(box: Element): JsonObject? {
        val link = box.selectFirst("a.coursename") ?: return null
        val url = link.absUrl("href")
        val title = box.selectFirst(".course-fullname")?.let(::strippedText)
            ?: link.attr("title").trim().ifEmpty { null }
            ?: strippedText(link)
        val id = box.attr("data-courseid").trim().toIntOrNull() ?: queryParameter(url, "id")?.toIntOrNull() ?: return null
        return buildJsonObject {
            put("id", id)
            put("name", title)
            put("code", strippedText(box.selectFirst(".course-shortname")))
            put("url", url)
        }
    }

    private fun node(name: String, url: String, categories: List<JsonObject>, courses: List<JsonObject>) = buildJsonObject {
        put("name", JsonPrimitive(name))
        put("url", JsonPrimitive(url))
        put("categories", JsonArray(categories))
        put("courses", JsonArray(courses))
    }

    private companion object {
        const val BASE_URL = "https://elearning.unimib.it/"
        const val CONCURRENCY = 4
        val HEADERS = mapOf(
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36",
            "Accept-Language" to "it-IT,it;q=0.9,en;q=0.8",
        )
    }
}
