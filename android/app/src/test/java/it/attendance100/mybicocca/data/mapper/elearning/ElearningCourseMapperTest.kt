package it.attendance100.mybicocca.data.mapper.elearning

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.local.elearning.course.ActivityCompletionEntity
import it.attendance100.mybicocca.data.local.elearning.course.CourseModuleEntity
import it.attendance100.mybicocca.data.local.elearning.course.CourseSectionEntity
import it.attendance100.mybicocca.data.local.elearning.course.CourseStaffEntity
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningActivityCompletionStatus
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningCourseModule
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningCoursePublicInfo
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningCoursePublicMetadata
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningCourseSection
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningCourseStaffGroup
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningCourseStaffMember
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningCourseSyllabus
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningCourseSyllabusField
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningEnrolledCourse
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningModuleContent
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningModuleDate
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseLevel
import it.attendance100.mybicocca.domain.model.elearning.course.CourseStaffRole
import it.attendance100.mybicocca.domain.model.elearning.course.ModuleType
import it.attendance100.mybicocca.domain.model.elearning.course.Semester
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.util.Locale

/**
 * Covers the Moodle course-content mappers: {mlang} resolution, enrolled-course field
 * collapse (displayName fallback, idNumber blanking, nullable-boolean defaults, epoch-second
 * normalization), section/module flattening (sort order, teacher vs user visibility,
 * subsection customdata, afterlink plain-texting, dates/contents JSON), completion-state
 * codes, staff numbering and role resolution, and the public-info syllabus language pick
 * plus its metadata-driven level/semester matching.
 */
class ElearningCourseMapperTest {

    private val account = AccountId("acc-1")
    private lateinit var savedLocale: Locale

    @Before
    fun fixLocale() {
        savedLocale = Locale.getDefault()
        Locale.setDefault(Locale.ENGLISH)
    }

    @After
    fun restoreLocale() {
        Locale.setDefault(savedLocale)
    }

    @Test
    fun `stripMlang keeps the english block under an english locale`() {
        val raw = "{mlang it}Matematica{mlang}{mlang en}Mathematics{mlang}"
        assertThat(raw.stripMlang()).isEqualTo("Mathematics")
    }

    @Test
    fun `stripMlang keeps the italian block under an italian locale`() {
        Locale.setDefault(Locale.ITALIAN)
        val raw = "{mlang it}Matematica{mlang}{mlang en}Mathematics{mlang}"
        assertThat(raw.stripMlang()).isEqualTo("Matematica")
    }

    @Test
    fun `stripMlang returns the input unchanged when no markup is present`() {
        assertThat("Plain name".stripMlang()).isEqualTo("Plain name")
    }

    @Test
    fun `stripMlang preserves text outside blocks`() {
        val raw = "Intro {mlang it}ita{mlang}{mlang en}eng{mlang} outro"
        assertThat(raw.stripMlang()).isEqualTo("Intro eng outro")
    }

    @Test
    fun `stripMlang resolves a regional locale to its base language block`() {
        Locale.setDefault(Locale.forLanguageTag("en-US"))
        val raw = "{mlang it}ita{mlang}{mlang en}eng{mlang}"
        assertThat(raw.stripMlang()).isEqualTo("eng")
    }

    @Test
    fun `stripMlang falls back to the other block when the language is missing`() {
        Locale.setDefault(Locale.forLanguageTag("de"))
        val raw = "{mlang it}ita{mlang}{mlang other}fallback{mlang}"
        assertThat(raw.stripMlang()).isEqualTo("fallback")
    }

