package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Poll
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
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
import it.attendance100.mybicocca.ui.component.text.HtmlBody
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.ext.containsRenderableHtml
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.ext.contentBlocks
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.ext.htmlToPlainText
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.ext.stripCopySuffix
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state.ContentBlock
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Emits one LazyColumn item per section card, so long courses only compose the cards on
 * screen (the host list owns spacing and padding).
 *
 * mod_subsection child sections are not top-level cards: they render inline under their
 * placeholder module. A subsection module resolves to its child by the linked section id
 * (customdata.sectionid) or, failing that, by the child's owning itemid.
 */
fun LazyListScope.sectionCards(
    sections: List<CourseSection>,
    expanded: Set<Int>,
    completion: Map<Int, CompletionState>,
    videoProgress: Map<Int, VideoProgress>,
    lastSeenCmId: Int?,
    onToggleSection: (Int) -> Unit,
    onModuleClick: (CourseModule) -> Unit,
    onModuleLongClick: (CourseModule) -> Unit,
) {
    val children = sections.filter { it.isSubsectionChild }
    val childById = children.associateBy { it.id }
    val childByItemId = children.mapNotNull { c -> c.itemId?.let { it to c } }.toMap()
    val resolveSubsection: (CourseModule) -> CourseSection? = { module ->
        module.linkedSectionId?.let(childById::get) ?: module.instanceId?.let(childByItemId::get)
    }
    val visibleSections = sections.filter {
        it.visible && !it.isSubsectionChild && it.isRenderable()
    }
    itemsIndexed(visibleSections, key = { _, section -> section.id }) { index, section ->
        SectionCard(
            section = section,
            ordinal = index + 1,
            expanded = section.id in expanded,
            completion = completion,
            videoProgress = videoProgress,
            lastSeenCmId = lastSeenCmId,
            resolveSubsection = resolveSubsection,
            onToggle = { onToggleSection(section.id) },
            onModuleClick = onModuleClick,
            onModuleLongClick = onModuleLongClick,
        )
    }
}

/**
 * A section earns a card if it has a renderable summary or at least one module the student can
 * actually see (visible + on the course page). Sections that are wholly empty or hold only
 * stealth modules render to nothing and are dropped rather than showing a blank expandable card.
 */
private fun CourseSection.isRenderable(): Boolean =
    summary?.containsRenderableHtml() == true ||
        modules.any { it.visible && it.onCoursePage }

