package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component

import androidx.compose.animation.animateColor
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Poll
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.elearning.course.CompletionState
import it.attendance100.mybicocca.domain.model.elearning.course.CourseModule
import it.attendance100.mybicocca.domain.model.elearning.course.CourseSection
import it.attendance100.mybicocca.domain.model.elearning.course.ModuleType
import it.attendance100.mybicocca.domain.model.elearning.course.kalvidresCmIdOrNull
import it.attendance100.mybicocca.domain.model.elearning.video.VideoProgress
import it.attendance100.mybicocca.ui.component.shape.OrganicShapes
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.ext.contentBlocks
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.ext.stripCopySuffix
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.ext.summaryPlainText
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state.ContentBlock
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun SectionsList(
    sections: List<CourseSection>,
    expanded: Set<Int>,
    completion: Map<Int, CompletionState>,
    videoProgress: Map<Int, VideoProgress>,
    onToggleSection: (Int) -> Unit,
    onModuleClick: (CourseModule) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visibleSections = sections.filter { it.visible }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        visibleSections.forEachIndexed { index, section ->
            SectionCard(
                section = section,
                ordinal = index + 1,
                expanded = section.id in expanded,
                completion = completion,
                videoProgress = videoProgress,
                onToggle = { onToggleSection(section.id) },
                onModuleClick = onModuleClick,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SectionCard(
    section: CourseSection,
    ordinal: Int,
    expanded: Boolean,
    completion: Map<Int, CompletionState>,
    videoProgress: Map<Int, VideoProgress>,
    onToggle: () -> Unit,
    onModuleClick: (CourseModule) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val blocks = remember(section) { section.contentBlocks() }
    val summary = remember(section) { section.summaryPlainText() }
    val subtitle = remember(blocks) { typeSummary(blocks) }

    val transition = updateTransition(expanded, label = "section-card")
    val cornerDp by transition.animateDp(
        transitionSpec = { motion.defaultSpatialSpec() },
        label = "corner",
    ) { if (it) 28.dp else 20.dp }
    val chevronRotation by transition.animateFloat(
        transitionSpec = { motion.defaultSpatialSpec() },
        label = "chevron",
    ) { if (it) 180f else 0f }
    // The number badge twirls as its container morphs from a quiet rounded square into an
    // expressive polygon; the digit itself stays upright because only the backdrop rotates.
    val badgeRotation by transition.animateFloat(
        transitionSpec = { motion.defaultSpatialSpec() },
        label = "badge-rotation",
    ) { if (it) 120f else 0f }
    val badgeColor by transition.animateColor(
        transitionSpec = { motion.defaultEffectsSpec() },
        label = "badge-color",
    ) { if (it) scheme.primary else scheme.secondaryContainer }
    val badgeContentColor by transition.animateColor(
        transitionSpec = { motion.defaultEffectsSpec() },
        label = "badge-content-color",
    ) { if (it) scheme.onPrimary else scheme.onSecondaryContainer }
    val sizeSpec = remember(motion) { motion.defaultSpatialSpec<IntSize>() }
    val expandedShape = expandedBadgeShape(ordinal)
    val badgeShape = if (expanded) expandedShape else RoundedCornerShape(14.dp)

    Surface(
        shape = RoundedCornerShape(cornerDp),
        color = scheme.surfaceContainerLow,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = sizeSpec),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .graphicsLayer { rotationZ = badgeRotation }
                            .background(badgeColor, badgeShape),
                    )
                    Text(
                        text = ordinal.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = badgeContentColor,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = section.name.stripCopySuffix()
                            .ifBlank { "Sezione ${section.sectionNumber}" },
                        style = MaterialTheme.typography.titleMedium,
                        color = scheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Comprimi" else "Espandi",
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.rotate(chevronRotation),
                )
            }
            if (expanded) {
                Column(
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    summary?.let { NoteCallout(text = it) }
                    if (blocks.isEmpty()) {
                        Text(
                            text = "Nessuna risorsa in questa sezione",
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                    }
                    blocks.forEach { block ->
                        when (block) {
                            is ContentBlock.Note -> NoteCallout(text = block.text)
                            is ContentBlock.Group -> GroupBlock(
                                group = block,
                                completion = completion,
                                videoProgress = videoProgress,
                                onModuleClick = onModuleClick,
                            )
                        }
                    }
                }
            }
        }
    }
}

