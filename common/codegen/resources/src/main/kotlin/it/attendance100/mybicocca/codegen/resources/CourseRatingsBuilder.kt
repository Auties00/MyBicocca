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
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.jsoup.nodes.Document
import org.jsoup.nodes.TextNode
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

/**
 * The students' opinions of the courses, from https://opinionistudenti.unimib.it/valdid/, by
 * academic year and then course: each teacher's three ratings, and the course's, weighted by
 * the teachers' questionnaires. Departments and study courses only lead to the courses.
 *
 * Ratings are on the 1-10 scale used from the academic year 2019/2020; earlier ones, on a 0-3
 * scale, become `1 + 3 × rating`, which maps the bands' boundaries exactly. A teacher whose
 * ratings aren't published, or are published empty, has none, and a course none when none of
 * its teachers has.
 *
 * The site now and then serves a page with some of its sections blank, or some of a listing's
 * rows missing; one of its servers can keep doing so for a while, while the others serve it
 * whole. Every page says how many questionnaires it covers, and every listing's row how many
 * its page does, so a page counts as fetched only once it agrees with the row that led to it
 * and its own rows add up to it; a teacher's page, once it has every series of its chart. A
 * page is tried again on the other servers, and one that never comes whole is kept as last
 * served and listed in its year's gaps, which later crawls fetch again: the data says what it
 * may miss rather than miss it unseen.
 *
 * Past academic years don't change, so only the site's last two are fetched again, along with
 * any the resource lacks and the gaps of the others. Years are fetched one at a time, the
 * latest first, and each is saved once fetched, so that a crawl stopped halfway resumes from
 * the years it finished.
 */
class CourseRatingsBuilder : ResourceBuilder {
    override val id: String = "course-ratings"

