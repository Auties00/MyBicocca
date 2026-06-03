package it.attendance100.mybicocca.data.mapper.elearning

import it.attendance100.mybicocca.data.local.elearning.course.ActivityCompletionEntity
import it.attendance100.mybicocca.data.local.elearning.course.CourseModuleEntity
import it.attendance100.mybicocca.data.local.elearning.course.CourseSectionEntity
import it.attendance100.mybicocca.data.local.elearning.course.CourseStaffEntity
import it.attendance100.mybicocca.data.local.elearning.course.CourseSyllabusEntity
import it.attendance100.mybicocca.data.local.elearning.course.EnrolledCourseEntity
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningJson
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningActivityCompletionStatus
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningCoursePublicInfo
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningCourseSection
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningEnrolledCourse
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CompletionState
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseLevel
import it.attendance100.mybicocca.domain.model.elearning.course.CourseModule
import it.attendance100.mybicocca.domain.model.elearning.course.CourseSection
import it.attendance100.mybicocca.domain.model.elearning.course.CourseStaffMember
import it.attendance100.mybicocca.domain.model.elearning.course.CourseStaffRole
import it.attendance100.mybicocca.domain.model.elearning.course.CourseSyllabusPointer
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourse
import it.attendance100.mybicocca.domain.model.elearning.deadline.Deadline
import it.attendance100.mybicocca.domain.model.elearning.course.ModuleContent
import it.attendance100.mybicocca.domain.model.elearning.course.ModuleType
import it.attendance100.mybicocca.domain.model.elearning.course.ProgrammeSection
import it.attendance100.mybicocca.domain.model.elearning.course.Semester
import it.attendance100.mybicocca.domain.model.elearning.course.SyllabusInfo
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Serializable
internal data class ModuleContentJson(
    val type: String? = null,
    val fileName: String? = null,
    val fileUrl: String? = null,
    val mimeType: String? = null,
    val sizeBytes: Long? = null,
    val timeModifiedMs: Long? = null,
)

@Serializable
internal data class SyllabusFieldJson(val title: String, val htmlContent: String)

@Serializable
internal data class ProgrammeSectionJson(
    val title: String,
    val items: List<String> = emptyList(),
)

@Serializable
internal data class SyllabusInfoJson(
    val type: String? = null,
    val credits: Int? = null,
    val hours: Int? = null,
    val language: String? = null,
    val level: String? = null,
    val semester: String? = null,
    val disciplinarySector: String? = null,
    val objectives: String? = null,
    val summary: String? = null,
    val extendedProgramme: List<ProgrammeSectionJson> = emptyList(),
    val prerequisites: String? = null,
    val teachingMethod: String? = null,
    val referenceMaterial: String? = null,
    val assessment: String? = null,
    val officeHours: String? = null,
)

@Serializable
internal data class SyllabusBlobJson(
    val fields: List<SyllabusFieldJson> = emptyList(),
    val info: SyllabusInfoJson = SyllabusInfoJson(),
)

internal fun ElearningEnrolledCourse.toEntity(accountId: AccountId, sortOrder: Int): EnrolledCourseEntity =
    EnrolledCourseEntity(
        accountId = accountId.value,
        courseId = id,
        shortName = shortName,
        fullName = fullName,
        displayName = displayName ?: fullName,
        idNumber = identificationNumber?.takeIf { it.isNotBlank() },
        summary = summary,
        courseImageUrl = courseImageUrl,
        format = format,
        language = language,
        categoryId = categoryId,
        progress = progress,
        completed = completed == true,
        completionEnabled = completionEnabled == true,
        startDateMs = startDateTimestamp?.toMillisOrNull(),
        endDateMs = endDateTimestamp?.toMillisOrNull(),
        lastAccessMs = lastAccessTimestamp?.toMillisOrNull(),
        isFavourite = isFavourite == true,
        hidden = hidden == true,
        sortOrder = sortOrder,
    )

internal fun EnrolledCourseEntity.toDomain(deadlines: List<Deadline> = emptyList()): EnrolledCourse =
    EnrolledCourse(
        id = CourseId(courseId),
        shortName = shortName,
        fullName = fullName,
        displayName = displayName,
        idNumber = idNumber,
        summary = summary,
        courseImageUrl = courseImageUrl,
        format = format,
        language = language,
        categoryId = categoryId,
        progress = progress?.toFloat(),
        completed = completed,
        completionEnabled = completionEnabled,
        startDate = startDateMs?.toLocalDate(),
        endDate = endDateMs?.toLocalDate(),
        lastAccessDate = lastAccessMs?.let(Instant::ofEpochMilli),
        isFavourite = isFavourite,
        hidden = hidden,
        deadlines = deadlines,
    )

