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
 * Every page says how many questionnaires it covers, and every listing's row how many its page
 * does, so a page counts as fetched only once it agrees with the row that led to it and its own
 * rows add up to it; a teacher's page, once it has every series of its chart. The site gets it
 * wrong in three ways:
 * - it now and then serves a page with some of its sections blank, and one of its servers can
 *   keep doing so for a while while the others serve it whole: a page is tried again, on the
 *   other servers;
 * - it leaves some study courses out of their department's listing, and some courses out of
 *   their study course's, for good, while their pages are there: once every year is fetched,
 *   the pages a listing lacks are looked for among the codes of every year, and taken when they
 *   make up what it lacks exactly ([RatingsDirectory]);
 * - it counts some questionnaires of a course for none of the teachers it lists: these are kept
 *   as the course's `unattributed_questionnaires`.
 * A page none of these explains is kept as last served and listed in its year's gaps, which
 * later crawls fetch again: the data says what it may miss rather than miss it unseen.
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
            if (year in previous && year !in years.take(2)) continue
            done[year] = Crawl(fetcher, year, context, fetched).year()
            context.checkpoint(data())
        }
        // The pages a listing leaves out can have their codes in any year, so they are looked for once every year is in.
        val directory = RatingsDirectory(done)
        for (year in years) {
            val saved = done[year]?.takeIf { gaps(it).isNotEmpty() } ?: continue
            done[year] = Crawl(fetcher, year, context, fetched).repair(saved, directory)
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
            val row = Row("${BASE_URL}opinioniAA.vm?idAA=$year", null, emptyList())
            return data(emptyMap(), children(Gap.YEAR, row, listing(row, Gap.YEAR)))
        }

        /**
         * The year [saved] with its gaps fetched again, and the pages their listings leave out
         * looked for in [directory]: the courses under each replace those it had.
         */
        suspend fun repair(saved: JsonObject, directory: RatingsDirectory): JsonObject {
            val gaps = gaps(saved)
            context.log("AA $year: fetching again the ${gaps.size} pages kept with parts missing")
            val kept = saved.getValue("courses").jsonObject.mapValues { it.value.jsonObject }
            val urls = kept.values.mapNotNull { it["url"]?.jsonPrimitive?.content }
            // What the year's first crawl reached, which stays as it is: a listing's rows were followed even when it never came whole.
            val reached = mapOf(
                Gap.YEAR to urls.mapNotNull { queryParameter(it, "idDIP") }.toSet(),
                Gap.DEPARTMENT to urls.mapNotNull { queryParameter(it, "idCDS") }.toSet(),
                Gap.STUDY_COURSE to kept.keys,
            )
            val courses = coroutineScope { gaps.map { async { recover(it, directory, reached) } }.awaitAll() }.flatten()
            return data(kept, courses)
        }

        /**
         * The courses under [gap] that the year's first crawl didn't reach, by [reached] code of
         * each level, with the pages its listing leaves out when they make up what it lacks; a
         * course's, with all of its teachers.
         */
        private suspend fun recover(gap: Gap, directory: RatingsDirectory, reached: Map<String, Set<String>>): List<JsonObject> {
            val row = Row(gap.url, gap.questionnaires, emptyList())
            val listing = served(row)
            var found = emptyList<Row>()
            var unattributed: Int? = null
            if (!listing.whole) {
                val expected = gap.questionnaires ?: listing.page?.let { header(it.document) }
                val missing = expected?.minus(listing.rows.sumOf { it.questionnaires ?: 0 })?.takeIf { it > 0 }
                val orphans = missing?.let { orphans(gap, listing.rows, it, directory) }
                when {
                    orphans != null -> {
                        context.log("AA $year: found the $missing questionnaires ${gap.url} lacks in ${orphans.size} pages it doesn't list")
                        found = orphans
                    }
                    // A course page that says how many questionnaires it covers, and lists teachers covering fewer, is the site's own count.
                    missing != null && gap.level == Gap.COURSE && listing.page?.let { header(it.document) } == expected -> {
                        context.log("AA $year: the site attributes $missing of the $expected questionnaires of ${gap.url} to none of its teachers")
                        unattributed = missing
                    }
                    missing != null -> keep(gap, "no page it doesn't list makes up the $missing questionnaires it lacks")
                    else -> keep(gap, listing.problem)
                }
            }
            val rows = listing.rows + found
            if (gap.level == Gap.COURSE) return listOfNotNull(course(row, rows, unattributed))
            val param = CHILD_PARAMETERS.getValue(gap.level)
            return children(gap.level, row, rows.filterNot { queryParameter(it.url, param) in reached.getValue(gap.level) })
        }

        /**
         * The pages the listing of [gap] leaves out that make up the [missing] questionnaires:
         * first among the codes [directory] has for it, then among the gaps in their numbering.
         * Null when none, or more than one choice of them, does.
         */
        private suspend fun orphans(gap: Gap, listed: List<Row>, missing: Int, directory: RatingsDirectory): List<Row>? {
            val probed = mutableListOf<Row>()
            val tried = mutableSetOf<String>()
            for (candidates in candidates(gap, listed, directory)) {
                val urls = candidates.filter { tried.add(it) }.take(MAX_PROBES - probed.size).takeIf { it.isNotEmpty() } ?: continue
                probed += coroutineScope { urls.map { async { probe(it, gap.level) } }.awaitAll() }.filterNotNull()
                RatingsDirectory.closing(probed.map { it.questionnaires ?: 0 }, missing)?.let { chosen -> return chosen.map(probed::get) }
            }
            return null
        }

        /** The pages [gap] may lead to that its listing leaves out: the codes of other years, then the gaps in their numbering. */
        private fun candidates(gap: Gap, listed: List<Row>, directory: RatingsDirectory): List<List<String>> {
            fun parameter(name: String) = queryParameter(gap.url, name).orEmpty()
            val department = parameter("idDIP")
            val studyCourse = parameter("idCDS")
            val type = parameter("idTC")
            val course = parameter("idAD")
            return when (gap.level) {
                Gap.YEAR -> {
                    val have = listed.mapNotNull { queryParameter(it.url, "idDIP") }.toSet()
                    listOf((directory.departments - have).map { "${BASE_URL}opinioniDIP.vm?idDIP=$it&idAA=$year" })
                }
                Gap.DEPARTMENT -> {
                    val have = listed.mapNotNull { row -> queryParameter(row.url, "idCDS")?.let { it to queryParameter(row.url, "idTC").orEmpty() } }.toMap()
                    // A study course the year lists under another department isn't this one's: its page loads under any.
                    val elsewhere = directory.listedStudyCourses[year].orEmpty()
                    val known = directory.studyCourses[department].orEmpty()
                    fun urls(codes: Map<String, String>) = codes.filterKeys { it !in have && it !in elsewhere }
                        .map { (code, codeType) -> "${BASE_URL}opinioniCDS.vm?idCDS=$code&idAA=$year&idDIP=$department&idTC=$codeType" }
                    listOf(urls(known), urls(RatingsDirectory.studyCourseHoles(known + have)))
                }
                Gap.STUDY_COURSE -> {
                    val have = listed.mapNotNull { queryParameter(it.url, "idAD") }.toSet()
                    val known = directory.courses[studyCourse].orEmpty()
                    fun urls(codes: Set<String>) = (codes - have).map { "${BASE_URL}opinioniAD.vm?idAD=$it&idAA=$year&idDIP=$department&idCDS=$studyCourse&idTC=$type" }
                    listOf(urls(known), urls(RatingsDirectory.courseHoles(known + have)))
                }
                else -> {
                    val have = listed.mapNotNull { queryParameter(it.url, "idDOC") }.toSet()
                    val known = directory.teachers[course].orEmpty() + directory.studyCourseTeachers[studyCourse to year].orEmpty()
                    listOf((known - have).map { "${BASE_URL}opinioniDOC.vm?idDOC=$it&idDIP=$department&idCDS=$studyCourse&idTC=$type&idAD=$course&idAA=$year" })
                }
            }
        }

        /**
         * The page at [url], one that a listing at [level] leaves out, as that listing's row would
         * have it: null when it has nothing for the year.
         */
        private suspend fun probe(url: String, level: String): Row? {
            // A page with nothing may be one served blank: it gets another attempt, on another server.
            val page = try {
                fetcher.get(url, "questionnaires for $year", attempts = PROBE_ATTEMPTS) { questionnaires(it, level) > 0 }
            } catch (e: FetchException) {
                return null
            }
            // A teacher's page has a chart only when its ratings are published, though the chart may come blank.
            return Row(url, questionnaires(page, level), emptyList(), published = page.html.contains(CHART))
        }

        /** The questionnaires of a page a listing at [level] leads to: a teacher's, in its header; others, there or in their rows. */
        private fun questionnaires(page: Page, level: String): Int = when (level) {
            Gap.COURSE -> header(page.document) ?: 0
            // A page the listing leaves out says nothing in its header: the link that's missing is what fills it.
            else -> header(page.document) ?: rows(page.document).sumOf { it.questionnaires ?: 0 }
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

        /** The courses under the page [row] leads to, at [level], whose listing has [rows]. */
        private suspend fun children(level: String, row: Row, rows: List<Row>): List<JsonObject> = coroutineScope {
            when (level) {
                Gap.YEAR -> rows.map { async { department(it) } }.awaitAll().flatten()
                Gap.DEPARTMENT -> rows.map { async { studyCourse(it) } }.awaitAll().flatten()
                Gap.STUDY_COURSE -> rows.map { async { course(it) } }.awaitAll().filterNotNull()
                else -> listOfNotNull(course(row, rows))
            }
        }

        private suspend fun department(row: Row): List<JsonObject> = children(Gap.DEPARTMENT, row, listing(row, Gap.DEPARTMENT))

        private suspend fun studyCourse(row: Row): List<JsonObject> = children(Gap.STUDY_COURSE, row, listing(row, Gap.STUDY_COURSE))

        private suspend fun course(row: Row): JsonObject? {
            if (queryParameter(row.url, "idAD") == null) return null
            return course(row, listing(row, Gap.COURSE))
        }

        /**
         * The course [row] leads to, whose listing has the [teachers]' rows, and the questionnaires
         * the site attributes to none of them, when [unattributed].
         */
        private suspend fun course(row: Row, teachers: List<Row>, unattributed: Int? = null): JsonObject? {
            val id = queryParameter(row.url, "idAD") ?: return null
            val stubs = teachers.mapNotNull { teacher ->
                val teacherId = queryParameter(teacher.url, "idDOC") ?: return@mapNotNull null
                Stub(teacherId, teacher.url, teacher.questionnaires, teacher.published)
            }
            val fetchedTeachers = coroutineScope { stubs.map { async { teacher(it, row) } }.awaitAll() }
            fetched.incrementAndGet()
            return buildJsonObject {
                put("id", id)
                put("url", row.url)
                put("questionnaires", row.questionnaires.toJson())
                put("ratings", weighted(fetchedTeachers))
                put("teachers", JsonArray(fetchedTeachers))
                unattributed?.let { put("unattributed_questionnaires", it) }
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
                    keep(Gap(Gap.COURSE, course.url, course.questionnaires), e.message)
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

        /** A listing as served: whole, or else the fullest it came, and what it lacked. */
        private class Listing(val page: Page?, val whole: Boolean, val problem: String? = null) {
            val rows = page?.let { rows(it.document) }.orEmpty()
        }

        /** The listing [row] leads to, once its rows add up to the row's questionnaires. */
        private suspend fun served(row: Row): Listing = try {
            Listing(fetcher.get(row.url, "rows adding up to ${row.questionnaires ?: "its"} questionnaires") { isWhole(it.document, row.questionnaires) }, whole = true)
        } catch (e: FetchException) {
            // Blanks only take parts away, so the fullest is the closest to the page.
            Listing(e.served.maxByOrNull { page -> rows(page.document).sumOf { it.questionnaires ?: 0 } }, whole = false, e.message)
        }

        /**
         * The rows of the listing [row] leads to, once they add up to its questionnaires; when
         * they never do, the fullest it was served with, and the listing goes in the year's gaps
         * at [level].
         */
        private suspend fun listing(row: Row, level: String): List<Row> {
            val listing = served(row)
            if (!listing.whole) keep(Gap(level, row.url, row.questionnaires), listing.problem)
            return listing.rows
        }

        private fun keep(gap: Gap, problem: String?) {
            gaps += gap
            context.log("AA $year: kept with parts missing, for a later run to fetch again: ${gap.url}${problem?.let { "\n    $it" }.orEmpty()}")
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

    /**
     * A listing's row: the page it leads to, how many questionnaires that page covers, and its
     * cells' text; for a teacher's, whether its ratings are published.
     */
    internal class Row(
        val url: String,
        val questionnaires: Int?,
        val cells: List<String>,
        // Each teacher's row: course code, course name, teacher, published (SI/NO), reason, questionnaires.
        val published: Boolean = cells.getOrNull(3)?.uppercase() == "SI",
    )

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

        /** The attempts at a page a listing may leave out, most of which have nothing for the year. */
        const val PROBE_ATTEMPTS = 2

        /** The pages looked for at most for one listing: the codes of a study course's courses, and the gaps in their numbering, are a few dozen. */
        const val MAX_PROBES = 150

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

        /** The query parameter of the pages a listing at each level leads to: a year's to departments, and so on. */
        val CHILD_PARAMETERS = mapOf(Gap.YEAR to "idDIP", Gap.DEPARTMENT to "idCDS", Gap.STUDY_COURSE to "idAD", Gap.COURSE to "idDOC")

        /** The call that draws a page's chart. */
        const val CHART = "\$.jqplot('chart'"

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
         * teacher's ratings published comes, and a page its parent's listing leaves out.
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
            val call = html.indexOf(CHART).takeIf { it >= 0 } ?: return emptyList()
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
