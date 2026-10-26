package it.attendance100.mybicocca.codegen.resources

import it.attendance100.mybicocca.codegen.core.ResourceBuilder
import it.attendance100.mybicocca.codegen.core.ResourceContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.jsoup.nodes.Document
import org.jsoup.nodes.TextNode
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * The students' opinions of the courses, from https://opinionistudenti.unimib.it/valdid/, by
 * academic year and then course: each teacher's three ratings, and the course's, weighted by
 * the teachers' questionnaires. Departments and study courses only lead to the courses.
 *
 * Ratings are on the 1-10 scale used from the academic year 2019/2020; earlier ones, on a 0-3
 * scale, become `1 + 3 × rating`, which maps the bands' boundaries exactly. A teacher whose
 * ratings aren't published has none, and a course none when none of its teachers has.
 *
 * The site now and then serves a page with some of its sections blank: a page counts as
 * fetched only once it has every section the crawl reads, and is asked for again until then.
 * It also slows down for everyone when asked too much, so the crawl keeps to a few requests
 * at a time, at a steady pace, and waits for each.
 *
 * Past academic years don't change, so only the last two the resource has are fetched again,
 * along with any later or missing one. Years are fetched one at a time, the latest first, and
 * each is saved once fetched, so that a crawl stopped halfway resumes from the years it finished.
 */
class CourseRatingsBuilder : ResourceBuilder {
    override val id: String = "course-ratings"

    override fun build(context: ResourceContext): JsonObject = runBlocking {
        // A session that has served one page gets the next stripped of its data, so every request has its own.
        val fetcher = Fetcher(
            CONCURRENCY, HEADERS, freshSessions = true,
            attempts = ATTEMPTS, backoffSeconds = 1.0, maxPauseSeconds = 15.0, timeoutSeconds = TIMEOUT_SECONDS,
            requestsPerSecond = REQUESTS_PER_SECOND,
        )
        val previous = context.previous?.get("academic_years")?.jsonObject.orEmpty()
            .mapKeys { it.key.toInt() }.mapValues { it.value.jsonObject }
        // The last two years the resource has, and any later, are fetched again.
        val lastKept = previous.keys.sortedDescending().take(2).minOrNull() ?: Int.MAX_VALUE
        val fetched = AtomicInteger()
        val done = sortedMapOf<Int, JsonObject>().apply { putAll(previous) }
        fun data() = buildJsonObject {
            put("fetched_at", Instant.now().truncatedTo(ChronoUnit.SECONDS).toString())
            put("academic_years", JsonObject(done.mapKeys { it.key.toString() }))
        }
        // A course counts once its teachers are fetched, which comes after its year's other courses: the requests show the way meanwhile.
        val progress = launch {
            while (true) {
                delay(PROGRESS_INTERVAL)
                context.log("  ${fetched.get()} courses fetched, ${fetcher.stats()}")
            }
        }
        for (year in LocalDate.now().year downTo FIRST_YEAR) {
            if (year < lastKept && year in previous) continue
            // A year that fails now keeps what it had.
            val data = Crawl(fetcher, year, context, fetched).yearOrNull() ?: continue
            done[year] = data
            context.checkpoint(data())
        }
        progress.cancel()
        context.log("Requests: ${fetcher.stats()}")
        if (fetcher.failed.isNotEmpty()) context.log("  ! ${fetcher.failed.size} pages kept failing")
        data()
    }

    private class Crawl(val fetcher: Fetcher, val year: Int, val context: ResourceContext, val fetched: AtomicInteger) {
        /** The year's courses, by id; null when the year has none published. */
        suspend fun yearOrNull(): JsonObject? {
            val url = "${BASE_URL}opinioniAA.vm?idAA=$year"
            context.log("AA $year: fetching the departments")
            // A year not published yet lists no departments, which looks like a page served blank: both end the same way.
            val page = tryGet(url, "its departments", log = false) { rows(it.document).isNotEmpty() }
            if (page == null) {
                context.log("AA $year: no departments listed")
                return null
            }
            val departments = rows(page.document)
            val courses = coroutineScope { departments.map { async { department(it) } }.awaitAll() }.flatten()
            // A course shared by study courses is listed under each: the first, by address, stands for all.
            val byId = sortedMapOf<String, JsonObject>()
            for (course in courses.sortedBy { it.getValue("url").jsonPrimitive.content }) {
                byId.putIfAbsent(course.getValue("id").jsonPrimitive.content, course)
            }
            context.log("AA $year: ${byId.size} courses, ${courses.size - byId.size} of them listed more than once (${fetcher.stats()})")
            return buildJsonObject {
                put("id", year)
                put("courses", JsonObject(byId))
            }
        }

        private suspend fun department(url: String): List<JsonObject> {
            val page = tryGet(url, "its study courses") { rows(it.document).isNotEmpty() } ?: return emptyList()
            return coroutineScope { rows(page.document).map { async { studyCourse(it) } }.awaitAll() }.flatten()
        }