internal fun ElearningCourseSection.toEntity(accountId: AccountId, courseId: Int): CourseSectionEntity =
    CourseSectionEntity(
        accountId = accountId.value,
        courseId = courseId,
        sectionId = id,
        sectionNumber = sectionNumber ?: 0,
        name = name,
        summary = summary,
        visible = visible != 0,
    )

internal fun List<ElearningCourseSection>.toModuleEntities(
    accountId: AccountId,
    courseId: Int,
): List<CourseModuleEntity> {
    val result = mutableListOf<CourseModuleEntity>()
    for (section in this) {
        section.modules.forEachIndexed { index, module ->
            val contents = module.contents.orEmpty().map {
                ModuleContentJson(
                    type = it.type,
                    fileName = it.fileName,
                    fileUrl = it.fileUrl,
                    mimeType = it.mimeType,
                    sizeBytes = it.fileSize,
                    timeModifiedMs = it.timeModifiedTimestamp?.toMillisOrNull(),
                )
            }
            val dueAtMs = module.dates.orEmpty()
                .firstOrNull { it.label?.contains("due", ignoreCase = true) == true }
                ?.timestamp?.toMillisOrNull()
            result += CourseModuleEntity(
                accountId = accountId.value,
                courseId = courseId,
                cmId = module.id,
                sectionId = section.id,
                sortOrder = index,
                instanceId = module.instanceId,
                name = module.name,
                modName = module.moduleName,
                description = module.description,
                url = module.url,
                iconUrl = module.moduleIconUrl,
                visible = module.visible != 0 && module.userVisible != false,
                dueAtMs = dueAtMs,
                contentsJson = if (contents.isEmpty()) null else ElearningJson.encodeToString(contents),
            )
        }
    }
    return result
}

internal fun CourseSectionEntity.toDomain(modules: List<CourseModuleEntity>): CourseSection {
    val sectionModules = modules.filter { it.sectionId == sectionId }
        .sortedBy { it.sortOrder }
        .map { it.toDomain() }
    return CourseSection(
        id = sectionId,
        sectionNumber = sectionNumber,
        name = name,
        summary = summary,
        visible = visible,
        modules = sectionModules,
    )
}

internal fun CourseModuleEntity.toDomain(): CourseModule {
    val contents = contentsJson?.let { json ->
        runCatching {
            ElearningJson.decodeFromString(ListSerializer(ModuleContentJson.serializer()), json)
        }.getOrNull()
    }.orEmpty().map {
        ModuleContent(
            type = it.type,
            fileName = it.fileName,
            fileUrl = it.fileUrl,
            mimeType = it.mimeType,
            sizeBytes = it.sizeBytes,
            timeModified = it.timeModifiedMs?.let(Instant::ofEpochMilli),
        )
    }
    return CourseModule(
        cmId = cmId,
        instanceId = instanceId,
        name = name,
        type = ModuleType.fromRaw(modName),
        description = description,
        url = url,
        iconUrl = iconUrl,
        visible = visible,
        contents = contents,
        completion = null,
        dueAt = dueAtMs?.let(Instant::ofEpochMilli),
    )
}

internal fun ElearningCoursePublicInfo.toSyllabusEntity(
    accountId: AccountId,
    courseId: Int,
): CourseSyllabusEntity? {
    val primary = syllabus.firstOrNull() ?: return null
    val info = buildSyllabusInfo(primary.fields, metadata)
    val blob = SyllabusBlobJson(
        fields = primary.fields.map { SyllabusFieldJson(it.title, it.htmlContent) },
        info = info,
    )
    return CourseSyllabusEntity(
        accountId = accountId.value,
        courseId = courseId,
        language = primary.language,
        exportPdfUrl = primary.exportPdfUrl,
        fieldsJson = ElearningJson.encodeToString(blob),
    )
}

internal fun CourseSyllabusEntity.toDomain(): CourseSyllabusPointer {
    val blob = decodeSyllabusBlob(fieldsJson)
    val fields = blob.fields.map { CourseSyllabusPointer.SyllabusField(it.title, it.htmlContent) }
    return CourseSyllabusPointer(
        language = language,
        exportPdfUrl = exportPdfUrl,
        fields = fields,
        info = blob.info.toDomain(),
    )
}

// Backwards-compatible decode: new shape is { "fields": [...], "info": {...} };
// pre-refactor rows stored a bare `[{title,htmlContent}, ...]`. Try the new
// shape first, fall back to the legacy list (info defaults to empty until the
// next refresh re-writes the row).
private fun decodeSyllabusBlob(raw: String): SyllabusBlobJson {
    runCatching {
        return ElearningJson.decodeFromString(SyllabusBlobJson.serializer(), raw)
    }
    val legacyFields = runCatching {
        ElearningJson.decodeFromString(
            ListSerializer(SyllabusFieldJson.serializer()),
            raw,
        )
    }.getOrDefault(emptyList())
    return SyllabusBlobJson(fields = legacyFields, info = SyllabusInfoJson())
}