@Composable
private fun SectionCard(
    section: CourseSection,
    ordinal: Int,
    expanded: Boolean,
    completion: Map<Int, CompletionState>,
    videoProgress: Map<Int, VideoProgress>,
    lastSeenCmId: Int?,
    resolveSubsection: (CourseModule) -> CourseSection?,
    onToggle: () -> Unit,
    onModuleClick: (CourseModule) -> Unit,
    onModuleLongClick: (CourseModule) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val blocks = remember(section) { section.contentBlocks(resolveSubsection) }
    val summaryHtml = remember(section) { section.summary?.takeIf { it.containsRenderableHtml() } }
    val subtitle = remember(blocks) { typeSummary(blocks) }

    ExpandableGroupCard(
        ordinal = ordinal,
        title = section.name.stripCopySuffix().ifBlank { "Sezione ${section.sectionNumber}" },
        subtitle = subtitle,
        expanded = expanded,
        onToggle = onToggle,
    ) {
        summaryHtml?.let { HtmlCallout(html = it) }
        if (blocks.isEmpty() && summaryHtml == null) {
            Text(
                text = "Nessuna risorsa in questa sezione",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
        SectionBlocks(
            blocks = blocks,
            completion = completion,
            videoProgress = videoProgress,
            lastSeenCmId = lastSeenCmId,
            onModuleClick = onModuleClick,
            onModuleLongClick = onModuleLongClick,
        )
    }
}

/**
 * Renders a run of content blocks; recurses for inlined subsections. Shared by the section
 * card body and the subsection expander so nesting renders identically.
 */
@Composable
private fun SectionBlocks(
    blocks: List<ContentBlock>,
    completion: Map<Int, CompletionState>,
    videoProgress: Map<Int, VideoProgress>,
    lastSeenCmId: Int?,
    onModuleClick: (CourseModule) -> Unit,
    onModuleLongClick: (CourseModule) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        blocks.forEach { block ->
            when (block) {
                is ContentBlock.Note -> HtmlCallout(html = block.html)
                is ContentBlock.Group -> GroupBlock(
                    group = block,
                    completion = completion,
                    videoProgress = videoProgress,
                    lastSeenCmId = lastSeenCmId,
                    onModuleClick = onModuleClick,
                    onModuleLongClick = onModuleLongClick,
                )
                is ContentBlock.Subsection -> SubsectionBlock(
                    block = block,
                    completion = completion,
                    videoProgress = videoProgress,
                    lastSeenCmId = lastSeenCmId,
                    onModuleClick = onModuleClick,
                    onModuleLongClick = onModuleLongClick,
                )
            }
        }
    }
}

/**
 * A teacher note (content label) rendered as real HTML so links stay tappable and lists keep
 * their structure — flattening to plain text would silently drop ebook/Meet/GitHub links and
 * lesson syllabi.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HtmlCallout(html: String) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
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
        HtmlBody(
            html = html,
            color = scheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
    }
}

/** An inlined mod_subsection: a labelled expander that reveals the child section's own blocks. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SubsectionBlock(
    block: ContentBlock.Subsection,
    completion: Map<Int, CompletionState>,
    videoProgress: Map<Int, VideoProgress>,
    lastSeenCmId: Int?,
    onModuleClick: (CourseModule) -> Unit,
    onModuleLongClick: (CourseModule) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    var open by remember(block) { mutableStateOf(false) }
    val chevron by animateFloatAsState(if (open) 180f else 0f, label = "subsection-chevron")
    val sizeSpec = remember(motion) { motion.defaultSpatialSpec<IntSize>() }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = scheme.surfaceContainerLowest,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = sizeSpec),
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { open = !open }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(scheme.tertiary, OrganicShapes.Leaf),
                )
                Text(
                    text = block.title.ifBlank { "Sezione" },
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (open) "Comprimi" else "Espandi",
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.rotate(chevron),
                )
            }
            if (open) {
                Column(
                    modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 8.dp, top = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    block.summaryHtml?.let { HtmlCallout(html = it) }
                    if (block.blocks.isEmpty() && block.summaryHtml == null) {
                        Text(
                            text = "Nessuna risorsa",
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                    }
                    SectionBlocks(
                        blocks = block.blocks,
                        completion = completion,
                        videoProgress = videoProgress,
                        lastSeenCmId = lastSeenCmId,
                        onModuleClick = onModuleClick,
                        onModuleLongClick = onModuleLongClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupBlock(
    group: ContentBlock.Group,
    completion: Map<Int, CompletionState>,
    videoProgress: Map<Int, VideoProgress>,
    lastSeenCmId: Int?,
    onModuleClick: (CourseModule) -> Unit,
    onModuleLongClick: (CourseModule) -> Unit,
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
                isLastSeen = module.kalvidresCmIdOrNull() == lastSeenCmId,
                onClick = { onModuleClick(module) },
                onLongClick = { onModuleLongClick(module) },
            )
        }
    }
}

/**
 * Row for one module inside a section card: a type-family-colored badge, the name, a meta line,
 * an optional description preview, and a watch-progress bar for partially watched videos (wavy
 * for the most recently seen one, flat for the rest).
 *
 * A module that is teacher-visible but gated for this user shows the restriction reason in
 * place of the usual meta, yet stays tappable (e.g. a password-protected Webex unlocks in the
 * browser). The description preview surfaces lecture syllabi / deadlines stated in the body,
 * kept as raw HTML so links stay tappable (forum deadlines, Meet/GitHub links), rendered
 * compactly and line-limited so video syllabi don't bloat the row. Indentation honours the
 * teacher's visual nesting; subsection placeholders carry indent on their children.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationApi::class)
@Composable
private fun ModuleRow(
    module: CourseModule,
    shape: Shape,
    progress: VideoProgress?,
    done: Boolean,
    isLastSeen: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val watched = progress?.completed == true
    val watchingFraction = progress
        ?.takeIf { !it.completed && it.progressFraction > 0.01f }
        ?.progressFraction
    val locked = !module.accessible
    val descriptionHtml = remember(module) {
        module.description?.takeIf { it.containsRenderableHtml() }
    }
    val restriction = remember(module) {
        module.availabilityInfo?.htmlToPlainText()?.takeIf { it.isNotBlank() }
    }
    val indentPadding = (module.indent.coerceIn(0, 4) * 14).dp
    Surface(
        shape = shape,
        color = scheme.surfaceContainerLowest,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        Row(
            modifier = Modifier.padding(
                start = 12.dp + indentPadding,
                end = 12.dp,
                top = 10.dp,
                bottom = 10.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ModuleBadge(module = module, done = done || watched, locked = locked)
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
                    text = restriction?.let { "🔒 $it" } ?: moduleMeta(
                        module,
                        progress
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        locked -> scheme.onSurfaceVariant
                        watched -> scheme.primary
                        else -> scheme.onSurfaceVariant
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (descriptionHtml != null && !locked) {
                    Spacer(Modifier.height(2.dp))
                    HtmlBody(
                        html = descriptionHtml,
                        color = scheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (watchingFraction != null) {
                    if (isLastSeen) {
                        LinearWavyProgressIndicator(
                            progress = { watchingFraction },
                            stopSize = 0.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            trackColor = scheme.surfaceContainerHigh,
                        )
                    } else {
                        LinearProgressIndicator(
                            progress = { watchingFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = scheme.primary,
                            trackColor = scheme.surfaceContainerHigh,
                            drawStopIndicator = {},
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModuleBadge(module: CourseModule, done: Boolean, locked: Boolean = false) {
    val scheme = MaterialTheme.colorScheme
    val family = module.family()
    val (bg, fg) = when {
        locked -> scheme.surfaceContainerHigh to scheme.onSurfaceVariant
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
            imageVector = when {
                locked -> Icons.Outlined.Lock
                done -> Icons.Filled.Check
                else -> module.icon()
            },
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(20.dp),
        )
    }
}

private enum class ModuleFamily { Media, Document, Interactive, Neutral }

private fun CourseModule.family(): ModuleFamily = when {
    isVideo() || type == ModuleType.Webex -> ModuleFamily.Media
    type in setOf(ModuleType.Resource, ModuleType.Folder, ModuleType.Book, ModuleType.Page) ->
        ModuleFamily.Document
    type in setOf(
        ModuleType.Quiz, ModuleType.Assign, ModuleType.Choice,
        ModuleType.Workshop, ModuleType.Lesson, ModuleType.Feedback,
        ModuleType.Wooclap, ModuleType.Lti, ModuleType.Reservation,
        ModuleType.ChoiceGroup, ModuleType.Database, ModuleType.Attendance,
        ModuleType.Scheduler,
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
    type == ModuleType.Forum || type == ModuleType.HsuForum -> Icons.Outlined.Forum
    type == ModuleType.Url -> Icons.Outlined.Link
    type == ModuleType.Folder -> Icons.Outlined.Folder
    type == ModuleType.Page -> Icons.AutoMirrored.Outlined.Article
    type == ModuleType.Book -> Icons.Outlined.Book
    type == ModuleType.Lesson -> Icons.Outlined.School
    type == ModuleType.Choice || type == ModuleType.Feedback -> Icons.Outlined.Poll
    type == ModuleType.Wooclap -> Icons.Outlined.Poll
    type == ModuleType.Webex -> Icons.Outlined.Videocam
    type == ModuleType.Lti -> Icons.Outlined.Extension
    type == ModuleType.Reservation || type == ModuleType.Scheduler -> Icons.Outlined.EventAvailable
    type == ModuleType.ChoiceGroup -> Icons.Outlined.Groups
    type == ModuleType.Database -> Icons.Outlined.Storage
    type == ModuleType.Attendance -> Icons.AutoMirrored.Outlined.FactCheck
    else -> Icons.AutoMirrored.Outlined.HelpOutline
}

private fun CourseModule.resourceIcon(): ImageVector {
    val mime = contents.firstOrNull()?.mimeType.orEmpty()
    return when {
        mime.contains("pdf") -> Icons.Outlined.PictureAsPdf
        mime.contains("zip") || mime.contains("compressed") -> Icons.Outlined.FolderZip
        mime.startsWith("image") -> Icons.Outlined.Image
        mime.startsWith("video") -> Icons.Filled.PlayArrow
        else -> Icons.Outlined.Description
    }
}

/**
 * One-line meta label for a module row: an Italian type label enriched with watch progress,
 * file type and size, file counts, or deadlines depending on the type.
 *
 * Deadlines read the module's typed due/open instants, which Moodle ships in dates[] keyed by
 * dataId — so Italian-labelled deadlines ("Data limite:", "Chiuso:") surface where matching on
 * English label substrings would drop them. For unknown types the server's plural label
 * ("Open Forums", …) beats a flat "Attività". Forum unread counts (and similar afterlink
 * badges) are worth surfacing; resource upload metadata is noise, so only forums carry the
 * afterlink through.
 */
private fun moduleMeta(module: CourseModule, progress: VideoProgress?): String {
    val base = when {
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
        module.type == ModuleType.Quiz -> withDeadline("Quiz", module.dueAt)
        module.type == ModuleType.Assign -> withDeadlineOrOpen("Compito", module)
        module.type == ModuleType.Folder -> {
            val files = module.contents.size.takeIf { it > 0 }
            if (files != null) "Cartella · $files file" else "Cartella"
        }
        module.type == ModuleType.Forum || module.type == ModuleType.HsuForum -> "Forum"
        module.type == ModuleType.Url -> "Link"
        module.type == ModuleType.Page -> "Pagina"
        module.type == ModuleType.Book -> "Libro"
        module.type == ModuleType.Lesson -> "Lezione"
        module.type == ModuleType.Choice -> withDeadline("Sondaggio", module.dueAt)
        module.type == ModuleType.ChoiceGroup -> withDeadlineOrOpen("Scelta gruppo", module)
        module.type == ModuleType.Wiki -> "Wiki"
        module.type == ModuleType.Glossary -> "Glossario"
        module.type == ModuleType.Workshop -> "Workshop"
        module.type == ModuleType.Feedback -> withDeadline("Questionario", module.dueAt)
        module.type == ModuleType.Wooclap -> "Wooclap"
        module.type == ModuleType.Lti -> "Strumento esterno"
        module.type == ModuleType.Reservation -> withDeadlineOrOpen("Prenotazione", module)
        module.type == ModuleType.Webex -> "Webex"
        module.type == ModuleType.Database -> "Database"
        module.type == ModuleType.Attendance -> "Presenze"
        module.type == ModuleType.Scheduler -> "Appuntamenti"
        else -> module.typeLabel?.takeIf { it.isNotBlank() } ?: "Attività"
    }
    val badge = module.afterLink
        ?.takeIf { module.type == ModuleType.Forum || module.type == ModuleType.HsuForum }
        ?.takeIf { it.length <= 30 }
    return if (badge != null) "$base · $badge" else base
}

private val DEADLINE_FORMAT = DateTimeFormatter.ofPattern("d MMM HH:mm", Locale.ITALIAN)

private fun withDeadline(base: String, deadline: Instant?): String {
    if (deadline == null) return base
    val label = DEADLINE_FORMAT.format(deadline.atZone(ZoneId.systemDefault()))
    return if (deadline.isBefore(Instant.now())) "$base · chiuso il $label" else "$base · entro il $label"
}

/** Prefers a real deadline; falls back to the opening date when that's all the module exposes. */
private fun withDeadlineOrOpen(base: String, module: CourseModule): String {
    module.dueAt?.let { return withDeadline(base, it) }
    val opens = module.opensAt ?: return base
    val label = DEADLINE_FORMAT.format(opens.atZone(ZoneId.systemDefault()))
    return if (opens.isAfter(Instant.now())) "$base · dal $label" else base
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

/**
 * Subtitle like "26 video · 8 PDF · 1 quiz" — surveyed data shows PDFs and videos dominate,
 * so a type-aware summary tells students far more than a flat resource count.
 */
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
