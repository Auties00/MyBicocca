package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.core.os.ConfigurationCompat
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.utils.capitalizeString
import it.attendance100.mybicocca.domain.model.elearning.course.CourseDetails
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseLevel
import it.attendance100.mybicocca.domain.model.elearning.course.CourseStaffMember
import it.attendance100.mybicocca.domain.model.elearning.course.CourseStaffRole
import it.attendance100.mybicocca.domain.model.elearning.course.ProgrammeSection
import it.attendance100.mybicocca.domain.model.elearning.course.Semester
import it.attendance100.mybicocca.domain.model.elearning.course.SyllabusInfo
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.component.shape.OrganicShapes
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state.StaffGridVariant
import it.attendance100.mybicocca.ui.screen.elearning.theme.CourseDetailTheme
import it.attendance100.mybicocca.ui.screen.elearning.theme.ProvideCourseAccentPalette
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import java.time.Month
import java.time.format.TextStyle.FULL

/**
 * Scheda tab page: the course syllabus rendered as a magazine-style column — an info tile
 * (CFU/hours stat cards, language flag swatch, degree-level pips, a semester calendar strip),
 * underlined prose sections (objectives, summary, prerequisites, teaching method, reference
 * material, assessment, office hours, SDGs), the extended programme as numbered "PARTE" parts
 * with topic pill clusters, and a two-column staff grid with organic-shape initial avatars.
 * Tapping a staff tile whose member has a published email opens the mail composer pre-addressed
 * to them with the course name pre-filled as the subject prefix. Shows an empty state when the
 * course has no published syllabus.
 */
@Composable
fun SyllabusContent(
    details: CourseDetails,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    emptyModifier: Modifier = Modifier,
) {
    val syllabus = details.syllabus
    if (syllabus == null || (syllabus.fields.isEmpty() && !syllabus.info.hasInfoTile)) {
        SyllabusEmpty(modifier = emptyModifier)
        return
    }

    val info = syllabus.info

    LazyColumn(
        state = listState,
        modifier = modifier.testTag(CourseDetailTestTags.SYLLABUS_CONTENT),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        if (info.hasInfoTile) {
            item {
                SyllabusInfoCard(info)
            }
        }

        info.objectives?.let { item { SyllabusSection(title = stringResource(R.string.elearning_course_objectives), body = it) } }
        info.summary?.let { item { SyllabusSection(title = stringResource(R.string.elearning_course_summary), body = it) } }

        if (info.extendedProgramme.isNotEmpty()) {
            item { ExtendedProgrammeList(parts = info.extendedProgramme) }
        }

        info.prerequisites?.let { item { SyllabusSection(title = stringResource(R.string.elearning_course_prerequisites), body = it) } }
        info.teachingMethod?.let { item { SyllabusSection(title = stringResource(R.string.elearning_course_teaching_method), body = it) } }
        info.referenceMaterial?.let { item { SyllabusSection(title = stringResource(R.string.elearning_course_reference_material), body = it) } }
        info.assessment?.let { item { SyllabusSection(title = stringResource(R.string.elearning_course_assessment), body = it) } }
        info.officeHours?.let { item { SyllabusSection(title = stringResource(R.string.elearning_course_office_hours), body = it) } }
        info.sustainableDevelopmentGoals?.let { item { SyllabusSection(title = stringResource(R.string.elearning_course_sustainable_goals), body = it) } }

        if (details.staff.isNotEmpty()) {
            item { StaffGrid(staff = details.staff, courseName = details.enrolled.fullName) }
        }
    }
}

@Composable
private fun SyllabusHeader(title: String) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = scheme.onSurface,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            lineHeight = 24.sp,
            letterSpacing = (-0.6).sp,
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(scheme.outlineVariant, RoundedCornerShape(2.dp)),
        )
    }
}

@Composable
fun SyllabusInfoCard(info: SyllabusInfo) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SyllabusHeader(title = stringResource(R.string.elearning_course_info_section))
        Spacer(Modifier.height(12.dp))
        SyllabusInfoTile(info = info)
    }
}

