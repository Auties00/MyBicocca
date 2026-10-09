package it.attendance100.mybicocca.demo

import it.attendance100.mybicocca.domain.model.elearning.assignment.AssignmentId
import it.attendance100.mybicocca.domain.model.elearning.course.CompletionState
import it.attendance100.mybicocca.domain.model.elearning.course.CourseDetails
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseLevel
import it.attendance100.mybicocca.domain.model.elearning.course.CourseModule
import it.attendance100.mybicocca.domain.model.elearning.course.CourseSection
import it.attendance100.mybicocca.domain.model.elearning.course.CourseStaffMember
import it.attendance100.mybicocca.domain.model.elearning.course.CourseStaffRole
import it.attendance100.mybicocca.domain.model.elearning.course.CourseSyllabusPointer
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourse
import it.attendance100.mybicocca.domain.model.elearning.course.ModuleContent
import it.attendance100.mybicocca.domain.model.elearning.course.ModuleDate
import it.attendance100.mybicocca.domain.model.elearning.course.ModuleType
import it.attendance100.mybicocca.domain.model.elearning.course.ProgrammeSection
import it.attendance100.mybicocca.domain.model.elearning.course.Semester
import it.attendance100.mybicocca.domain.model.elearning.course.SyllabusInfo
import it.attendance100.mybicocca.domain.model.elearning.deadline.Deadline
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizId
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * The Moodle side of the demo student: one course page per [DemoCourse], each built from the
 * same template of weekly sections so only the topics differ.
 */
object DemoElearning {

    const val BASE_URL = "https://demo.invalid"

    /** Activity ids of a course are derived from its Moodle id, so they never collide. */
    fun cmId(course: DemoCourse, index: Int): Int = course.moodleCourseId * 100 + index
    fun forumId(course: DemoCourse): Int = course.moodleCourseId + 2000
    fun quizId(course: DemoCourse): Int = course.moodleCourseId + 1100

    private const val FORUM = 1
    private const val SYLLABUS_PDF = 2
    const val ASSIGNMENT = 80
    const val QUIZ = 91

    private val topics = mapOf(
        DemoCourses.algorithms to listOf("Analisi asintotica", "Ordinamento", "Alberi binari di ricerca", "Grafi e visite", "Programmazione dinamica"),
        DemoCourses.databases to listOf("Modello relazionale", "Algebra relazionale", "SQL", "Progettazione concettuale", "Normalizzazione"),
        DemoCourses.operatingSystems to listOf("Processi e thread", "Scheduling della CPU", "Sincronizzazione", "Gestione della memoria", "File system"),
        DemoCourses.statistics to listOf("Spazi di probabilità", "Variabili aleatorie", "Distribuzioni notevoli", "Stima puntuale", "Test di ipotesi"),
        DemoCourses.languages to listOf("Paradigmi di programmazione", "Programmazione funzionale", "Sistemi di tipi", "Programmazione logica", "Interpreti"),
        DemoCourses.programming to listOf("Variabili e tipi", "Strutture di controllo", "Funzioni", "Array e stringhe", "Ricorsione"),
        DemoCourses.softwareEngineering to listOf("Processi di sviluppo", "Requisiti", "UML", "Design pattern", "Testing"),
    )

    /** Courses left out have no assignments, so the list and course pages show that case too. */
    private val assignmentsByCourse: Map<DemoCourse, List<DemoAssignment>> = mapOf(
        DemoCourses.databases to listOf(),
        DemoCourses.operatingSystems to listOf(
            DemoAssignment(DemoCourses.operatingSystems, 0, "Esercitazione 6: scheduling", DemoClock.now.withHour(23).withMinute(59), submitted = true),
        ),
        DemoCourses.languages to listOf(
            DemoAssignment(DemoCourses.languages, 0, "Progetto: interprete", DemoClock.now.plusDays(15).withHour(23).withMinute(59)),
        ),
    )

    /** Null where the quiz never closes, so that course lists no quiz deadline. */
    fun quizClosesAt(course: DemoCourse): Instant? =
        if (course == DemoCourses.databases || course == DemoCourses.programming) null
        else DemoClock.instant(DemoClock.now.plusDays(20))

    fun assignments(course: DemoCourse): List<DemoAssignment> = assignmentsByCourse[course].orEmpty()

    val allAssignments: List<DemoAssignment> = DemoCourses.all.flatMap(::assignments)

