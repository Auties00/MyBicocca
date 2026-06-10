package it.attendance100.mybicocca.data.mapper.elearning

import it.attendance100.mybicocca.data.local.elearning.course.ActivityCompletionEntity
import it.attendance100.mybicocca.data.local.elearning.course.CourseModuleEntity
import it.attendance100.mybicocca.data.local.elearning.course.CourseSectionEntity
import it.attendance100.mybicocca.data.local.elearning.course.CourseStaffEntity
import it.attendance100.mybicocca.data.local.elearning.course.CourseSyllabusEntity
import it.attendance100.mybicocca.data.local.elearning.course.EnrolledCourseEntity
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningJson
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningActivityCompletionStatus
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningCourseModule
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
import it.attendance100.mybicocca.domain.model.elearning.course.ModuleContent
import it.attendance100.mybicocca.domain.model.elearning.course.ModuleDate
import it.attendance100.mybicocca.domain.model.elearning.course.ModuleType
import it.attendance100.mybicocca.domain.model.elearning.course.ProgrammeSection
import it.attendance100.mybicocca.domain.model.elearning.course.Semester
import it.attendance100.mybicocca.domain.model.elearning.course.SyllabusInfo
import it.attendance100.mybicocca.domain.model.elearning.deadline.Deadline
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale

/**
 * JSON envelope for one bundled file/link of a module, stored as a list in the module
 * entity's contents_json column. Timestamps are epoch milliseconds.
 */
@Serializable
internal data class ModuleContentJson(
    val type: String? = null,
    val fileName: String? = null,
    val filePath: String? = null,
    val fileUrl: String? = null,
    val mimeType: String? = null,
    val sizeBytes: Long? = null,
    val timeModifiedMs: Long? = null,
)

/**
 * JSON envelope for one dated event of a module, stored as a list in the module
 * entity's dates_json column. `timestampMs` is epoch milliseconds; `dataId` is
 * Moodle's stable machine key for the date kind.
 */
@Serializable
internal data class ModuleDateJson(
    val label: String? = null,
    val timestampMs: Long,
    val dataId: String? = null,
)

/** JSON envelope for one titled HTML section of the course sheet. */
@Serializable
internal data class SyllabusFieldJson(val title: String, val htmlContent: String)

/** JSON envelope for one parsed extended-programme topic (title plus bullet items). */
@Serializable
internal data class ProgrammeSectionJson(
    val title: String,
    val items: List<String> = emptyList(),
)

/**
 * JSON envelope for the structured facts extracted from the course sheet. `level` and
 * `semester` store domain enum names; every field defaults so blobs written before a
 * field existed still decode.
 */
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
    val sustainableDevelopmentGoals: String? = null,
)

/**
 * JSON envelope persisted in the syllabus entity's fields_json column: the sheet's raw
 * fields plus the structured info extracted from them.
 */
@Serializable
internal data class SyllabusBlobJson(
    val fields: List<SyllabusFieldJson> = emptyList(),
    val info: SyllabusInfoJson = SyllabusInfoJson(),
)

/**
 * Maps one course of the Moodle user-courses web service into its cache row. Strips
 * {mlang} markup from the names, blanks an empty idNumber to null, collapses nullable
 * server booleans to false, normalizes epoch-second timestamps to milliseconds, and
 * records the list position as the sort order.
 */