@Composable
private fun SyllabusSection(title: String, body: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SyllabusHeader(title = title)
        Spacer(Modifier.height(12.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 2.dp),
        )
    }
}

@Composable
private fun SyllabusInfoTile(info: SyllabusInfo) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = scheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (info.credits != null) {
                    BigStatCard(
                        value = info.credits.toString(),
                        label = stringResource(R.string.common_cfu),
                        caption = stringResource(R.string.elearning_course_credits_label),
                        modifier = Modifier.weight(1f),
                    )
                }
                if (info.hours != null && info.hours > 0) {
                    BigStatCard(
                        value = info.hours.toString(),
                        label = stringResource(R.string.elearning_course_hours_stat),
                        caption = stringResource(R.string.elearning_course_hours_label),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!info.language.isNullOrBlank()) {
                    LanguageCard(language = info.language, modifier = Modifier.weight(1f))
                }
                if (info.level != null) {
                    LevelCard(level = info.level, modifier = Modifier.weight(1f))
                }
            }

            if (info.semester != null) {
                SemesterCalendar(semester = info.semester)
            }
        }
    }
}

@Composable
private fun BigStatCard(value: String, label: String, caption: String, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = scheme.surfaceContainerHigh,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = value,
                    color = scheme.onSurface,
                    fontWeight = FontWeight.Black,
                    fontSize = 46.sp,
                    lineHeight = 40.sp,
                    letterSpacing = (-2.5).sp,
                )
                Text(
                    text = label,
                    color = scheme.onSurfaceVariant.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 1.4.sp,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }
            Spacer(Modifier.height(3.dp))
            Text(
                text = caption,
                color = scheme.onSurfaceVariant.copy(alpha = 0.78f),
                fontStyle = FontStyle.Italic,
                fontSize = 10.5.sp,
            )
        }
    }
}

@Composable
private fun LanguageCard(language: String, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = scheme.surfaceContainerHigh,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 12.dp)) {
            FlagSwatch(language = language)
            Spacer(Modifier.height(8.dp))
            Text(
                text = language.capitalizeString(),
                color = scheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                lineHeight = 19.sp,
                letterSpacing = (-0.3).sp,
            )
            Text(
                text = stringResource(R.string.elearning_course_language_label),
                color = scheme.onSurfaceVariant.copy(alpha = 0.78f),
                fontStyle = FontStyle.Italic,
                fontSize = 10.5.sp,
            )
        }
    }
}

@Composable
private fun FlagSwatch(language: String) {
    val colors = when (language.trim().lowercase()) {
        "italiano", "italian", "it" -> listOf(
            Color(0xFF008C45),
            Color(0xFFF4F5F0),
            Color(0xFFCD212A),
        )
        "inglese", "english", "en" -> listOf(
            Color(0xFF012169),
            Color(0xFFFFFFFF),
            Color(0xFFC8102E),
        )
        else -> listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.secondary,
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        colors.forEach { c ->
            Box(
                modifier = Modifier
                    .size(width = 12.dp, height = 20.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(c),
            )
        }
    }
}

@Composable
private fun LevelCard(level: CourseLevel, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = scheme.surfaceContainerHigh,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                CourseLevel.entries.forEachIndexed { i, t ->
                    LevelPip(label = courseLevelShort(t), active = t == level)
                    if (i < CourseLevel.entries.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(5.dp)
                                .height(1.5.dp)
                                .background(scheme.outlineVariant, RoundedCornerShape(1.dp)),
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = courseLevelTitle(level),
                color = scheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                lineHeight = 19.sp,
                letterSpacing = (-0.3).sp,
            )
            Text(
                text = stringResource(R.string.elearning_course_level_label),
                color = scheme.onSurfaceVariant.copy(alpha = 0.78f),
                fontStyle = FontStyle.Italic,
                fontSize = 10.5.sp,
            )
        }
    }
}