    private val progress = mapOf(
        DemoCourses.algorithms to 0.72f,
        DemoCourses.databases to 0.64f,
        DemoCourses.operatingSystems to 0.55f,
        DemoCourses.statistics to 0.81f,
        DemoCourses.languages to 0.38f,
        DemoCourses.programming to 1.0f,
        DemoCourses.softwareEngineering to 0.12f,
    )

    private fun yearsAgo(course: DemoCourse): Long = if (course.academicYear == "2526") 0 else 1

    fun course(id: CourseId): DemoCourse? = DemoCourses.all.firstOrNull { it.moodleCourseId == id.value }

    val enrolled: List<EnrolledCourse> = DemoCourses.all.mapIndexed { index, course ->
        EnrolledCourse(
            id = CourseId(course.moodleCourseId),
            shortName = course.shortLabel,
            fullName = titleCase(course.name),
            displayName = titleCase(course.name),
            idNumber = "${course.academicYear}-${course.year}-${course.code}",
            summary = null,
            courseImageUrl = null,
            format = "topics",
            language = "it",
            categoryId = 310,
            progress = progress.getValue(course),
            completed = progress.getValue(course) >= 1.0f,
            completionEnabled = true,
            startDate = LocalDate.of(2026, 3, 2).minusYears(yearsAgo(course)),
            endDate = LocalDate.of(2026, 6, 26).minusYears(yearsAgo(course)),
            lastAccessDate = DemoClock.instant(DemoClock.now.minusHours(2L + index * 19)),
            isFavourite = index < 2,
            hidden = false,
            deadlines = (
                assignments(course).map {
                    Deadline.Assignment(
                        id = AssignmentId(it.id),
                        courseId = CourseId(course.moodleCourseId),
                        title = it.title,
                        dueAt = it.dueAt,
                    )
                } + listOfNotNull(
                    quizClosesAt(course)?.let {
                        Deadline.Quiz(
                            id = QuizId(quizId(course)),
                            courseId = CourseId(course.moodleCourseId),
                            title = "Quiz di autovalutazione",
                            dueAt = it,
                        )
                    },
                )
            ).filter { it.dueAt.isAfter(DemoClock.fixed.instant().minusSeconds(60)) },
        )
    }

    fun details(course: DemoCourse): CourseDetails = CourseDetails(
        enrolled = enrolled.first { it.id.value == course.moodleCourseId },
        sections = sections(course),
        staff = listOf(
            staff(course.teacher, CourseStaffRole.Docente, 1),
            staff(tutors.getValue(course), CourseStaffRole.Esercitatore, 2),
        ),
        syllabus = syllabus(course),
    )

    fun completionStates(course: DemoCourse): Map<Int, CompletionState> =
        sections(course).flatMap { it.modules }.mapNotNull { it.completion }.associateBy { it.cmId }

    private val tutors = mapOf(
        DemoCourses.algorithms to "Matteo Riva",
        DemoCourses.databases to "Chiara Fontana",
        DemoCourses.operatingSystems to "Luca Bassi",
        DemoCourses.statistics to "Anna Villa",
        DemoCourses.languages to "Simone Greco",
        DemoCourses.programming to "Irene Costa",
        DemoCourses.softwareEngineering to "Pietro Longo",
    )

    private fun staff(name: String, role: CourseStaffRole, index: Int) = CourseStaffMember(
        userId = 30000 + name.hashCode().mod(9000) + index,
        fullName = name,
        role = role,
        initials = name.split(' ').mapNotNull { it.firstOrNull() }.joinToString(""),
        email = name.lowercase().replace(' ', '.') + "@unimib.it",
        profileUrl = null,
    )