private fun SyllabusInfoJson.toDomain(): SyllabusInfo = SyllabusInfo(
    type = type,
    credits = credits,
    hours = hours,
    language = language,
    level = level?.let { runCatching { CourseLevel.valueOf(it) }.getOrNull() },
    semester = semester?.let { runCatching { Semester.valueOf(it) }.getOrNull() },
    disciplinarySector = disciplinarySector,
    objectives = objectives,
    summary = summary,
    extendedProgramme = extendedProgramme.map { ProgrammeSection(title = it.title, items = it.items) },
    prerequisites = prerequisites,
    teachingMethod = teachingMethod,
    referenceMaterial = referenceMaterial,
    assessment = assessment,
    officeHours = officeHours,
)

// Pull structured info out of (a) the Moodle metadata sidebar and (b) Jsoup-parsed
// HTML of each syllabus field. Field titles are server-localized Italian/English,
// so the dispatch matches on lowercase substrings — the localizations the Moodle
// site actually uses today are exhaustive for the courses we care about.
private fun buildSyllabusInfo(
    fields: List<it.attendance100.mybicocca.data.remote.elearning.dto.ElearningCourseSyllabusField>,
    metadata: it.attendance100.mybicocca.data.remote.elearning.dto.ElearningCoursePublicMetadata,
): SyllabusInfoJson {
    val byKey = fields.associate { field ->
        field.title.trim().lowercase() to Jsoup.parseBodyFragment(field.htmlContent).body()
    }

    fun pick(vararg keys: String): Element? {
        for (key in keys) {
            val match = byKey.entries.firstOrNull { it.key.contains(key) }?.value
            if (match != null && match.text().isNotBlank()) return match
        }
        return null
    }

    fun pickText(vararg keys: String): String? =
        pick(*keys)?.textWithNewlines()?.takeIf { it.isNotBlank() }

    val programme = pick("programma esteso", "detailed program", "programma del corso", "programma")
        ?.let { parseProgrammeSections(it) }
        .orEmpty()

    return SyllabusInfoJson(
        type = metadata.activityType?.takeIf { it.isNotBlank() }
            ?: pickText("tipologia di attività", "tipo di insegnamento")?.let { firstWord(it) },
        credits = metadata.cfu,
        hours = metadata.hours,
        language = metadata.language?.takeIf { it.isNotBlank() }
            ?: pickText("lingua", "language"),
        level = matchCourseLevel(metadata.degreeType ?: pickText("livello", "tipologia di corso"))?.name,
        semester = matchSemester(metadata.period ?: pickText("periodo", "semestre", "ciclo"))?.name,
        disciplinarySector = metadata.disciplinarySector,
        objectives = pickText("obiettivi", "aims"),
        summary = pickText("contenuti sintetici", "contents", "syllabus"),
        extendedProgramme = programme,
        prerequisites = pickText("prerequisiti", "prerequisites"),
        teachingMethod = pickText("modalità didattica", "teaching form", "metodi didattici"),
        referenceMaterial = pickText(
            "materiale didattico",
            "textbook and teaching resource",
            "testi di riferimento",
            "bibliografia",
        ),
        assessment = pickText(
            "verifica del profitto",
            "assessment method",
            "modalità d'esame",
            "modalità di esame",
        ),
        officeHours = pickText("orario di ricevimento", "office hours", "ricevimento"),
    )
}

// "Programma esteso" comes in two common shapes:
//   1. <p><strong>Header</strong><br>item<br>item</p><p><strong>Header2</strong>...
//   2. <ol><li><p>Header</p></li></ol>  with items in sibling <ul><li> blocks
//
// Strategy: walk children top-to-bottom, treat <strong>/<h*> openings as section
// headers, and accumulate any <li> / <br>-separated text as items.
private fun parseProgrammeSections(body: Element): List<ProgrammeSectionJson> {
    val sections = mutableListOf<ProgrammeSectionJson>()
    var currentTitle: String? = null
    val currentItems = mutableListOf<String>()
    fun flush() {
        val title = currentTitle ?: return
        if (currentItems.isNotEmpty() || title.isNotBlank()) {
            sections += ProgrammeSectionJson(title = title, items = currentItems.toList())
        }
        currentItems.clear()
    }

    body.children().forEach { node ->
        when (node.tagName().lowercase()) {
            "p" -> {
                val strong = node.selectFirst("strong, b")
                if (strong != null && strong.text().isNotBlank()) {
                    flush()
                    currentTitle = strong.text().trim()
                    val rest = node.html()
                        .substringAfter("</strong>", "")
                        .substringAfter("</b>", "")
                    if (rest.isNotBlank()) {
                        Jsoup.parseBodyFragment(rest).body().splitOnBreaks()
                            .filter { it.isNotBlank() }
                            .forEach { currentItems += it }
                    }
                } else {
                    node.splitOnBreaks().filter { it.isNotBlank() }.forEach { currentItems += it }
                }
            }
            "h1", "h2", "h3", "h4", "h5", "h6" -> {
                flush()
                currentTitle = node.text().trim()
            }
            "ul", "ol" -> {
                node.select("> li").forEach { li ->
                    val text = li.text().trim()
                    if (text.isNotBlank()) currentItems += text
                }
            }
            else -> Unit
        }
    }
    flush()
    return sections.filter { it.items.isNotEmpty() }
}