@Composable
private fun LevelPip(label: String, active: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val bg = if (active) scheme.primary else Color.Transparent
    val fg = if (active) scheme.onPrimary else scheme.outline
    val mod = if (active) {
        Modifier.background(bg, RoundedCornerShape(6.dp))
    } else {
        Modifier.border(1.5.dp, scheme.outlineVariant, RoundedCornerShape(6.dp))
    }
    Box(
        modifier = mod
            .padding(horizontal = 4.dp)
            .height(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = fg,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 9.5.sp,
            letterSpacing = 0.4.sp,
        )
    }
}

@Composable
private fun SemesterCalendar(semester: Semester) {
    val scheme = MaterialTheme.colorScheme
    val academicMonths = rememberAcademicMonths()
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = scheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
            ) {
                academicMonths.forEachIndexed { i, m ->
                    val active = i in semester.activeMonthIndices
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = m,
                            color = if (active) scheme.onSurface else scheme.outlineVariant,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 9.sp,
                            letterSpacing = 0.6.sp,
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (active) scheme.primary
                                    else scheme.outlineVariant.copy(alpha = 0.45f),
                                ),
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = semesterTitle(semester),
                    color = scheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    letterSpacing = (-0.2).sp,
                )
                Text(
                    text = semesterRange(semester),
                    color = scheme.onSurfaceVariant.copy(alpha = 0.78f),
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

/**
 * The "Programma esteso" block: one magazine-numbered entry per programme part, each with a
 * topic-count line and a pill cluster of its items. Headerless programmes parse as a single
 * untitled section, so a blank cleaned title simply skips the title row.
 */
@Composable
private fun ExtendedProgrammeList(parts: List<ProgrammeSection>) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth()) {
        SyllabusHeader(title = stringResource(R.string.elearning_course_extended_program))
        Spacer(Modifier.height(8.dp))
        parts.forEachIndexed { index, part ->
            if (index > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(scheme.outlineVariant),
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (index == 0) 4.dp else 18.dp, bottom = 18.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.elearning_course_part_prefix),
                            color = scheme.tertiary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 1.6.sp,
                            style = TightTextStyle,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = (index + 1).toString().padStart(2, '0'),
                            color = scheme.primary,
                            fontWeight = FontWeight.Black,
                            fontSize = 54.sp,
                            lineHeight = 48.sp,
                            letterSpacing = (-3.5).sp,
                            style = TightTextStyle,
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 18.dp)
                    ) {
                        val title = cleanProgrammeTitle(part.title)
                        if (title.isNotBlank()) {
                            Text(
                                text = title,
                                color = scheme.onSurface,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                lineHeight = 22.sp,
                                letterSpacing = (-0.4).sp,
                                overflow = TextOverflow.Ellipsis,
                                style = TightTextStyle,
                            )
                        }
                        if (part.items.isNotEmpty()) {
                            Spacer(Modifier.height(5.dp))
                            Text(
                                text = stringResource(R.string.elearning_course_topics_count, part.items.size),
                                color = scheme.onSurfaceVariant.copy(alpha = 0.75f),
                                fontStyle = FontStyle.Italic,
                                fontSize = 12.sp,
                                style = TightTextStyle,
                            )
                        }
                    }
                }
                if (part.items.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    PillCluster(items = part.items)
                }
            }
        }
    }
}

/**
 * Strips Compose's default font padding so visual text edges line up with the
 * layout box; the magazine PARTE/title/number column drifts visibly without it.
 */
private val TightTextStyle = TextStyle(
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Top,
        trim = LineHeightStyle.Trim.Both,
    ),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PillCluster(items: List<String>) {
    val scheme = MaterialTheme.colorScheme
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        items.forEach { label ->
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = scheme.surfaceContainerLow,
            ) {
                Text(
                    text = label,
                    color = scheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
                )
            }
        }
    }
}