    private fun sections(course: DemoCourse): List<CourseSection> {
        val completedWeeks = (progress.getValue(course) * topics.getValue(course).size).toInt()
        val general = section(
            course, number = 0, name = "Informazioni generali",
            modules = listOf(
                module(course, FORUM, "Annunci", ModuleType.Forum, instanceId = forumId(course)),
                file(course, SYLLABUS_PDF, "Programma del corso", "programma.pdf", completed = true),
            ),
        )
        val weeks = topics.getValue(course).mapIndexed { week, topic ->
            val base = 10 + week * 10
            val done = week < completedWeeks
            section(
                course, number = week + 1, name = topic,
                modules = listOf(
                    file(course, base, "Slide ${week + 1}: $topic", "slide_${week + 1}.pdf", completed = done),
                    module(
                        course, base + 1, "Lezione ${week + 1}: registrazione", ModuleType.Kalvidres,
                        completed = done,
                        url = "$BASE_URL/mod/kalvidres/view.php?id=${cmId(course, base + 1)}",
                    ),
                    file(course, base + 2, "Esercizi ${week + 1}", "esercizi_${week + 1}.pdf", completed = done),
                ),
            )
        }
        val assessment = section(
            course, number = weeks.size + 1, name = "Consegne e verifiche",
            modules = assignments(course).map {
                module(
                    course, ASSIGNMENT + it.index, it.title, ModuleType.Assign,
                    instanceId = it.id,
                    dates = listOf(ModuleDate("Scadenza", it.dueAt, "duedate")),
                )
            } + listOf(
                module(
                    course, QUIZ, "Quiz di autovalutazione", ModuleType.Quiz,
                    instanceId = quizId(course),
                    dates = listOfNotNull(quizClosesAt(course)?.let { ModuleDate("Chiusura", it, "timeclose") }),
                ),
            ),
        )
        return listOf(general) + weeks + assessment
    }

    private fun section(course: DemoCourse, number: Int, name: String, modules: List<CourseModule>) = CourseSection(
        id = course.moodleCourseId * 10 + number,
        sectionNumber = number,
        name = name,
        summary = null,
        visible = true,
        component = null,
        itemId = null,
        modules = modules,
    )

    private fun file(course: DemoCourse, index: Int, name: String, fileName: String, completed: Boolean) = module(
        course, index, name, ModuleType.Resource,
        completed = completed,
        contents = listOf(
            ModuleContent(
                type = "file",
                fileName = fileName,
                filePath = "/",
                fileUrl = "$BASE_URL/files/${course.shortLabel.lowercase()}/$fileName",
                mimeType = "application/pdf",
                sizeBytes = 900_000L + index * 73_000L,
                timeModified = DemoClock.instant(LocalDateTime.of(2026, 3, 2, 9, 0).plusDays(index.toLong())),
            ),
        ),
    )

    private fun module(
        course: DemoCourse,
        index: Int,
        name: String,
        type: ModuleType,
        instanceId: Int? = null,
        completed: Boolean = false,
        url: String? = null,
        contents: List<ModuleContent> = emptyList(),
        dates: List<ModuleDate> = emptyList(),
    ): CourseModule {
        val cmId = cmId(course, index)
        return CourseModule(
            cmId = cmId,
            instanceId = instanceId,
            name = name,
            type = type,
            typeLabel = null,
            description = null,
            url = url ?: "$BASE_URL/mod/${type.raw}/view.php?id=$cmId",
            iconUrl = null,
            visible = true,
            accessible = true,
            availabilityInfo = null,
            onCoursePage = true,
            indent = 0,
            afterLink = null,
            linkedSectionId = null,
            contents = contents,
            completion = CompletionState(
                cmId = cmId,
                isCompleted = completed,
                completedAt = if (completed) DemoClock.instant(DemoClock.now.minusDays(index.toLong())) else null,
                isManual = true,
                isAutomatic = false,
                isTracked = type != ModuleType.Forum,
            ),
            dates = dates,
        )
    }

    private fun syllabus(course: DemoCourse) = CourseSyllabusPointer(
        language = "it",
        exportPdfUrl = null,
        fields = emptyList(),
        info = SyllabusInfo.Empty.copy(
            type = "Obbligatorio",
            credits = course.cfu,
            hours = course.cfu * 8,
            language = "Italiano",
            level = CourseLevel.Bachelor,
            semester = Semester.Second,
            disciplinarySector = "INF/01",
            objectives = "Fornire le conoscenze di base e gli strumenti metodologici della disciplina.",
            summary = topics.getValue(course).joinToString(", ") + ".",
            extendedProgramme = listOf(ProgrammeSection("Argomenti", topics.getValue(course))),
            prerequisites = "Programmazione 1, Fondamenti dell'informatica.",
            teachingMethod = "Lezioni frontali ed esercitazioni in aula e in laboratorio.",
            referenceMaterial = "Slide e dispense pubblicate sulla pagina del corso.",
            assessment = "Prova scritta e discussione orale facoltativa.",
            officeHours = "Su appuntamento, via e-mail.",
        ),
    )

    private fun titleCase(name: String): String =
        name.lowercase().split(' ').joinToString(" ") { word ->
            if (word in setOf("e", "di", "del", "della")) word else word.replaceFirstChar(Char::uppercase)
        }
}