    private fun enrolledCourse(
        id: Int = 5,
        shortName: String = "MAT",
        fullName: String = "Matematica",
        displayName: String? = null,
        idNumber: String? = "2526-1-E3101Q113",
        completed: Boolean? = null,
        completionEnabled: Boolean? = null,
        isFavourite: Boolean? = null,
        hidden: Boolean? = null,
        startDate: Long? = 1_700_000_000L,
        progress: Double? = 50.0,
    ) = ElearningEnrolledCourse(
        id = id,
        shortName = shortName,
        fullName = fullName,
        displayName = displayName,
        identificationNumber = idNumber,
        completed = completed,
        completionEnabled = completionEnabled,
        isFavourite = isFavourite,
        hidden = hidden,
        startDateTimestamp = startDate,
        progress = progress,
    )

    @Test
    fun `enrolled course toEntity falls back display name to full name`() {
        assertThat(enrolledCourse(displayName = null, fullName = "Matematica").toEntity(account, 0).displayName)
            .isEqualTo("Matematica")
    }

    @Test
    fun `enrolled course toEntity strips mlang from the names`() {
        val course = enrolledCourse(
            shortName = "{mlang it}MAT{mlang}{mlang en}MATH{mlang}",
            fullName = "{mlang it}Matematica{mlang}{mlang en}Mathematics{mlang}",
            displayName = "{mlang it}Matematica{mlang}{mlang en}Mathematics{mlang}",
        )
        val entity = course.toEntity(account, 0)
        assertThat(entity.shortName).isEqualTo("MATH")
        assertThat(entity.fullName).isEqualTo("Mathematics")
        assertThat(entity.displayName).isEqualTo("Mathematics")
    }

    @Test
    fun `enrolled course toEntity blanks an empty id number to null`() {
        assertThat(enrolledCourse(idNumber = "  ").toEntity(account, 0).idNumber).isNull()
        assertThat(enrolledCourse(idNumber = null).toEntity(account, 0).idNumber).isNull()
        assertThat(enrolledCourse(idNumber = "code").toEntity(account, 0).idNumber).isEqualTo("code")
    }

    @Test
    fun `enrolled course toEntity collapses nullable booleans to false`() {
        val entity = enrolledCourse(completed = null, completionEnabled = null, isFavourite = null, hidden = null)
            .toEntity(account, 0)
        assertThat(entity.completed).isFalse()
        assertThat(entity.completionEnabled).isFalse()
        assertThat(entity.isFavourite).isFalse()
        assertThat(entity.hidden).isFalse()
    }

    @Test
    fun `enrolled course toEntity records the sort order and normalizes timestamps`() {
        val entity = enrolledCourse(startDate = 1_700_000_000L).toEntity(account, sortOrder = 7)
        assertThat(entity.sortOrder).isEqualTo(7)
        assertThat(entity.startDateMs).isEqualTo(1_700_000_000_000L)
    }

    @Test
    fun `enrolled course toEntity reads a zero timestamp as absent`() {
        assertThat(enrolledCourse(startDate = 0L).toEntity(account, 0).startDateMs).isNull()
    }

    @Test
    fun `enrolled course entity toDomain converts progress to float and ms to dates`() {
        val entity = enrolledCourse(progress = 50.0, startDate = 1_700_000_000L).toEntity(account, 0)
        val domain = entity.toDomain()
        assertThat(domain.id).isEqualTo(CourseId(5))
        assertThat(domain.progress).isEqualTo(50f)
        assertThat(domain.startDate).isEqualTo(LocalDate.of(2023, 11, 14))
    }

    @Test
    fun `enrolled course entity toDomain attaches the supplied deadlines`() {
        val entity = enrolledCourse().toEntity(account, 0)
        assertThat(entity.toDomain().deadlines).isEmpty()
    }

    private fun module(
        id: Int = 100,
        name: String = "Lezione",
        moduleName: String? = "resource",
        visible: Int? = 1,
        userVisible: Boolean? = true,
        indent: Int? = 0,
        afterLinkHtml: String? = null,
        customData: String? = null,
        contents: List<ElearningModuleContent>? = null,
        dates: List<ElearningModuleDate>? = null,
        modulePluralName: String? = "Risorse",
        visibleOnCoursePage: Int? = 1,
    ) = ElearningCourseModule(
        id = id,
        name = name,
        moduleName = moduleName,
        visible = visible,
        userVisible = userVisible,
        indent = indent,
        afterLinkHtml = afterLinkHtml,
        customData = customData,
        contents = contents,
        dates = dates,
        modulePluralName = modulePluralName,
        visibleOnCoursePage = visibleOnCoursePage,
        instanceId = 200,
    )