    override fun build(context: ResourceContext): JsonObject = runBlocking {
        // A session that has served one page gets the next stripped of its data, so every request has its own.
        val backends = Backends(AFFINITY_COOKIE, PER_BACKEND, SLOW_SECONDS, REST_SECONDS)
        val fetcher = Fetcher(
            CONCURRENCY, HEADERS, freshSessions = true,
            attempts = ATTEMPTS, backoffSeconds = 1.0, maxPauseSeconds = 60.0, timeoutSeconds = TIMEOUT_SECONDS, backends = backends,
        )
        val previous = context.previous?.get("academic_years")?.jsonObject.orEmpty()
            .mapKeys { it.key.toInt() }.mapValues { it.value.jsonObject }
        // Years don't leave the site, so a list without one the resource has is a list served short.
        val years = fetcher.get(BASE_URL, "the academic years, ${previous.keys.sorted()} among them") { page ->
            years(page.document).let { it.isNotEmpty() && it.containsAll(previous.keys) }
        }.let { years(it.document) }.sortedDescending()
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
                context.log("  ${fetched.get()} courses fetched, ${fetcher.stats()}; ${backends.describe()}")
            }
        }
        for (year in years) {
            val saved = previous[year]
            val crawl = Crawl(fetcher, year, context, fetched)
            val data = when {
                saved == null || year in years.take(2) -> crawl.year()
                gaps(saved).isNotEmpty() -> crawl.fill(saved)
                else -> continue
            }
            done[year] = data
            context.checkpoint(data())
        }
        progress.cancel()
        context.log("Requests: ${fetcher.stats()}")
        for ((year, data) in done) {
            gaps(data).takeIf { it.isNotEmpty() }?.let { context.log("AA $year: ${it.size} pages kept with parts missing, for a later run to fetch again") }
        }
        data()
    }

    private class Crawl(val fetcher: Fetcher, val year: Int, val context: ResourceContext, val fetched: AtomicInteger) {
        private val gaps = ConcurrentLinkedQueue<Gap>()

        /** The year's courses, by id. */
        suspend fun year(): JsonObject {
            context.log("AA $year: fetching the departments")
            val departments = listing(Row("${BASE_URL}opinioniAA.vm?idAA=$year", null, emptyList()), Gap.YEAR)
            val courses = coroutineScope { departments.map { async { department(it) } }.awaitAll() }.flatten()
            return data(emptyMap(), courses)
        }

        /** The year [saved] with its gaps fetched again: the courses under each replace those it had. */
        suspend fun fill(saved: JsonObject): JsonObject {
            val gaps = gaps(saved)
            if (gaps.any { it.level == Gap.YEAR }) return year()
            context.log("AA $year: fetching again the ${gaps.size} pages kept with parts missing")
            val courses = coroutineScope {
                gaps.map { gap ->
                    async {
                        val row = Row(gap.url, gap.questionnaires, emptyList())
                        when (gap.level) {
                            Gap.DEPARTMENT -> department(row)
                            Gap.STUDY_COURSE -> studyCourse(row)
                            else -> listOfNotNull(course(row))
                        }
                    }
                }.awaitAll()
            }.flatten()
            return data(saved.getValue("courses").jsonObject.mapValues { it.value.jsonObject }, courses)
        }

        /** The year's data: the [kept] courses, by id, with [courses] added, and the gaps this crawl left. */
        private fun data(kept: Map<String, JsonObject>, courses: List<JsonObject>): JsonObject {
            // A course shared by study courses is listed under each: the first, by address, stands for all.
            val byId = kept.toSortedMap()
            for (course in courses) {
                val id = course.getValue("id").jsonPrimitive.content
                val other = byId[id]?.getValue("url")?.jsonPrimitive?.content
                if (other == null || course.getValue("url").jsonPrimitive.content <= other) byId[id] = course
            }
            val left = gaps.distinct().sortedBy { it.url }
            context.log("AA $year: ${byId.size} courses, ${left.size} pages kept with parts missing (${fetcher.stats()})")
            return buildJsonObject {
                put("id", year)
                put("courses", JsonObject(byId))
                if (left.isNotEmpty()) put("gaps", JsonArray(left.map(Gap::toJson)))
            }
        }

        private suspend fun department(row: Row): List<JsonObject> =
            coroutineScope { listing(row, Gap.DEPARTMENT).map { async { studyCourse(it) } }.awaitAll() }.flatten()

        private suspend fun studyCourse(row: Row): List<JsonObject> =
            coroutineScope { listing(row, Gap.STUDY_COURSE).map { async { course(it) } }.awaitAll() }.filterNotNull()

        private suspend fun course(row: Row): JsonObject? {
            val id = queryParameter(row.url, "idAD") ?: return null
            // Each row: course code, course name, teacher, published (SI/NO), reason, questionnaires.
            val stubs = listing(row, Gap.COURSE).mapNotNull { teacher ->
                val teacherId = queryParameter(teacher.url, "idDOC") ?: return@mapNotNull null
                Stub(teacherId, teacher.url, teacher.questionnaires, teacher.cells.getOrNull(3)?.uppercase() == "SI")
            }
            val teachers = coroutineScope { stubs.map { async { teacher(it, row) } }.awaitAll() }
            fetched.incrementAndGet()
            return buildJsonObject {
                put("id", id)
                put("url", row.url)
                put("questionnaires", row.questionnaires.toJson())
                put("ratings", weighted(teachers))
                put("teachers", JsonArray(teachers))
            }
        }

        /** The teacher of [stub], of the course [course] leads to: its ratings, when published. */
        private suspend fun teacher(stub: Stub, course: Row): JsonObject {
            val ratings = if (stub.published && (stub.questionnaires ?: 0) > 0) {
                try {
                    val page = fetcher.get(stub.url, "the ${stub.questionnaires} questionnaires of its course's row, and every series of its chart") { page ->
                        header(page.document) == stub.questionnaires && chartSeries(page.html).let { it.size == SERIES && it.all(List<*>::isNotEmpty) }
                    }
                    // The chart's series: university, department, course type, study course, then the teacher's.
                    ratings(chartSeries(page.html).last(), "DOC ${stub.id} / AA $year")
                } catch (e: FetchException) {
                    // Its course is fetched again, teachers and all.
                    keep(Gap(Gap.COURSE, course.url, course.questionnaires), e)
                    JsonNull
                }
            } else {
                JsonNull
            }
            return buildJsonObject {
                put("id", stub.id)
                put("url", stub.url)
                put("questionnaires", stub.questionnaires.toJson())
                put("ratings", ratings)
            }
        }

        /**
         * The rows of the listing [row] leads to, once they add up to its questionnaires; when
         * they never do, those it was last served with, and the listing goes in the year's gaps
         * at [level].
         */
        private suspend fun listing(row: Row, level: String): List<Row> = try {
            rows(fetcher.get(row.url, "rows adding up to ${row.questionnaires ?: "its"} questionnaires") { isWhole(it.document, row.questionnaires) }.document)
        } catch (e: FetchException) {
            keep(Gap(level, row.url, row.questionnaires), e)
            e.last?.let { rows(it.document) }.orEmpty()
        }

        private fun keep(gap: Gap, e: FetchException) {
            gaps += gap
            context.log("AA $year: kept with parts missing, for a later run to fetch again: ${e.message}")
        }

        /**
         * The ratings of a chart's series, rescaled to 1-10; it must hold the three known ones
         * exactly. None when the site publishes them empty.
         */
        private fun ratings(pairs: List<Pair<String, Double?>>, where: String): JsonElement {
            require(pairs.isNotEmpty()) { "No ratings in the chart of $where" }
            if (pairs.all { it.second == null }) return JsonNull
            val seen = linkedMapOf<String, Double>()
            for ((label, value) in pairs) {
                val key = KPIS[label] ?: throw IllegalArgumentException("Unknown rating '$label' at $where; known: ${KPIS.keys}")
                requireNotNull(value) { "Rating '$label' empty among others at $where" }
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

    /** A listing's row: the page it leads to, how many questionnaires that page covers, and its cells' text. */
    internal class Row(val url: String, val questionnaires: Int?, val cells: List<String>)

    /** A page that never came whole: the listing at [url], of a [level], meant to cover [questionnaires]. */
    internal data class Gap(val level: String, val url: String, val questionnaires: Int?) {
        fun toJson(): JsonObject = buildJsonObject {
            put("level", level)
            put("url", url)
            put("questionnaires", questionnaires.toJson())
        }

        companion object {
            const val YEAR = "year"
            const val DEPARTMENT = "department"
            const val STUDY_COURSE = "study_course"
            const val COURSE = "course"
        }
    }

    internal companion object {
        const val BASE_URL = "https://opinionistudenti.unimib.it/valdid/"

        /**
         * The site's servers sit behind an Azure Application Gateway, which keeps a client on one
         * of them by this cookie. Each server slows down on its own once asked too much, until
         * its data pages take minutes while the others still answer at once; a request given up
         * on goes on loading it. So requests go to each server as fast as it answers.
         */
        const val AFFINITY_COOKIE = "ApplicationGatewayAffinity"

        /**
         * The requests in flight to a server. Measured on 2026-10-26: 4 in all, spread by the
         * gateway, choked one server within 4 minutes, and 2 to each choked them all within 2,
         * on pages fetched the first time in a while; 1 at a time kept up for minutes.
         */
        const val PER_BACKEND = 1

        /**
         * A data page usually takes a fraction of a second, and a big listing a few; a choked
         * server takes 20 to 60. The first crawl rested a server past 3 seconds, and they spent
         * most of it resting.
         */
        const val SLOW_SECONDS = 10.0

        /** How long a busy server is left alone: they got over it in 3 to 5 minutes left alone entirely, and this one's share now goes to the others. */
        const val REST_SECONDS = 120.0

        /** The requests in flight in all: the site has had three servers. */
        const val CONCURRENCY = 3 * PER_BACKEND

        /**
         * Long enough for the heaviest pages, an old year's, which can take tens of seconds the
         * first time: a request given up on still loads the site.
         */
        const val TIMEOUT_SECONDS = 120L

        /**
         * A page served with parts missing usually comes whole on the next attempt, or on
         * another server; these try each of three servers three times, over a few minutes.
         */
        const val ATTEMPTS = 9

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

        val PAIR = Regex("""\[\s*['"]([^'"]+)['"]\s*,\s*([0-9.]+|null)\s*]|\[\s*([0-9.]+|null)\s*,\s*['"]([^'"]+)['"]\s*]""")

        /** The script that writes a row's numbers into its cells: the first is its questionnaires. */
        val FORMATTED = Regex("""formatta\(\s*([0-9.]+)\s*\)""")
        val YEAR_LINK = Regex("""opinioniAA\.vm\?idAA=(\d{4})""")

        /** The academic years the site links to. */
        fun years(document: Document): Set<Int> =
            document.select("a[href]").mapNotNull { YEAR_LINK.find(it.attr("href"))?.groupValues?.get(1)?.toInt() }.toSet()

        /** The gaps of a year's data. */
        fun gaps(year: JsonObject): List<Gap> = year["gaps"]?.jsonArray.orEmpty().map { gap ->
            val fields = gap.jsonObject
            Gap(fields.getValue("level").jsonPrimitive.content, fields.getValue("url").jsonPrimitive.content, fields["questionnaires"]?.jsonPrimitive?.intOrNull)
        }

        /** A listing's rows, each once: a page can list the same row twice. */
        fun rows(document: Document): List<Row> =
            document.select("table tbody tr").filter { it.hasAttr("data-href") }.mapNotNull { row ->
                val url = row.absUrl("data-href").ifEmpty { return@mapNotNull null }
                val questionnaires = FORMATTED.find(row.html())?.groupValues?.get(1)?.toIntOrNull()
                Row(url, questionnaires, row.select("td").map { strippedText(it) })
            }.distinctBy { it.url }

        /**
         * Whether a listing came whole: its rows add up to the questionnaires it covers, the
         * [expected] of the row that led to it, or else those its header says. A header that
         * says otherwise is a page served wrong; none at all is how a course page with no
         * teacher's ratings published comes.
         */
        fun isWhole(document: Document, expected: Int?): Boolean {
            val total = header(document)
            if (expected != null && total != null && total != expected) return false
            return rows(document).sumOf { it.questionnaires ?: 0 } == (expected ?: total ?: return false)
        }

        /** The questionnaires a page says it covers, in its header. */
        fun header(document: Document): Int? = header(document, "Numero di questionari")?.let(::parseCount)

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
         * pairs, with no value where the site writes `null`; the page repeats the same data in
         * other calls.
         */
        fun chartSeries(html: String): List<List<Pair<String, Double?>>> {
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
                    if (label.isNotEmpty()) label to value.toDoubleOrNull() else swappedLabel to swappedValue.toDoubleOrNull()
                }.toList()
            }
        }

        fun Int?.toJson(): JsonElement = this?.let(::JsonPrimitive) ?: JsonNull
    }
}