        private suspend fun studyCourse(url: String): List<JsonObject> {
            val page = tryGet(url, "its courses") { rows(it.document).isNotEmpty() } ?: return emptyList()
            return coroutineScope { rows(page.document).map { async { course(it) } }.awaitAll() }.filterNotNull()
        }

        private suspend fun course(url: String): JsonObject? {
            val id = queryParameter(url, "idAD") ?: return null
            val page = tryGet(url, "its header or its teachers") {
                header(it.document, "Numero di questionari") != null && rows(it.document).isNotEmpty()
            } ?: return null
            val questionnaires = header(page.document, "Numero di questionari")?.let(::parseCount)
            // Each row: course code, course name, teacher, published (SI/NO), reason, questionnaires.
            val stubs = page.document.select("table tbody tr").mapNotNull { row ->
                val href = row.attr("data-href").ifEmpty { return@mapNotNull null }
                val teacher = queryParameter(href, "idDOC") ?: return@mapNotNull null
                val cells = row.select("td").map { strippedText(it) }
                Stub(teacher, row.absUrl("data-href"), cells.getOrNull(5)?.let(::parseCount), cells.getOrNull(3)?.uppercase() == "SI")
            }
            val teachers = coroutineScope { stubs.map { async { teacher(it) } }.awaitAll() }
            fetched.incrementAndGet()
            return buildJsonObject {
                put("id", id)
                put("url", url)
                put("questionnaires", questionnaires.toJson())
                put("ratings", weighted(teachers))
                put("teachers", JsonArray(teachers))
            }
        }

        private suspend fun teacher(stub: Stub): JsonObject {
            fun unpublished() = buildJsonObject {
                put("id", stub.id)
                put("url", stub.url)
                put("questionnaires", stub.questionnaires.toJson())
                put("ratings", JsonNull)
            }
            if (!stub.published) return unpublished()
            // The chart's series: university, department, course type, study course, then the
            // teacher's, any of which a page can come with blank. Some teachers marked as
            // published never have theirs, which retries can't tell from a page served blank:
            // they count as unpublished.
            val page = tryGet(stub.url, "the teacher's ratings") { page ->
                chartSeries(page.html).let { it.size == SERIES && it.last().isNotEmpty() }
            } ?: return unpublished()
            val series = chartSeries(page.html)
            return buildJsonObject {
                put("id", stub.id)
                put("url", stub.url)
                put("questionnaires", (header(page.document, "Numero di questionari")?.let(::parseCount) ?: stub.questionnaires).toJson())
                put("ratings", ratings(series.last(), "DOC ${stub.id} / AA $year"))
            }
        }

        private suspend fun tryGet(url: String, lacking: String, log: Boolean = true, complete: (Page) -> Boolean): Page? = try {
            fetcher.get(url, lacking, complete)
        } catch (e: FetchException) {
            if (log) context.log("  ! ${e.message}")
            null
        }

        /** The ratings of a chart's series, rescaled to 1-10; it must hold the three known ones exactly. */
        private fun ratings(pairs: List<Pair<String, Double>>, where: String): JsonObject {
            require(pairs.isNotEmpty()) { "No ratings in the chart of $where" }
            val seen = linkedMapOf<String, Double>()
            for ((label, value) in pairs) {
                val key = KPIS[label] ?: throw IllegalArgumentException("Unknown rating '$label' at $where; known: ${KPIS.keys}")
                require(seen.put(key, if (year <= LAST_LEGACY_YEAR) 1.0 + 3.0 * value else value) == null) { "Rating '$label' twice at $where" }
            }
            val missing = KPIS.values - seen.keys
            require(missing.isEmpty()) { "Ratings $missing missing at $where" }
            return JsonObject(seen.mapValues { JsonPrimitive(it.value) })
        }

        /** The teachers' ratings, weighted by their questionnaires; none when no teacher with questionnaires has any. */
        private fun weighted(teachers: List<JsonObject>): JsonElement {
            val totals = KPIS.values.associateWith { 0.0 }.toMutableMap()
            var weights = 0
            for (teacher in teachers) {
                val ratings = teacher["ratings"] as? JsonObject ?: continue
                val weight = teacher["questionnaires"]?.jsonPrimitive?.intOrNull ?: 0
                if (weight <= 0) continue
                weights += weight
                for (key in KPIS.values) totals[key] = totals.getValue(key) + ratings.getValue(key).jsonPrimitive.doubleOrNull!! * weight
            }
            if (weights == 0) return JsonNull
            return JsonObject(totals.mapValues { (_, total) -> JsonPrimitive(BigDecimal(total / weights).setScale(6, RoundingMode.HALF_EVEN).toDouble()) })
        }
    }