    private fun section(
        id: Int = 1,
        name: String = "Sezione",
        sectionNumber: Int? = 0,
        visible: Int? = 1,
        modules: List<ElearningCourseModule> = emptyList(),
    ) = ElearningCourseSection(
        id = id,
        name = name,
        sectionNumber = sectionNumber,
        visible = visible,
        modules = modules,
    )

    @Test
    fun `section toEntity reads the 0-1 visible flag and strips mlang`() {
        assertThat(section(visible = 0).toEntity(account, courseId = 5).visible).isFalse()
        assertThat(section(visible = 1).toEntity(account, courseId = 5).visible).isTrue()
        assertThat(
            section(name = "{mlang it}Generale{mlang}{mlang en}General{mlang}").toEntity(account, 5).name,
        ).isEqualTo("General")
    }

    @Test
    fun `section toEntity defaults a null section number to zero`() {
        assertThat(section(sectionNumber = null).toEntity(account, 5).sectionNumber).isEqualTo(0)
    }

    @Test
    fun `module entities record their position within the section as sort order`() {
        val sec = section(modules = listOf(module(id = 100), module(id = 101), module(id = 102)))
        val entities = listOf(sec).toModuleEntities(account, courseId = 5)
        assertThat(entities.map { it.cmId }).containsExactly(100, 101, 102).inOrder()
        assertThat(entities.map { it.sortOrder }).containsExactly(0, 1, 2).inOrder()
        assertThat(entities.all { it.sectionId == 1 }).isTrue()
    }

    @Test
    fun `module entities keep a user-gated module marked inaccessible`() {
        val entities = listOf(section(modules = listOf(module(visible = 1, userVisible = false))))
            .toModuleEntities(account, 5)
        val entity = entities.single()
        assertThat(entity.visible).isTrue()
        assertThat(entity.accessible).isFalse()
    }

    @Test
    fun `module entities reduce the afterlink html to plain text`() {
        val entities = listOf(section(modules = listOf(module(afterLinkHtml = "<span>7 unread posts</span>"))))
            .toModuleEntities(account, 5)
        assertThat(entities.single().afterLink).isEqualTo("7 unread posts")
    }

    @Test
    fun `module entities yield a null afterlink for blank html`() {
        val entities = listOf(section(modules = listOf(module(afterLinkHtml = "   "))))
            .toModuleEntities(account, 5)
        assertThat(entities.single().afterLink).isNull()
    }

    @Test
    fun `module entities resolve the linked section id from subsection customdata`() {
        val entities = listOf(section(modules = listOf(module(customData = "{\"sectionid\":\"333817\"}"))))
            .toModuleEntities(account, 5)
        assertThat(entities.single().linkedSectionId).isEqualTo(333817)
    }

    @Test
    fun `module entities yield a null linked section for empty customdata`() {
        val entities = listOf(section(modules = listOf(module(customData = "\"\""))))
            .toModuleEntities(account, 5)
        assertThat(entities.single().linkedSectionId).isNull()
    }

    @Test
    fun `module entities pack dates and contents into the json columns`() {
        val mod = module(
            contents = listOf(ElearningModuleContent(type = "file", fileName = "a.pdf", fileSize = 10L, timeModifiedTimestamp = 1_000L)),
            dates = listOf(ElearningModuleDate(label = "Data limite:", timestamp = 2_000L, dataId = "duedate")),
        )
        val entity = listOf(section(modules = listOf(mod))).toModuleEntities(account, 5).single()
        assertThat(entity.contentsJson).isNotNull()
        assertThat(entity.contentsJson).contains("a.pdf")
        assertThat(entity.datesJson).isNotNull()
        assertThat(entity.datesJson).contains("duedate")
    }