internal fun ElearningEnrolledCourse.toEntity(accountId: AccountId, sortOrder: Int): EnrolledCourseEntity =
    EnrolledCourseEntity(
        accountId = accountId.value,
        courseId = id,
        shortName = shortName.stripMlang(),
        fullName = fullName.stripMlang(),
        displayName = (displayName ?: fullName).stripMlang(),
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

/**
 * Maps a cached enrolled-course row to the domain model, attaching the course's
 * deadlines from the deadline cache and converting epoch-millisecond columns to
 * dates/instants.
 */
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

/**
 * Maps one section of the Moodle course-contents web service into its cache row,
 * stripping {mlang} markup from the name and reading `visible` as the 0/1 flag Moodle
 * ships.
 */
internal fun ElearningCourseSection.toEntity(accountId: AccountId, courseId: Int): CourseSectionEntity =
    CourseSectionEntity(
        accountId = accountId.value,
        courseId = courseId,
        sectionId = id,
        sectionNumber = sectionNumber ?: 0,
        name = name.stripMlang(),
        summary = summary,
        visible = visible != 0,
        component = component,
        itemId = itemId,
    )

/**
 * Flattens the sections of the Moodle course-contents web service into module cache
 * rows, recording each module's position within its section as the sort order. Bundled
 * contents and dated events are packed into the JSON columns with epoch-second
 * timestamps normalized to milliseconds; the `afterlink` HTML is reduced to plain
 * text; subsection placeholders get their linked section id resolved from customdata.
 * The `visible` column stores teacher-visibility only — a module gated for this user
 * (uservisible false) is kept and rendered locked, not dropped, with the gate recorded
 * in the `accessible` column.
 */
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
                    filePath = it.filePath,
                    fileUrl = it.fileUrl,
                    mimeType = it.mimeType,
                    sizeBytes = it.fileSize,
                    timeModifiedMs = it.timeModifiedTimestamp?.toMillisOrNull(),
                )
            }
            val dates = module.dates.orEmpty().mapNotNull {
                val ms = it.timestamp?.toMillisOrNull() ?: return@mapNotNull null
                ModuleDateJson(label = it.label, timestampMs = ms, dataId = it.dataId)
            }
            result += CourseModuleEntity(
                accountId = accountId.value,
                courseId = courseId,
                cmId = module.id,
                sectionId = section.id,
                sortOrder = index,
                instanceId = module.instanceId,
                name = module.name.stripMlang(),
                modName = module.moduleName,
                typeLabel = module.modulePluralName?.takeIf { it.isNotBlank() },
                description = module.description,
                url = module.url,
                iconUrl = module.moduleIconUrl,
                visible = module.visible != 0,
                accessible = module.userVisible != false,
                availabilityInfo = module.availabilityInfo?.takeIf { it.isNotBlank() },
                onCoursePage = module.visibleOnCoursePage != 0,
                indent = module.indent ?: 0,
                afterLink = module.afterLinkHtml.htmlToPlainTextOrNull(),
                linkedSectionId = module.linkedSectionId(),
                datesJson = if (dates.isEmpty()) null else ElearningJson.encodeToString(dates),
                contentsJson = if (contents.isEmpty()) null else ElearningJson.encodeToString(contents),
            )
        }
    }
    return result
}

/**
 * Reads the content-section id a mod_subsection placeholder module points at via
 * customdata, e.g. {"sectionid":"333817"}. customData is a JSON string (sometimes the
 * literal "" when empty), so any parse failure yields null.
 */
private fun ElearningCourseModule.linkedSectionId(): Int? {
    val raw = customData ?: return null
    return runCatching {
        val element = ElearningJson.parseToJsonElement(raw)
        (element as? JsonObject)?.get("sectionid")?.jsonPrimitive?.content?.toIntOrNull()
    }.getOrNull()
}

private fun String?.htmlToPlainTextOrNull(): String? =
    this?.takeIf { it.isNotBlank() }
        ?.let { Jsoup.parseBodyFragment(it).text().trim() }
        ?.takeIf { it.isNotBlank() }

/**
 * Maps a cached section row to the domain model, picking its own modules out of the
 * course's module rows by section id and restoring their sort order.
 */
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
        component = component,
        itemId = itemId,
        modules = sectionModules,
    )
}

/**
 * Maps a cached module row to the domain model, decoding the JSON contents/dates
 * columns (defensively, dropping the blob on any decode failure) and resolving the
 * module type from the stored plugin name. Completion is attached separately from the
 * completion cache, so it maps as null here.
 */
internal fun CourseModuleEntity.toDomain(): CourseModule {
    val contents = contentsJson?.let { json ->
        runCatching {
            ElearningJson.decodeFromString(ListSerializer(ModuleContentJson.serializer()), json)
        }.getOrNull()
    }.orEmpty().map {
        ModuleContent(
            type = it.type,
            fileName = it.fileName,
            filePath = it.filePath,
            fileUrl = it.fileUrl,
            mimeType = it.mimeType,
            sizeBytes = it.sizeBytes,
            timeModified = it.timeModifiedMs?.let(Instant::ofEpochMilli),
        )
    }
    val dates = datesJson?.let { json ->
        runCatching {
            ElearningJson.decodeFromString(ListSerializer(ModuleDateJson.serializer()), json)
        }.getOrNull()
    }.orEmpty().map {
        ModuleDate(
            label = it.label,
            instant = Instant.ofEpochMilli(it.timestampMs),
            dataId = it.dataId,
        )
    }
    return CourseModule(
        cmId = cmId,
        instanceId = instanceId,
        name = name,
        type = ModuleType.fromRaw(modName),
        typeLabel = typeLabel,
        description = description,
        url = url,
        iconUrl = iconUrl,
        visible = visible,
        accessible = accessible,
        availabilityInfo = availabilityInfo,
        onCoursePage = onCoursePage,
        indent = indent,
        afterLink = afterLink,
        linkedSectionId = linkedSectionId,
        contents = contents,
        completion = null,
        dates = dates,
    )
}

