package it.attendance100.mybicocca.demo

import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import it.attendance100.mybicocca.domain.model.exam.ExamCallKey
import java.io.ByteArrayOutputStream
import java.time.LocalDate

/** One activity of the demo student's degree, as Esse3 would list it in the transcript. */
data class DemoActivity(
    val id: Long,
    val code: String,
    val name: String,
    val year: Int,
    val credits: Float,
    val grade: Int? = null,
    val cumLaude: Boolean = false,
    val passedOn: LocalDate? = null,
    val attending: Boolean = false,
) {
    val passed: Boolean get() = passedOn != null
}

/**
 * The Esse3 side of the demo student: a three-year Informatica degree with the first year and a
 * half passed, the [DemoCourses] being attended now, and the third year still to come.
 */
object DemoCareer {

    const val COURSE_OF_STUDY_ID = 10001L
    const val DEGREE = "INFORMATICA"
    const val TOTAL_CREDITS = 180f

    /** Sat but not yet recorded: its result is the one still waiting for the student to accept. */
    const val WEB_LAB_ID = 20110L

    private fun passed(id: Long, code: String, name: String, year: Int, credits: Float, grade: Int?, on: LocalDate, laude: Boolean = false) =
        DemoActivity(id, code, name, year, credits, grade, laude, on)

    private fun attending(course: DemoCourse) =
        DemoActivity(activityId(course), course.code, course.name, 2, course.cfu.toFloat(), attending = true)

    fun activityId(course: DemoCourse): Long = 20000L + course.moodleCourseId

    val activities: List<DemoActivity> = listOf(
        passed(activityId(DemoCourses.programming), "E3101Q101", "PROGRAMMAZIONE 1", 1, 8f, 28, LocalDate.of(2025, 1, 22)),
        passed(20002, "E3101Q102", "FONDAMENTI DELL'INFORMATICA", 1, 8f, 27, LocalDate.of(2025, 2, 10)),
        passed(20003, "E3101Q103", "ANALISI MATEMATICA", 1, 8f, 24, LocalDate.of(2025, 2, 24)),
        passed(20004, "E3101Q104", "ARCHITETTURA DEGLI ELABORATORI", 1, 8f, 30, LocalDate.of(2025, 6, 17), laude = true),
        passed(20005, "E3101Q105", "PROGRAMMAZIONE 2", 1, 8f, 29, LocalDate.of(2025, 7, 3)),
        passed(20006, "E3101Q111", "ALGEBRA LINEARE E GEOMETRIA", 1, 8f, 26, LocalDate.of(2025, 7, 18)),
        passed(20007, "E3101Q112", "LINGUA INGLESE", 1, 3f, null, LocalDate.of(2025, 9, 9)),
        passed(20008, "E3101Q113", "FISICA", 2, 8f, 25, LocalDate.of(2026, 1, 20)),
        passed(20009, "E3101Q114", "RICERCA OPERATIVA", 2, 8f, 28, LocalDate.of(2026, 2, 5)),
        DemoActivity(WEB_LAB_ID, "E3101Q119", "LABORATORIO DI PROGRAMMAZIONE WEB", 2, 3f),
    ) + DemoCourses.current.map(::attending) + listOf(
        DemoActivity(activityId(DemoCourses.softwareEngineering), "E3101Q115", "INGEGNERIA DEL SOFTWARE", 3, 8f),
        DemoActivity(20102, "E3101Q116", "RETI DI CALCOLATORI", 3, 8f),
        DemoActivity(20103, "E3101Q117", "SICUREZZA INFORMATICA", 3, 6f),
        DemoActivity(20104, "E3101Q118", "INTELLIGENZA ARTIFICIALE", 3, 8f),
        DemoActivity(20105, "E3101Q190", "STAGE", 3, 12f),
        DemoActivity(20106, "E3101Q199", "PROVA FINALE", 3, 4f),
    )

    fun activity(course: DemoCourse): DemoActivity = activities.first { it.id == activityId(course) }

    fun callKey(activity: DemoActivity, call: Int) =
        ExamCallKey(courseOfStudyId = COURSE_OF_STUDY_ID, activityId = activity.id, callId = call)

    /** A single-page PDF standing in for the documents the university would generate. */
    fun pdf(title: String, lines: List<String>): ByteArray {
        val document = PdfDocument()
        val page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val heading = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val body = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 12f }
        page.canvas.drawText("Università degli Studi di Milano-Bicocca", 48f, 64f, body)
        page.canvas.drawText(title, 48f, 104f, heading)
        (listOf("${DemoStudent.FULL_NAME} · matricola ${DemoStudent.STUDENT_NUMBER}", "") + lines + listOf("", "Documento dimostrativo, privo di valore."))
            .forEachIndexed { index, line -> page.canvas.drawText(line, 48f, 144f + index * 20f, body) }
        document.finishPage(page)
        return ByteArrayOutputStream().also { document.writeTo(it); document.close() }.toByteArray()
    }
}
