package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.domain.model.elearning.course.CompletionState
import it.attendance100.mybicocca.domain.model.elearning.course.CourseModule
import it.attendance100.mybicocca.domain.model.elearning.course.CourseSection
import it.attendance100.mybicocca.domain.model.elearning.course.ModuleType
import it.attendance100.mybicocca.ui.component.shape.OrganicShapes

@Composable
fun SectionsList(
    sections: List<CourseSection>,
    expanded: Set<Int>,
    completion: Map<Int, CompletionState>,
    onToggleSection: (Int) -> Unit,
    onModuleClick: (CourseModule) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visibleSections = sections.filter { it.visible }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        visibleSections.forEach { section ->
            SectionRow(
                section = section,
                expanded = section.id in expanded,
                completion = completion,
                onToggle = { onToggleSection(section.id) },
                onModuleClick = onModuleClick,
            )
        }
    }
}

@Composable
private fun SectionRow(
    section: CourseSection,
    expanded: Boolean,
    completion: Map<Int, CompletionState>,
    onToggle: () -> Unit,
    onModuleClick: (CourseModule) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val visibleModules = section.modules.filter { it.visible && it.type != ModuleType.Label }
    val motion = MaterialTheme.motionScheme
    val transition = updateTransition(expanded, label = "section-row")
    val cornerDp by transition.animateDp(
        transitionSpec = { motion.defaultSpatialSpec() },
        label = "corner",
    ) { if (it) 28.dp else 20.dp }
    val chevronRotation by transition.animateFloat(
        transitionSpec = { motion.defaultSpatialSpec() },
        label = "chevron",
    ) { if (it) 180f else 0f }
    val sizeSpec = remember(motion) { motion.defaultSpatialSpec<IntSize>() }

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
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Comprimi" else "Espandi",
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.rotate(chevronRotation),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = section.name.ifBlank { "Sezione ${section.sectionNumber}" },
                        style = MaterialTheme.typography.titleSmall,
                        color = scheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${visibleModules.size} risorse",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                        fontStyle = FontStyle.Italic,
                    )
                }
            }
            if (expanded && visibleModules.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    visibleModules.forEach { module ->
                        ModuleRow(
                            module = module,
                            done = completion[module.cmId]?.isCompleted == true,
                            onClick = { onModuleClick(module) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModuleRow(
    module: CourseModule,
    done: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = scheme.surfaceContainerLowest,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ModuleIconBadge(type = module.type, done = done)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = moduleTypeLabel(module.type).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    fontSize = 9.sp,
                )
                Text(
                    text = module.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                moduleSubtitle(module)?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ModuleIconBadge(type: ModuleType, done: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val (bg, fg, shape) = if (done) {
        Triple(scheme.primary, scheme.onPrimary, OrganicShapes.Cookie)
    } else {
        Triple(scheme.surfaceContainerHigh, scheme.onSurfaceVariant, RoundedCornerShape(12.dp))
    }
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(shape)
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (done) Icons.Filled.Check else moduleIcon(type),
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(18.dp),
        )
    }
}

private fun moduleIcon(type: ModuleType): ImageVector = when (type) {
    ModuleType.Quiz -> Icons.Outlined.Quiz
    ModuleType.Assign -> Icons.AutoMirrored.Outlined.Assignment
    ModuleType.Forum -> Icons.Outlined.Forum
    ModuleType.Url -> Icons.Outlined.Link
    ModuleType.Folder -> Icons.Outlined.Folder
    ModuleType.Page -> Icons.AutoMirrored.Outlined.Article
    ModuleType.Book -> Icons.Outlined.Book
    ModuleType.Lesson -> Icons.Outlined.School
    ModuleType.H5p, ModuleType.Scorm, ModuleType.Kalvidres -> Icons.Outlined.PlayCircleOutline
    ModuleType.Resource -> Icons.AutoMirrored.Outlined.Article
    else -> Icons.AutoMirrored.Outlined.HelpOutline
}

private fun moduleTypeLabel(type: ModuleType): String = when (type) {
    ModuleType.Resource -> "Risorsa"
    ModuleType.Url -> "Link"
    ModuleType.Page -> "Pagina"
    ModuleType.Folder -> "Cartella"
    ModuleType.Quiz -> "Quiz"
    ModuleType.Assign -> "Compito"
    ModuleType.Forum -> "Forum"
    ModuleType.Choice -> "Scelta"
    ModuleType.Workshop -> "Workshop"
    ModuleType.Lesson -> "Lezione"
    ModuleType.Scorm -> "SCORM"
    ModuleType.Glossary -> "Glossario"
    ModuleType.Wiki -> "Wiki"
    ModuleType.Book -> "Libro"
    ModuleType.Feedback -> "Feedback"
    ModuleType.H5p -> "H5P"
    ModuleType.Kalvidres -> "Video"
    ModuleType.Label -> "Etichetta"
    ModuleType.Other -> "Risorsa"
}

private fun moduleSubtitle(module: CourseModule): String? {
    if (module.contents.isNotEmpty()) {
        val files = module.contents.size
        val totalBytes = module.contents.sumOf { it.sizeBytes ?: 0L }
        val sizeLabel = when {
            totalBytes <= 0L -> null
            totalBytes < 1024 -> "${totalBytes} B"
            totalBytes < 1024 * 1024 -> "${totalBytes / 1024} KB"
            else -> "${totalBytes / (1024 * 1024)} MB"
        }
        val parts = mutableListOf("$files file")
        if (sizeLabel != null) parts += sizeLabel
        return parts.joinToString(" · ")
    }
    return null
}