// Splits an element's content on `<br>` tags into a list of trimmed text lines.
private fun Element.splitOnBreaks(): List<String> {
    val pieces = html().split(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE))
    return pieces.map { Jsoup.parseBodyFragment(it).body().text().trim() }
}

// Jsoup's text() collapses every line break, gluing the lines of a syllabus field together;
// wholeText() instead keeps source newlines and renders <br> as "\n". Block tags contribute
// no separator to it though, so close them off with explicit newlines first. Works on a
// clone - the parsed elements are shared with the programme parser and must stay untouched.
private fun Element.textWithNewlines(): String {
    val copy = clone()
    copy.select("p, div, li, tr, h1, h2, h3, h4, h5, h6").forEach { it.appendText("\n") }
    return copy.wholeText()
        .lines()
        .joinToString("\n") { it.trim() }
        .replace(BlankLineRun, "\n\n")
        .trim()
}

private val BlankLineRun = Regex("\n{3,}")

private fun firstWord(text: String): String? =
    text.split(' ', '\n', '\t', ',', '.', ':').firstOrNull { it.isNotBlank() }

private fun matchCourseLevel(text: String?): CourseLevel? {
    if (text.isNullOrBlank()) return null
    val lower = text.lowercase()
    return when {
        "magistrale" in lower || "specialistica" in lower || "master" in lower -> CourseLevel.Master
        "dottor" in lower || "doctorate" in lower || "phd" in lower -> CourseLevel.Doctorate
        "triennale" in lower || "bachelor" in lower || "laurea" in lower -> CourseLevel.Bachelor
        else -> null
    }
}

private fun matchSemester(text: String?): Semester? {
    if (text.isNullOrBlank()) return null
    val lower = text.lowercase()
    return when {
        "primo" in lower || "1°" in lower || "i sem" in lower || "first" in lower -> Semester.First
        "secondo" in lower || "2°" in lower || "ii sem" in lower || "second" in lower -> Semester.Second
        "annuale" in lower || "annual" in lower || "full year" in lower -> Semester.FullYear
        else -> null
    }
}

internal fun ElearningCoursePublicInfo.toStaffEntities(
    accountId: AccountId,
    courseId: Int,
): List<CourseStaffEntity> {
    var rowIndex = 0
    return staff.flatMap { group ->
        group.members.map { member ->
            CourseStaffEntity(
                accountId = accountId.value,
                courseId = courseId,
                rowIndex = rowIndex++,
                userId = member.userId,
                fullName = member.name,
                roleRaw = group.role,
                initials = member.initials,
                email = member.email,
                profileUrl = member.profileUrl,
            )
        }
    }
}

internal fun CourseStaffEntity.toDomain(): CourseStaffMember =
    CourseStaffMember(
        userId = userId,
        fullName = fullName,
        role = CourseStaffRole.fromRaw(roleRaw),
        initials = initials,
        email = email,
        profileUrl = profileUrl,
    )

internal fun ElearningActivityCompletionStatus.toEntity(
    accountId: AccountId,
    courseId: Int,
): ActivityCompletionEntity {
    val isCompleted = state == 2 || state == 3
    val tracking = tracking ?: 0
    return ActivityCompletionEntity(
        accountId = accountId.value,
        courseId = courseId,
        cmId = courseModuleId,
        isCompleted = isCompleted,
        completedAtMs = timeCompleted?.toMillisOrNull(),
        isManual = tracking == 1,
        isAutomatic = tracking == 2,
        isTracked = state != 0,
    )
}

internal fun ActivityCompletionEntity.toDomain(): CompletionState =
    CompletionState(
        cmId = cmId,
        isCompleted = isCompleted,
        completedAt = completedAtMs?.let(Instant::ofEpochMilli),
        isManual = isManual,
        isAutomatic = isAutomatic,
        isTracked = isTracked,
    )

private fun Long?.toMillisOrNull(): Long? = this?.takeIf { it > 0 }?.let { it * 1000L }

private fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