    @Test
    fun `module entities leave the json columns null when there are no dates or contents`() {
        val entity = listOf(section(modules = listOf(module()))).toModuleEntities(account, 5).single()
        assertThat(entity.contentsJson).isNull()
        assertThat(entity.datesJson).isNull()
    }

    @Test
    fun `module entities drop a date with no timestamp`() {
        val mod = module(dates = listOf(ElearningModuleDate(label = "x", timestamp = null, dataId = "duedate")))
        val entity = listOf(section(modules = listOf(mod))).toModuleEntities(account, 5).single()
        assertThat(entity.datesJson).isNull()
    }

    @Test
    fun `section entity toDomain picks its own modules by section id and restores order`() {
        val sec = section(modules = listOf(module(id = 100), module(id = 101)))
        val modules = listOf(sec).toModuleEntities(account, 5).reversed()
        val other = CourseModuleEntity(
            accountId = "acc-1", courseId = 5, cmId = 900, sectionId = 999, sortOrder = 0,
            instanceId = null, name = "other", modName = "page", typeLabel = null, description = null,
            url = null, iconUrl = null, visible = true, accessible = true, availabilityInfo = null,
            onCoursePage = true, indent = 0, afterLink = null, linkedSectionId = null,
            datesJson = null, contentsJson = null,
        )
        val domain = sec.toEntity(account, 5).toDomain(modules + other)
        assertThat(domain.id).isEqualTo(1)
        assertThat(domain.modules.map { it.cmId }).containsExactly(100, 101).inOrder()
    }

    @Test
    fun `module entity toDomain resolves the type and decodes dates and contents`() {
        val mod = module(
            moduleName = "assign",
            contents = listOf(ElearningModuleContent(type = "file", fileName = "a.pdf", fileSize = 10L)),
            dates = listOf(ElearningModuleDate(label = "Data limite:", timestamp = 2_000L, dataId = "duedate")),
        )
        val entity = listOf(section(modules = listOf(mod))).toModuleEntities(account, 5).single()
        val domain = entity.toDomain()
        assertThat(domain.type).isEqualTo(ModuleType.Assign)
        assertThat(domain.completion).isNull()
        assertThat(domain.contents.single().fileName).isEqualTo("a.pdf")
        assertThat(domain.dates.single().dataId).isEqualTo("duedate")
        assertThat(domain.dates.single().instant).isEqualTo(Instant.ofEpochMilli(2_000_000L))
    }

    @Test
    fun `module entity toDomain collapses an unknown module name to other`() {
        val entity = listOf(section(modules = listOf(module(moduleName = "exoticplugin"))))
            .toModuleEntities(account, 5).single()
        assertThat(entity.toDomain().type).isEqualTo(ModuleType.Other)
    }

    @Test
    fun `module entity toDomain drops a malformed contents blob`() {
        val entity = listOf(section(modules = listOf(module()))).toModuleEntities(account, 5).single()
            .copy(contentsJson = "{bad json", datesJson = "{bad json")
        val domain = entity.toDomain()
        assertThat(domain.contents).isEmpty()
        assertThat(domain.dates).isEmpty()
    }

    @Test
    fun `completion status toEntity treats states 2 and 3 as completed`() {
        assertThat(completion(state = 2).toEntity(account, 5).isCompleted).isTrue()
        assertThat(completion(state = 3).toEntity(account, 5).isCompleted).isTrue()
        assertThat(completion(state = 1).toEntity(account, 5).isCompleted).isFalse()
    }

    @Test
    fun `completion status toEntity reads state 0 as untracked`() {
        val entity = completion(state = 0).toEntity(account, 5)
        assertThat(entity.isTracked).isFalse()
        assertThat(entity.isCompleted).isFalse()
    }