// MaterialShapes getters are @Composable in this material3 version, so the mapper is too.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun expandedBadgeShape(ordinal: Int): Shape = when (ordinal % 6) {
    0 -> MaterialShapes.Sunny
    1 -> MaterialShapes.Cookie9Sided
    2 -> MaterialShapes.Clover4Leaf
    3 -> MaterialShapes.Pentagon
    4 -> MaterialShapes.Flower
    else -> MaterialShapes.Cookie6Sided
}.toShape()

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun NoteCallout(text: String) {
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    var noteExpanded by remember { mutableStateOf(false) }
    val sizeSpec = remember(motion) { motion.defaultSpatialSpec<IntSize>() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { noteExpanded = !noteExpanded }
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .animateContentSize(animationSpec = sizeSpec),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = scheme.secondary,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
            fontStyle = FontStyle.Italic,
            maxLines = if (noteExpanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun GroupBlock(
    group: ContentBlock.Group,
    completion: Map<Int, CompletionState>,
    videoProgress: Map<Int, VideoProgress>,
    onModuleClick: (CourseModule) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        group.title?.let { title ->
            Row(
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp, top = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(scheme.tertiary, OrganicShapes.Leaf),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Text(
                    text = group.modules.size.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
        group.modules.forEachIndexed { index, module ->
            ModuleRow(
                module = module,
                shape = stackShape(index, group.modules.size),
                progress = module.kalvidresCmIdOrNull()?.let { videoProgress[it] },
                done = completion[module.cmId]?.isCompleted == true,
                onClick = { onModuleClick(module) },
            )
        }
    }
}

// The signature M3 Expressive grouped-list silhouette: big corners cap the run,
// small corners knit the inner seams together.
private fun stackShape(index: Int, count: Int): RoundedCornerShape {
    val big = 16.dp
    val small = 5.dp
    val top = if (index == 0) big else small
    val bottom = if (index == count - 1) big else small
    return RoundedCornerShape(topStart = top, topEnd = top, bottomEnd = bottom, bottomStart = bottom)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ModuleRow(
    module: CourseModule,
    shape: Shape,
    progress: VideoProgress?,
    done: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val watched = progress?.completed == true
    val watchingFraction = progress
        ?.takeIf { !it.completed && it.progressFraction > 0.01f }
        ?.progressFraction
    Surface(
        onClick = onClick,
        shape = shape,
        color = scheme.surfaceContainerLowest,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ModuleBadge(module = module, done = done || watched)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = module.name.stripCopySuffix(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(1.dp))
                Text(
                    text = moduleMeta(module, progress),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (watched) scheme.primary else scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (watchingFraction != null) {
                    LinearWavyProgressIndicator(
                        progress = { watchingFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ModuleBadge(module: CourseModule, done: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val family = module.family()
    val (bg, fg) = when {
        done -> scheme.primary to scheme.onPrimary
        family == ModuleFamily.Media -> scheme.primaryContainer to scheme.onPrimaryContainer
        family == ModuleFamily.Document -> scheme.secondaryContainer to scheme.onSecondaryContainer
        family == ModuleFamily.Interactive -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        else -> scheme.surfaceContainerHigh to scheme.onSurfaceVariant
    }
    val shape = if (done) OrganicShapes.Cookie else RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(bg, shape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (done) Icons.Filled.Check else module.icon(),
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(20.dp),
        )
    }
}

private enum class ModuleFamily { Media, Document, Interactive, Neutral }

private fun CourseModule.family(): ModuleFamily = when {
    isVideo() -> ModuleFamily.Media
    type in setOf(ModuleType.Resource, ModuleType.Folder, ModuleType.Book, ModuleType.Page) ->
        ModuleFamily.Document
    type in setOf(
        ModuleType.Quiz, ModuleType.Assign, ModuleType.Choice,
        ModuleType.Workshop, ModuleType.Lesson, ModuleType.Feedback,
    ) -> ModuleFamily.Interactive
    else -> ModuleFamily.Neutral
}

private fun CourseModule.isVideo(): Boolean =
    type == ModuleType.Kalvidres || type == ModuleType.H5p || type == ModuleType.Scorm ||
        kalvidresCmIdOrNull() != null

private fun CourseModule.icon(): ImageVector = when {
    isVideo() -> Icons.Filled.PlayArrow
    type == ModuleType.Resource -> resourceIcon()
    type == ModuleType.Quiz -> Icons.Outlined.Quiz
    type == ModuleType.Assign -> Icons.AutoMirrored.Outlined.Assignment
    type == ModuleType.Forum -> Icons.Outlined.Forum
    type == ModuleType.Url -> Icons.Outlined.Link
    type == ModuleType.Folder -> Icons.Outlined.Folder
    type == ModuleType.Page -> Icons.AutoMirrored.Outlined.Article
    type == ModuleType.Book -> Icons.Outlined.Book
    type == ModuleType.Lesson -> Icons.Outlined.School
    type == ModuleType.Choice -> Icons.Outlined.Poll
    else -> Icons.AutoMirrored.Outlined.HelpOutline
}

private fun CourseModule.resourceIcon(): ImageVector {
    val mime = contents.firstOrNull()?.mimeType.orEmpty()
    return when {
        mime.contains("pdf") -> Icons.Outlined.PictureAsPdf
        mime.contains("zip") || mime.contains("compressed") -> Icons.Outlined.Archive
        mime.startsWith("image") -> Icons.Outlined.Image
        mime.startsWith("video") -> Icons.Filled.PlayArrow
        else -> Icons.Outlined.Description
    }
}

private fun moduleMeta(module: CourseModule, progress: VideoProgress?): String = when {
    module.isVideo() -> when {
        progress?.completed == true -> "Guardato"
        progress != null && progress.progressFraction > 0.01f ->
            "Visto al ${(progress.progressFraction * 100).roundToInt()}%"
        else -> "Video"
    }
    module.type == ModuleType.Resource -> {
        val mimeLabel = mimeShortLabel(module.contents.firstOrNull()?.mimeType)
        val size = module.contents.sumOf { it.sizeBytes ?: 0L }.takeIf { it > 0 }?.let(::formatBytes)
        listOfNotNull(mimeLabel, size).joinToString(" · ")
    }
    module.type == ModuleType.Quiz -> withDueDate("Quiz", module.dueAt)
    module.type == ModuleType.Assign -> withDueDate("Compito", module.dueAt)
    module.type == ModuleType.Folder -> {
        val files = module.contents.size.takeIf { it > 0 }
        if (files != null) "Cartella · $files file" else "Cartella"
    }
    module.type == ModuleType.Forum -> "Forum"
    module.type == ModuleType.Url -> "Link"
    module.type == ModuleType.Page -> "Pagina"
    module.type == ModuleType.Book -> "Libro"
    module.type == ModuleType.Lesson -> "Lezione"
    module.type == ModuleType.Choice -> "Sondaggio"
    module.type == ModuleType.Wiki -> "Wiki"
    module.type == ModuleType.Glossary -> "Glossario"
    module.type == ModuleType.Workshop -> "Workshop"
    module.type == ModuleType.Feedback -> "Questionario"
    else -> "Attività"
}

private val DUE_DATE_FORMAT = DateTimeFormatter.ofPattern("d MMM", Locale.ITALIAN)

private fun withDueDate(base: String, dueAt: Instant?): String {
    if (dueAt == null) return base
    val label = DUE_DATE_FORMAT.format(dueAt.atZone(ZoneId.systemDefault()))
    return if (dueAt.isBefore(Instant.now())) "$base · chiuso il $label" else "$base · scade il $label"
}

private fun formatBytes(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> {
        val mb = bytes / (1024.0 * 1024.0)
        val pattern = if (mb >= 100) "%.0f MB" else "%.1f MB"
        String.format(Locale.ITALIAN, pattern, mb)
    }
}

// Subtitle like "26 video · 8 PDF · 1 quiz" — surveyed data shows PDFs and videos dominate,
// so a type-aware summary tells students far more than a flat resource count.
private fun typeSummary(blocks: List<ContentBlock>): String {
    val modules = blocks.flatMap { (it as? ContentBlock.Group)?.modules ?: emptyList() }
    if (modules.isEmpty()) return "Nessuna risorsa"
    val counts = linkedMapOf<String, Int>()
    fun add(key: String) = counts.merge(key, 1, Int::plus)
    modules.forEach { m ->
        when {
            m.isVideo() -> add("video")
            m.type == ModuleType.Resource ->
                add(if (m.contents.firstOrNull()?.mimeType?.contains("pdf") == true) "pdf" else "file")
            m.type == ModuleType.Quiz -> add("quiz")
            m.type == ModuleType.Assign -> add("assign")
            m.type == ModuleType.Forum -> add("forum")
            m.type == ModuleType.Url -> add("url")
            m.type == ModuleType.Folder -> add("folder")
            m.type == ModuleType.Page -> add("page")
            else -> add("other")
        }
    }
    return counts.entries
        .sortedByDescending { it.value }
        .take(3)
        .joinToString(" · ") { (key, n) -> "$n ${countLabel(key, n)}" }
}

private fun countLabel(key: String, count: Int): String = when (key) {
    "video" -> "video"
    "pdf" -> "PDF"
    "file" -> "file"
    "quiz" -> "quiz"
    "assign" -> if (count == 1) "compito" else "compiti"
    "forum" -> "forum"
    "url" -> "link"
    "folder" -> if (count == 1) "cartella" else "cartelle"
    "page" -> if (count == 1) "pagina" else "pagine"
    else -> "attività"
}

private fun mimeShortLabel(mime: String?): String {
    val m = mime.orEmpty()
    return when {
        m.contains("pdf") -> "PDF"
        m.contains("zip") || m.contains("compressed") -> "ZIP"
        m.startsWith("video") -> "Video"
        m.startsWith("image") -> "Immagine"
        m.contains("presentationml") || m.contains("ms-powerpoint") -> "PPT"
        m.contains("spreadsheetml") || m.contains("ms-excel") -> "XLS"
        m.contains("wordprocessingml") || m.contains("msword") -> "DOC"
        m == "text/plain" -> "TXT"
        else -> "File"
    }
}