/**
 * Maps the course sheet of the public course page into its cache row, or null when
 * the page exposes no sheet. Sheet tabs are one-per-language ("it", "en"); the tab
 * matching the app language is preferred, falling back to English, then to whatever
 * the page lists first. The chosen tab's fields and the structured info extracted from
 * them are packed into the JSON blob column.
 */
internal fun ElearningCoursePublicInfo.toSyllabusEntity(
    accountId: AccountId,
    courseId: Int,
): CourseSyllabusEntity? {
    val appLanguage = Locale.getDefault().language.lowercase()
    val primary = syllabus.firstOrNull { it.language.equals(appLanguage, ignoreCase = true) }
        ?: syllabus.firstOrNull { it.language.equals("en", ignoreCase = true) }
        ?: syllabus.firstOrNull()
        ?: return null
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

/** Maps a cached syllabus row to the domain model by unpacking its JSON blob. */
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

/**
 * Decodes the syllabus blob, tolerating both layouts a row can hold: the composite
 * { "fields": [...], "info": {...} } object and a bare `[{title,htmlContent}, ...]`
 * field list. A bare-list row decodes with empty info until the next refresh
 * re-writes it in the composite layout.
 */
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

/**
 * Maps the persisted info blob to the domain model, resolving the stored enum names
 * leniently so an unknown name reads as absent rather than failing the row.
 */
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
    sustainableDevelopmentGoals = sustainableDevelopmentGoals,
)

/**
 * Pulls structured info out of (a) the Moodle metadata sidebar and (b) Jsoup-parsed
 * HTML of each syllabus field. Field titles are server-localized Italian/English, so
 * the dispatch matches on lowercase substrings covering the localizations the Moodle
 * site uses. The metadata sidebar "Periodo" and the "Periodo di erogazione
 * dell'insegnamento" field each can carry detail the other lacks, so the semester is
 * matched on both combined.
 */
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
        semester = matchSemester(
            listOfNotNull(metadata.period, pickText("periodo", "semestre", "ciclo", "semester"))
                .joinToString(" ")
                .takeIf { it.isNotBlank() },
        )?.name,
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
            "verifica dell'apprendimento",
        ),
        officeHours = pickText("orario di ricevimento", "office hours", "ricevimento"),
        sustainableDevelopmentGoals = pickText("sustainable development"),
    )
}

/**
 * Parses the "programma esteso" HTML into titled topic sections. The shapes observed
 * across the live course sheets:
 *   1. `<p><strong>Header</strong><br>item<br>item</p>` blocks
 *   2. a short plain `<p>` (or a one-item `<ol start=N>`) acting as the header of the
 *      `<ul>`/`<ol>` that immediately follows it
 *   3. bare `<ol>`/`<ul>`/`<p>` blocks with no header at all
 *   4. `<li>` wrapping a topic title plus its own nested sub-list
 *   5. run-in `<em>label.</em>` headers, literal markdown `**header**` lines typed as
 *      plain text, and hard-wrapped prose where `<br>` is a soft line wrap, not an
 *      item separator
 *
 * Strategy: walk children top-to-bottom, promote everything header-like to a section
 * title (title-only sections are emitted so lone headings are never dropped),
 * accumulate `<li>` / `<br>`-separated text as items keeping `<ol>` ordinals, and glue
 * hard-wrapped continuation lines back onto the previous item. Specifics folded into
 * the walk:
 * - Bold opening a paragraph is a header; mid-paragraph bold is emphasis.
 * - `<br>`-split lines drop hand-typed bullet markers (the app styles rows itself); a
 *   lowercase-starting line after an unterminated one is a soft wrap and is re-joined.
 * - Hand-typed dash outlines mark sub-items with "- " lines; the bare short lines
 *   between them are the topic headers (e.g. "3 Metodi:" over "- metodi di istanza").
 * - In still-headerless documents, lists whose items are long "Label: details" or
 *   multi-sentence topic runs would read as one wall-of-text pill each, so every such
 *   item becomes its own section; a list inside a titled section is its body.
 * - A numbered topic typed as a one-item `<ol start=N>` right before its bullet list
 *   becomes that list's header, keeping its ordinal.
 */