    private class Stub(val id: String, val url: String, val questionnaires: Int?, val published: Boolean)

    internal companion object {
        const val BASE_URL = "https://opinionistudenti.unimib.it/valdid/"
        /**
         * The requests in flight. Once its database is busy the site answers late, or seems not
         * to answer at all, and it goes on working on a request its client gave up on: a few
         * requests, each waited for, keep it answering everyone.
         */
        const val CONCURRENCY = 3

        /** Long enough for a busy site to answer, since a request given up on still loads it. */
        const val TIMEOUT_SECONDS = 90L

        /** A page served with a section blank is asked for again, until it comes with them all. */
        const val ATTEMPTS = 15

        /** A burst of requests slows the site down for a while, so they start at this pace at most. */
        const val REQUESTS_PER_SECOND = 3.0
        const val FIRST_YEAR = 2013

        /** The series of a teacher's chart. */
        const val SERIES = 5

        /** How often the crawl says how far it got, in milliseconds. */
        const val PROGRESS_INTERVAL = 60_000L

        /** Academic years up to this one rate on a 0-3 scale; later ones on 1-10. */
        const val LAST_LEGACY_YEAR = 2018

        /** The chart's Italian labels, and the keys of their ratings. */
        val KPIS = linkedMapOf(
            "Soddisfazione Complessiva" to "overall_satisfaction",
            "Efficacia Didattica" to "teaching_effectiveness",
            "Aspetti organizzativi" to "organizational_aspects",
        )

        /** A browser's headers: the site sits behind a gateway that tells browsers from other clients. */
        val HEADERS = mapOf(
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36",
            "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
            "Accept-Language" to "it-IT,it;q=0.9,en-US;q=0.8,en;q=0.7",
            "Upgrade-Insecure-Requests" to "1",
            "Sec-Ch-Ua" to "\"Chromium\";v=\"148\", \"Google Chrome\";v=\"148\", \"Not/A)Brand\";v=\"99\"",
            "Sec-Ch-Ua-Mobile" to "?0",
            "Sec-Ch-Ua-Platform" to "\"Windows\"",
            "Sec-Fetch-Dest" to "document",
            "Sec-Fetch-Mode" to "navigate",
            "Sec-Fetch-Site" to "none",
            "Sec-Fetch-User" to "?1",
        )

        val PAIR = Regex("""\[\s*['"]([^'"]+)['"]\s*,\s*([0-9.]+)\s*]|\[\s*([0-9.]+)\s*,\s*['"]([^'"]+)['"]\s*]""")

        /** The links of a listing's rows, resolved. */
        fun rows(document: Document): List<String> =
            document.select("table tbody tr").filter { it.hasAttr("data-href") }.map { it.absUrl("data-href") }.filter { it.isNotEmpty() }

        /** The value after `Label :` in the page's header, where each field is a bold label followed by its value. */
        fun header(document: Document, label: String): String? {
            val strong = document.select(".jumbotron strong").firstOrNull { strippedText(it) == label } ?: return null
            val value = (strong.nextSibling() as? TextNode)?.wholeText ?: return null
            return value.trim().removePrefix(":").trim().ifEmpty { null }
        }

        /** A count, ignoring thousands separators. */
        fun parseCount(text: String): Int? = text.replace(",", "").replace(".", "").trim().toIntOrNull()

        /**
         * The series of the page's first `$.jqplot('chart', [[…], […]], …)` call, as (label, value)
         * pairs; the page repeats the same data in other calls.
         */
        fun chartSeries(html: String): List<List<Pair<String, Double>>> {
            val call = html.indexOf("\$.jqplot('chart'").takeIf { it >= 0 } ?: return emptyList()
            val start = html.indexOf('[', call).takeIf { it >= 0 } ?: return emptyList()
            var depth = 0
            var end = -1
            for (i in start until html.length) {
                when (html[i]) {
                    '[' -> depth++
                    ']' -> if (--depth == 0) {
                        end = i
                        break
                    }
                }
            }
            if (end < 0) return emptyList()
            val blocks = mutableListOf<String>()
            val block = StringBuilder()
            depth = 0
            for (c in html.substring(start + 1, end)) {
                when {
                    c == '[' -> {
                        depth++
                        block.append(c)
                    }
                    c == ']' -> {
                        depth--
                        block.append(c)
                        if (depth == 0) {
                            blocks += block.toString()
                            block.clear()
                        }
                    }
                    depth > 0 -> block.append(c)
                }
            }
            return blocks.map { text ->
                PAIR.findAll(text).map { match ->
                    val (label, value, swappedValue, swappedLabel) = match.destructured
                    if (label.isNotEmpty()) label to value.toDouble() else swappedLabel to swappedValue.toDouble()
                }.toList()
            }
        }

        fun Int?.toJson(): JsonElement = this?.let(::JsonPrimitive) ?: JsonNull
    }
}
