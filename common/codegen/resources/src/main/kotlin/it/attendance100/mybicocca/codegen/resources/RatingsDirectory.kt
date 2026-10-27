package it.attendance100.mybicocca.codegen.resources

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.Locale

/**
 * The codes of the ratings site's pages, from the data of every academic year: where to look
 * for the pages a listing leaves out. The site sometimes leaves a study course out of its
 * department's listing, or a course out of its study course's, for good, while its pages are
 * there; the codes it has in other years, and the gaps in their numbering, find them again.
 */
internal class RatingsDirectory(years: Map<Int, JsonObject>) {
    /** The departments of any year. */
    val departments = sortedSetOf<String>()

    /** Each department's study courses in any year, by code, with their type (`idTC`). */
    val studyCourses = mutableMapOf<String, MutableMap<String, String>>()

    /** The study courses each year lists, under any department. */
    val listedStudyCourses = mutableMapOf<Int, MutableSet<String>>()

    /** Each study course's courses in any year. */
    val courses = mutableMapOf<String, MutableSet<String>>()

    /** Each course's teachers in any year. */
    val teachers = mutableMapOf<String, MutableSet<String>>()

    /** The teachers of each study course's courses, by year. */
    val studyCourseTeachers = mutableMapOf<Pair<String, Int>, MutableSet<String>>()

    init {
        for ((year, data) in years) {
            for (course in data["courses"]?.jsonObject?.values.orEmpty()) {
                val url = course.jsonObject["url"]?.jsonPrimitive?.content ?: continue
                val department = queryParameter(url, "idDIP") ?: continue
                val studyCourse = queryParameter(url, "idCDS") ?: continue
                val id = queryParameter(url, "idAD") ?: continue
                departments += department
                queryParameter(url, "idTC")?.let { studyCourses.getOrPut(department) { mutableMapOf() }.putIfAbsent(studyCourse, it) }
                listedStudyCourses.getOrPut(year) { mutableSetOf() } += studyCourse
                courses.getOrPut(studyCourse) { sortedSetOf() } += id
                for (teacher in course.jsonObject["teachers"]?.jsonArray.orEmpty()) {
                    val teacherId = teacher.jsonObject["url"]?.jsonPrimitive?.content?.let { queryParameter(it, "idDOC") } ?: continue
                    teachers.getOrPut(id) { sortedSetOf() } += teacherId
                    studyCourseTeachers.getOrPut(studyCourse to year) { sortedSetOf() } += teacherId
                }
            }
        }
    }

    companion object {
        /** A study course's code: its degree class, its number in the class, and its school's letter, such as `F5102P`. */
        private val STUDY_COURSE = Regex("""([A-Z]\d{2})(\d{2})([A-Z])""")

        /** A course's code: its study course's, and its number, such as `I0102D901`. */
        private val COURSE = Regex("""(.+?)(\d{3})""")

        /**
         * The study-course codes missing from the numbering of [codes] (each with its type), and
         * the one after the last of each class: `F5102P`, between `F5101P` and `F5103P`, with
         * their type.
         */
        fun studyCourseHoles(codes: Map<String, String>): Map<String, String> {
            val holes = sortedMapOf<String, String>()
            codes.entries.mapNotNull { (code, type) -> STUDY_COURSE.matchEntire(code)?.let { it to type } }
                .groupBy({ (match, type) -> Triple(match.groupValues[1], match.groupValues[3], type) }, { (match, _) -> match.groupValues[2].toInt() })
                .forEach { (group, numbers) ->
                    val (degreeClass, school, type) = group
                    for (number in 1..numbers.max() + 1) {
                        val code = "%s%02d%s".format(Locale.ROOT, degreeClass, number, school)
                        if (number !in numbers && code !in codes) holes.putIfAbsent(code, type)
                    }
                }
            return holes
        }

        /**
         * The course codes missing from the numbering of [codes], within each hundred they use,
         * and the one after the last of each: `I0102D002` among `I0102D001` and `I0102D003`.
         */
        fun courseHoles(codes: Set<String>): Set<String> {
            val holes = sortedSetOf<String>()
            codes.mapNotNull { COURSE.matchEntire(it) }
                .groupBy({ it.groupValues[1] to it.groupValues[2].toInt() / 100 }, { it.groupValues[2].toInt() })
                .forEach { (group, numbers) ->
                    val (studyCourse, hundred) = group
                    for (number in maxOf(hundred * 100, 1)..minOf(numbers.max() + 1, hundred * 100 + 99)) {
                        val code = "%s%03d".format(Locale.ROOT, studyCourse, number)
                        if (number !in numbers && code !in codes) holes += code
                    }
                }
            return holes
        }

        /**
         * The one choice among [counts] that adds up to [missing], as indices: null when none
         * does, or when more than one does, which proves nothing.
         */
        fun closing(counts: List<Int>, missing: Int): List<Int>? {
            if (missing <= 0 || counts.any { it <= 0 }) return null
            // ways[i][s]: how many choices among the first i counts add up to s, stopping at 2.
            val ways = Array(counts.size + 1) { ByteArray(missing + 1) }
            ways[0][0] = 1
            for (i in counts.indices) {
                for (sum in 0..missing) {
                    var total = ways[i][sum].toInt()
                    if (sum >= counts[i]) total += ways[i][sum - counts[i]]
                    ways[i + 1][sum] = minOf(total, 2).toByte()
                }
            }
            if (ways[counts.size][missing].toInt() != 1) return null
            val chosen = mutableListOf<Int>()
            var sum = missing
            for (i in counts.indices.reversed()) {
                // The one choice either leaves this count out, or takes it.
                if (ways[i][sum].toInt() == 0) {
                    chosen += i
                    sum -= counts[i]
                }
            }
            return chosen.reversed()
        }
    }
}