private fun parseProgrammeSections(body: Element): List<ProgrammeSectionJson> {
    val sections = mutableListOf<ProgrammeSectionJson>()
    var currentTitle: String? = null
    var titlePending = false
    val currentItems = mutableListOf<String>()

    fun flush() {
        if (currentItems.isNotEmpty() || titlePending) {
            sections += ProgrammeSectionJson(title = currentTitle.orEmpty(), items = currentItems.toList())
        }
        currentItems.clear()
        titlePending = false
    }

    fun startSection(title: String) {
        flush()
        currentTitle = title.trim().trimEnd('.', ':', ';').trim()
        titlePending = true
    }

    fun addLine(raw: String) {
        val line = raw.trim()
        if (line.isEmpty()) return
        val continuation = line.first().isLowerCase() &&
            currentItems.lastOrNull()?.last()?.let { it !in SentenceTerminals } == true
        val text = line.replace(LeadingBulletRegex, "").trim()
        if (text.isEmpty()) return
        if (continuation) {
            currentItems[currentItems.lastIndex] = "${currentItems.last()} $text"
        } else {
            currentItems += text
        }
    }

    fun nextIsList(node: Element): Boolean =
        node.nextElementSibling()?.tagName()?.lowercase() in ListTags

    val dashOutline = body.children()
        .filter { it.tagName().equals("p", ignoreCase = true) }
        .flatMap { it.splitOnBreaks() }
        .count { LeadingBulletRegex.containsMatchIn(it.trim()) } >= 2

    fun appendListItems(list: Element) {
        val ordered = list.tagName().equals("ol", ignoreCase = true)
        val start = list.attr("start").toIntOrNull() ?: 1
        list.select("> li").forEachIndexed { index, li ->
            val text = li.text().trim()
            if (text.isNotBlank()) {
                currentItems += if (ordered) "${start + index}. $text" else text
            }
        }
    }

    body.children().forEach { node ->
        when (node.tagName().lowercase()) {
            "p" -> {
                val strong = node.selectFirst("strong, b")
                val emphasis = node.selectFirst("em, i")
                when {
                    strong != null && strong.text().isNotBlank() && node.opensWith(strong) -> {
                        startSection(strong.text())
                        val rest = node.clone()
                        rest.selectFirst("strong, b")?.remove()
                        addRunInBody(rest) { addLine(it) }
                    }
                    emphasis != null && node.opensWith(emphasis) && isRunInLabel(emphasis, node) -> {
                        startSection(emphasis.text())
                        val rest = node.clone()
                        rest.selectFirst("em, i")?.remove()
                        addRunInBody(rest) { addLine(it) }
                    }
                    else -> {
                        val pieces = node.splitOnBreaks().filter { it.isNotBlank() }
                        val first = pieces.firstOrNull()?.trim()
                        val markdown = first?.let { MarkdownHeaderRegex.matchEntire(it) }
                        when {
                            markdown != null -> {
                                startSection(markdown.groupValues[1])
                                pieces.drop(1).forEach { addLine(it) }
                            }
                            dashOutline -> pieces.forEach { piece ->
                                val line = piece.trim()
                                if (!LeadingBulletRegex.containsMatchIn(line) && line.length <= 80) {
                                    startSection(line)
                                } else {
                                    addLine(line)
                                }
                            }
                            first != null && pieces.size == 1 && nextIsList(node) && isHeadingLike(first) ->
                                startSection(first)
                            else -> pieces.forEach { addLine(it) }
                        }
                    }
                }
            }
            "h1", "h2", "h3", "h4", "h5", "h6" -> startSection(node.text())
            "ul", "ol" -> {
                val items = node.select("> li")
                val ordered = node.tagName().equals("ol", ignoreCase = true)
                val start = node.attr("start").toIntOrNull() ?: 1
                val loneLi = items.singleOrNull()
                val plainLis = items.filter { it.select("> ul, > ol").isEmpty() }
                val topicRun = currentTitle == null && plainLis.size >= 2 &&
                    plainLis.sumOf { it.text().trim().length } / plainLis.size > 120
                if (ordered && loneLi != null && nextIsList(node) &&
                    loneLi.select("> ul, > ol").isEmpty() && loneLi.text().isNotBlank()
                ) {
                    startSection("$start. ${loneLi.text().trim()}")
                } else {
                    items.forEachIndexed { index, li ->
                        val nested = li.select("> ul, > ol")
                        if (nested.isNotEmpty()) {
                            val own = li.clone().also { it.select("ul, ol").remove() }.text().trim()
                            startSection(if (ordered) "${start + index}. $own" else own)
                            nested.forEach { appendListItems(it) }
                        } else {
                            val text = li.text().trim()
                            if (text.isBlank()) return@forEachIndexed
                            if (topicRun) {
                                val ordinal = if (ordered) "${start + index}. " else ""
                                val colon = text.indexOf(':')
                                val sentences = text.split(SentenceBoundaryRegex)
                                when {
                                    colon in 1..60 -> {
                                        startSection(ordinal + text.take(colon))
                                        topicItems(text.substring(colon + 1)).forEach { currentItems += it }
                                    }
                                    sentences.size >= 2 && sentences.first().length <= 80 -> {
                                        startSection(ordinal + sentences.first())
                                        topicItems(text.substring(sentences.first().length)).forEach { currentItems += it }
                                    }
                                    text.length <= 80 -> startSection(ordinal + text)
                                    else -> {
                                        startSection("")
                                        topicItems(text).forEach { currentItems += it }
                                    }
                                }
                            } else {
                                currentItems += if (ordered) "${start + index}. $text" else text
                            }
                        }
                    }
                }
            }
            else -> Unit
        }
    }
    flush()
    return sections
}