@Composable
private fun StaffGrid(staff: List<CourseStaffMember>, courseName: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SyllabusHeader(title = stringResource(R.string.elearning_course_staff))
        Spacer(Modifier.height(12.dp))
        val rows = staff.chunked(2)
        rows.forEachIndexed { rowIdx, pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                pair.forEachIndexed { colIdx, member ->
                    val variantIdx = rowIdx * 2 + colIdx
                    StaffGridTile(
                        member = member,
                        variant = staffVariantAt(variantIdx),
                        courseName = courseName,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                }
                if (pair.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
            if (rowIdx < rows.lastIndex) Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun staffVariantAt(index: Int): StaffGridVariant {
    val scheme = MaterialTheme.colorScheme
    val tileBg = scheme.surfaceContainerHigh
    return when (index % 4) {
        0 -> StaffGridVariant(tileBg, OrganicShapes.Cookie, scheme.primary, scheme.onPrimary)
        1 -> StaffGridVariant(tileBg, OrganicShapes.Burst, scheme.tertiary, scheme.onTertiary)
        2 -> StaffGridVariant(tileBg, OrganicShapes.Sunny, scheme.primary, scheme.onPrimary)
        else -> StaffGridVariant(tileBg, OrganicShapes.SmoothCookie6, scheme.tertiary, scheme.onTertiary)
    }
}

@Composable
private fun StaffGridTile(
    member: CourseStaffMember,
    variant: StaffGridVariant,
    courseName: String,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val email = member.email
    Surface(
        onClick = { email?.let { context.emailStaff(it, "$courseName | ") } },
        enabled = email != null,
        shape = RoundedCornerShape(22.dp),
        color = variant.tileBg,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(variant.avatarShape)
                    .background(variant.avatarBg),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = member.initials ?: initialsOf(member.fullName),
                    color = variant.avatarFg,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                )
            }
            Column {
                Text(
                    text = member.fullName,
                    color = scheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = roleLabel(member.role).uppercase(),
                    color = scheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    letterSpacing = 1.4.sp,
                )
            }
        }
    }
}

@Composable
private fun SyllabusEmpty(modifier: Modifier) {
    EmptyState(
        icon = Icons.Outlined.Description,
        title = stringResource(R.string.elearning_course_syllabus_not_available),
        body = stringResource(R.string.elearning_course_syllabus_not_published),
        modifier = modifier.testTag(CourseDetailTestTags.SYLLABUS_EMPTY),
    )
}

@Composable
private fun courseLevelTitle(level: CourseLevel): String = stringResource(
    when (level) {
        CourseLevel.Bachelor -> R.string.elearning_course_level_bachelor
        CourseLevel.Master -> R.string.elearning_course_level_master
        CourseLevel.Doctorate -> R.string.elearning_course_level_doctorate
    }
)

@Composable
private fun courseLevelShort(level: CourseLevel): String = stringResource(
    when (level) {
        CourseLevel.Bachelor -> R.string.elearning_course_level_bachelor_short
        CourseLevel.Master -> R.string.elearning_course_level_master_short
        CourseLevel.Doctorate -> R.string.elearning_course_level_doctorate_short
    }
)

@Composable
private fun semesterTitle(semester: Semester): String = stringResource(
    when (semester) {
        Semester.First -> R.string.elearning_course_semester_first
        Semester.Second -> R.string.elearning_course_semester_second
        Semester.FullYear -> R.string.elearning_course_semester_annual
    }
)

@Composable
private fun semesterRange(semester: Semester): String = stringResource(
    when (semester) {
        Semester.First -> R.string.elearning_course_semester_first_range
        Semester.Second -> R.string.elearning_course_semester_second_range
        Semester.FullYear -> R.string.elearning_course_semester_annual_range
    }
)

@Composable
private fun roleLabel(role: CourseStaffRole): String = stringResource(
    when (role) {
        CourseStaffRole.Docente -> R.string.elearning_course_docente_role
        CourseStaffRole.Tutor -> R.string.elearning_course_tutor_role
        CourseStaffRole.Esercitatore -> R.string.elearning_course_esercitatore_role
        CourseStaffRole.Other -> R.string.elearning_course_staff_role
    }
)

private fun initialsOf(name: String): String =
    name.split(" ", "\t").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifBlank { "?" }

/**
 * Opens the device's email composer addressed to [address] with [subject] pre-filled, so the
 * student only has to append their message. Uses a bare `mailto:` target with the recipient
 * and subject passed as intent extras, which restricts resolution to email apps. Wrapped in
 * runCatching so a device with no mail app resolves to a no-op instead of crashing.
 */
private fun Context.emailStaff(address: String, subject: String) {
    runCatching {
        startActivity(
            Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:".toUri()
                putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
                putExtra(Intent.EXTRA_SUBJECT, subject)
            }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}

private val LeadingNumberRegex = Regex("""^\d+[.):\s-]*""")

private fun cleanProgrammeTitle(raw: String): String =
    raw.trim().replaceFirst(LeadingNumberRegex, "").trim()

@Composable
fun rememberAcademicMonths(): List<String> {
    val configuration = LocalConfiguration.current
    val currentLocale =
        ConfigurationCompat.getLocales(configuration).get(0) ?: Locale.current.platformLocale

    val academicSequence = listOf(
        Month.SEPTEMBER, Month.OCTOBER, Month.NOVEMBER, Month.DECEMBER,
        Month.JANUARY, Month.FEBRUARY, Month.MARCH, Month.APRIL,
        Month.MAY, Month.JUNE, Month.JULY, Month.AUGUST
    )

    return academicSequence.map { month ->
        month.getDisplayName(FULL, currentLocale)
            .first()
            .uppercase()
    }
}

@Preview(showBackground = true)
@Composable
fun StaffGridPreview() {
    val sampleStaff = listOf(
        CourseStaffMember(
            1,
            "Mario Rossi",
            CourseStaffRole.Docente,
            "MR",
            "mario.rossi@unimib.it",
            null
        ),
        CourseStaffMember(
            2,
            "Gianfranco Eleganti Bianchi",
            CourseStaffRole.Tutor,
            "LB",
            "luigi.bianchi@unimib.it",
            null
        ),
        CourseStaffMember(3, "Anna Verdi", CourseStaffRole.Esercitatore, "AV", null, null),
        CourseStaffMember(
            4,
            "Paolo Neri",
            CourseStaffRole.Other,
            "PN",
            "paolo.neri@unimib.it",
            null
        ),
        CourseStaffMember(
            5,
            "Sofia Gialli",
            CourseStaffRole.Docente,
            "SG",
            "sofia.gialli@unimib.it",
            null
        )
    )
    BicoccaTheme(dark = false) {
        ProvideCourseAccentPalette(dark = false) {
            CourseDetailTheme(courseId = CourseId(1), dark = false) {
                Surface {
                    StaffGrid(
                        staff = sampleStaff,
                        courseName = "Programmazione Mobile"
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SyllabusInfoCardPreview() {
    val sampleInfo = SyllabusInfo(
        type = "Lezione",
        credits = 6,
        hours = 48,
        language = "Italiano",
        level = CourseLevel.Bachelor,
        semester = Semester.First,
        disciplinarySector = "INF/01",
        objectives = "Il corso si propone di fornire le basi della programmazione mobile.",
        summary = "Introduzione ad Android, Kotlin e Jetpack Compose.",
        extendedProgramme = listOf(
            ProgrammeSection(
                "Introduzione",
                listOf("Storia di Android", "Architettura del sistema")
            ),
            ProgrammeSection("Kotlin", listOf("Sintassi base", "Coroutines", "Flow"))
        ),
        prerequisites = "Conoscenza della programmazione a oggetti.",
        teachingMethod = "Lezioni frontali ed esercitazioni in laboratorio.",
        referenceMaterial = "Documentazione ufficiale Android.",
        assessment = "Progetto finale e prova orale.",
        officeHours = "Su appuntamento via email.",
        sustainableDevelopmentGoals = "Istruzione di qualità."
    )
    BicoccaTheme(dark = false) {
        ProvideCourseAccentPalette(dark = false) {
            CourseDetailTheme(courseId = CourseId(1), dark = false) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    SyllabusInfoCard(info = sampleInfo)
                }
            }
        }
    }
}