    @Test
    fun `completion status toEntity maps the tracking mode flags`() {
        assertThat(completion(tracking = 1).toEntity(account, 5).isManual).isTrue()
        assertThat(completion(tracking = 1).toEntity(account, 5).isAutomatic).isFalse()
        assertThat(completion(tracking = 2).toEntity(account, 5).isAutomatic).isTrue()
        assertThat(completion(tracking = null).toEntity(account, 5).isManual).isFalse()
    }

    @Test
    fun `completion status toEntity normalizes the completed timestamp`() {
        assertThat(completion(timeCompleted = 4_000L).toEntity(account, 5).completedAtMs).isEqualTo(4_000_000L)
        assertThat(completion(timeCompleted = 0L).toEntity(account, 5).completedAtMs).isNull()
    }

    @Test
    fun `completion entity toDomain copies the flags and the instant`() {
        val entity = ActivityCompletionEntity(
            accountId = "acc-1", courseId = 5, cmId = 100, isCompleted = true,
            completedAtMs = 4_000_000L, isManual = false, isAutomatic = true, isTracked = true,
        )
        val domain = entity.toDomain()
        assertThat(domain.cmId).isEqualTo(100)
        assertThat(domain.isCompleted).isTrue()
        assertThat(domain.isAutomatic).isTrue()
        assertThat(domain.completedAt).isEqualTo(Instant.ofEpochMilli(4_000_000L))
    }

    @Test
    fun `staff entities are numbered across the role groups`() {
        val info = publicInfo(
            staff = listOf(
                ElearningCourseStaffGroup(
                    role = "Docente",
                    members = listOf(staffMember("Mario Rossi"), staffMember("Anna Verdi")),
                ),
                ElearningCourseStaffGroup(
                    role = "Tutor",
                    members = listOf(staffMember("Luca Bianchi")),
                ),
            ),
        )
        val entities = info.toStaffEntities(account, courseId = 5)
        assertThat(entities.map { it.rowIndex }).containsExactly(0, 1, 2).inOrder()
        assertThat(entities.map { it.fullName }).containsExactly("Mario Rossi", "Anna Verdi", "Luca Bianchi").inOrder()
        assertThat(entities.map { it.roleRaw }).containsExactly("Docente", "Docente", "Tutor").inOrder()
    }

    @Test
    fun `staff entity toDomain resolves the role from its raw label`() {
        val entity = CourseStaffEntity(
            accountId = "acc-1", courseId = 5, rowIndex = 0, userId = 7, fullName = "Mario Rossi",
            roleRaw = "Esercitatore", initials = "MR", email = "m@x.it", profileUrl = "https://p",
        )
        val domain = entity.toDomain()
        assertThat(domain.role).isEqualTo(CourseStaffRole.Esercitatore)
        assertThat(domain.fullName).isEqualTo("Mario Rossi")
    }

    @Test
    fun `staff entity toDomain collapses an unknown role to other`() {
        val entity = CourseStaffEntity(
            accountId = "acc-1", courseId = 5, rowIndex = 0, userId = null, fullName = "X",
            roleRaw = "Coordinatore", initials = null, email = null, profileUrl = null,
        )
        assertThat(entity.toDomain().role).isEqualTo(CourseStaffRole.Other)
    }

    @Test
    fun `syllabus entity prefers the english tab under an english locale`() {
        val info = publicInfo(
            syllabus = listOf(
                ElearningCourseSyllabus(language = "it", exportPdfUrl = "pdf-it", fields = emptyList()),
                ElearningCourseSyllabus(language = "en", exportPdfUrl = "pdf-en", fields = emptyList()),
            ),
        )
        val entity = info.toSyllabusEntity(account, courseId = 5)
        assertThat(entity).isNotNull()
        assertThat(entity!!.language).isEqualTo("en")
        assertThat(entity.exportPdfUrl).isEqualTo("pdf-en")
    }