private val ListTags = setOf("ul", "ol")
private val SentenceTerminals = setOf('.', '!', '?', ':', ';')
private val LeadingBulletRegex = Regex("^[-–—•·]\\s*")
private val MarkdownHeaderRegex = Regex("""^\*\*\s*([^*]+?)\s*(?:\*\*)?$""")

/** True when the element is the first visible content of the paragraph. */
private fun Element.opensWith(child: Element): Boolean =
    childNodes().firstOrNull { it !is TextNode || it.text().isNotBlank() } === child

/**
 * Adds the body of a run-in-header paragraph: `<br>`-separated lines stay one item per
 * line, but a single prose run is a sentence-separated topic list ("Matrici.
 * Determinanti. ...") that would otherwise render as one wall-of-text row, so it is
 * split at sentence boundaries.
 */
private inline fun addRunInBody(rest: Element, addLine: (String) -> Unit) {
    val pieces = rest.splitOnBreaks().filter { it.isNotBlank() }
    if (pieces.size == 1) {
        pieces[0].split(SentenceBoundaryRegex).forEach { addLine(it) }
    } else {
        pieces.forEach { addLine(it) }
    }
}

private val SentenceBoundaryRegex = Regex("(?<=[.!?])\\s+(?=[A-ZÀ-ÖØ-Þ0-9])")

/**
 * Splits the detail text of a topic-run `<li>` into items: one per sentence, with
 * comma enumerations ("unione, intersezione, complementazione, ...") fanned out into
 * separate items.
 */
private fun topicItems(detail: String): List<String> =
    detail.split(SentenceBoundaryRegex)
        .flatMap { sentence ->
            val parts = splitTopLevelCommas(sentence)
            if (parts.size >= 3) parts else listOf(sentence)
        }
        .map { it.trim() }
        .filter { it.isNotEmpty() }

/** Splits on commas/semicolons outside any bracket pair, so "(sintassi, semantica)" stays whole. */
private fun splitTopLevelCommas(text: String): List<String> {
    val pieces = mutableListOf<String>()
    val current = StringBuilder()
    var depth = 0
    text.forEach { c ->
        when {
            c == '(' || c == '[' -> { depth++; current.append(c) }
            c == ')' || c == ']' -> { depth = (depth - 1).coerceAtLeast(0); current.append(c) }
            (c == ',' || c == ';') && depth == 0 -> { pieces += current.toString(); current.clear() }
            else -> current.append(c)
        }
    }
    pieces += current.toString()
    return pieces
}

/**
 * Recognizes run-in headers like `<em>Analisi vettoriale.</em>` prose...: short,
 * punctuation-closed, and with actual content after the label — a fully-italic
 * paragraph stays an item.
 */
private fun isRunInLabel(emphasis: Element, paragraph: Element): Boolean {
    val label = emphasis.text().trim()
    return label.length in 1..60 &&
        (label.endsWith(".") || label.endsWith(":")) &&
        paragraph.text().trim().length > label.length
}