    @Test
    fun `syllabus entity falls back to the first tab when neither app language nor english exist`() {
        val info = publicInfo(
            syllabus = listOf(
                ElearningCourseSyllabus(language = "fr", exportPdfUrl = "pdf-fr", fields = emptyList()),
                ElearningCourseSyllabus(language = "de", exportPdfUrl = "pdf-de", fields = emptyList()),
            ),
        )
        assertThat(info.toSyllabusEntity(account, 5)?.language).isEqualTo("fr")
    }

    @Test
    fun `syllabus entity returns null when the page has no tabs`() {
        assertThat(publicInfo(syllabus = emptyList()).toSyllabusEntity(account, 5)).isNull()
    }

    @Test
    fun `syllabus round-trips the metadata-derived facts through the blob`() {
        val info = publicInfo(
            metadata = ElearningCoursePublicMetadata(
                disciplinarySector = "INF/01",
                cfu = 8,
                period = "Primo Semestre",
                activityType = "Obbligatorio",
                hours = 64,
                degreeType = "Laurea Triennale",
                language = "Italiano",
            ),
            syllabus = listOf(
                ElearningCourseSyllabus(
                    language = "en",
                    exportPdfUrl = null,
                    fields = listOf(ElearningCourseSyllabusField(title = "Aims", htmlContent = "<p>learn things</p>")),
                ),
            ),
        )
        val domain = info.toSyllabusEntity(account, 5)!!.toDomain()
        assertThat(domain.info.credits).isEqualTo(8)
        assertThat(domain.info.hours).isEqualTo(64)
        assertThat(domain.info.disciplinarySector).isEqualTo("INF/01")
        assertThat(domain.info.level).isEqualTo(CourseLevel.Bachelor)
        assertThat(domain.info.semester).isEqualTo(Semester.First)
        assertThat(domain.info.objectives).isEqualTo("learn things")
        assertThat(domain.fields.single().title).isEqualTo("Aims")
    }

    @Test
    fun `syllabus matches the magistrale degree type to the master level`() {
        val info = publicInfo(
            metadata = ElearningCoursePublicMetadata(
                disciplinarySector = null, cfu = null, period = "Secondo semestre", activityType = null,
                hours = null, degreeType = "Laurea Magistrale", language = null,
            ),
            syllabus = listOf(ElearningCourseSyllabus(language = "en", exportPdfUrl = null, fields = emptyList())),
        )
        val domain = info.toSyllabusEntity(account, 5)!!.toDomain()
        assertThat(domain.info.level).isEqualTo(CourseLevel.Master)
        assertThat(domain.info.semester).isEqualTo(Semester.Second)
    }

    private fun completion(
        cmId: Int = 100,
        state: Int = 2,
        tracking: Int? = 1,
        timeCompleted: Long? = 4_000L,
    ) = ElearningActivityCompletionStatus(
        courseModuleId = cmId,
        state = state,
        tracking = tracking,
        timeCompleted = timeCompleted,
    )

    private fun staffMember(name: String) = ElearningCourseStaffMember(
        name = name,
        profileUrl = "https://moodle/profile",
        userId = 7,
        initials = name.split(" ").map { it.first() }.joinToString(""),
        email = "x@y.it",
    )

    private fun publicInfo(
        metadata: ElearningCoursePublicMetadata = ElearningCoursePublicMetadata(
            disciplinarySector = null, cfu = null, period = null, activityType = null,
            hours = null, degreeType = null, language = null,
        ),
        syllabus: List<ElearningCourseSyllabus> = emptyList(),
        staff: List<ElearningCourseStaffGroup> = emptyList(),
    ) = ElearningCoursePublicInfo(
        id = 5,
        name = "Matematica",
        code = "2526-1-E3101Q113",
        viewUrl = "https://elearning/course",
        metadata = metadata,
        syllabus = syllabus,
        staff = staff,
        enrolmentMethods = emptyList(),
        studentOpinionUrl = null,
        bibliographyUrl = null,
    )
}