/** Sentence-terminated or lowercase-starting paragraphs are prose/continuations, not titles. */
private fun isHeadingLike(text: String): Boolean =
    text.length <= 80 && text.last() !in HeadingForbiddenEndings && !text.first().isLowerCase()

private val HeadingForbiddenEndings = setOf('.', ',', ';')

/** Splits an element's content on `<br>` tags into a list of trimmed text lines. */
private fun Element.splitOnBreaks(): List<String> {
    val pieces = html().split(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE))
    return pieces.map { Jsoup.parseBodyFragment(it).body().text().trim() }
}

/**
 * Extracts an element's text with line structure intact. Jsoup's text() collapses
 * every line break, gluing the lines of a syllabus field together; wholeText() keeps
 * source newlines and renders `<br>` as "\n", but block tags contribute no separator
 * to it, so they are closed off with explicit newlines first. Works on a clone — the
 * parsed elements are shared with the programme parser and must stay untouched.
 */
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

/**
 * Flattens the role-grouped staff listing of the public course page into staff cache
 * rows, numbering rows across the groups so the page order survives as the row index.
 */
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

/** Maps a cached staff row to the domain model, resolving the role from its raw label. */
internal fun CourseStaffEntity.toDomain(): CourseStaffMember =
    CourseStaffMember(
        userId = userId,
        fullName = fullName,
        role = CourseStaffRole.fromRaw(roleRaw),
        initials = initials,
        email = email,
        profileUrl = profileUrl,
    )

/**
 * Maps one status of the Moodle activity-completion web service into its cache row.
 * Moodle's `state` codes collapse to a boolean (2 = done, 3 = done-with-pass; 0 also
 * means untracked) and `tracking` to the manual (1) / automatic (2) flags.
 */
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

/** Maps a cached completion row to the domain model. */
internal fun ActivityCompletionEntity.toDomain(): CompletionState =
    CompletionState(
        cmId = cmId,
        isCompleted = isCompleted,
        completedAt = completedAtMs?.let(Instant::ofEpochMilli),
        isManual = isManual,
        isAutomatic = isAutomatic,
        isTracked = isTracked,
    )

/**
 * Matches one multilang2 block, capturing its comma-separated language list and its
 * text. The closing braces must stay escaped — Android's ICU regex engine rejects the
 * bare `}` the JVM tolerates, and the failure crashes this class's static init.
 */
private val MlangBlock = Regex(
    """\{\s*mlang\s+([a-z0-9_-]+(?:\s*,\s*[a-z0-9_-]+)*)\s*\}(.*?)\{\s*mlang\s*\}""",
    setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
)

/**
 * Resolves multilang2 {mlang} markup in course/module names client-side. The markup
 * arrives unresolved even with moodlewssettingfilter=true: names go through
 * format_string (headings), which the site's multilang filter doesn't cover, and no
 * WS parameter can override that. The official Moodle app has the same problem and
 * ships a client-side filter (moodleapp src/addons/filter/multilang2); this mirrors
 * its semantics — comma-separated language lists per block, blocks matching the
 * wanted language kept, all others removed, text outside blocks preserved. A block
 * tagged with a parent language (e.g. "en") also serves variants ("en-us"). Wanted
 * language order: app language, the dedicated "other" block, English, then the first
 * block's language as a last resort so a name never collapses to nothing.
 */
internal fun String.stripMlang(): String {
    if (!contains("mlang", ignoreCase = true)) return this
    val firstBlock = MlangBlock.find(this) ?: return this
    val appLanguage = normalizeMlangCode(Locale.getDefault().language)
    val firstBlockLanguage = normalizeMlangCode(firstBlock.groupValues[1].substringBefore(','))
    for (wanted in listOf(appLanguage, "other", "en", firstBlockLanguage).distinct()) {
        var matched = false
        val result = MlangBlock.replace(this) { block ->
            val languages = block.groupValues[1].split(',').map(::normalizeMlangCode)
            val blockMatches = languages.any { it == wanted || wanted.startsWith("$it-") }
            if (blockMatches) matched = true
            if (blockMatches) block.groupValues[2] else ""
        }
        if (matched) return result.trim()
    }
    return this
}

private fun normalizeMlangCode(code: String): String = code.trim().lowercase().replace('_', '-')

/** Converts a Moodle epoch-second timestamp to milliseconds, reading 0 as absent. */
private fun Long?.toMillisOrNull(): Long? = this?.takeIf { it > 0 }?.let { it * 1000L }

private fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
