package it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.subscreen.courseAttendanceDetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.attendance.ClassroomAttendance
import it.attendance100.mybicocca.domain.model.attendance.CourseAttendance
import it.attendance100.mybicocca.domain.model.attendance.SessionAttendance
import it.attendance100.mybicocca.ui.component.SegmentedSwitch
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.screen.registry.theme.RegistryAccent
import it.attendance100.mybicocca.ui.screen.registry.theme.registryAccent
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// The course page inside the presenze sheet: a swipable pager with one attendance tab
// per source — "Lezioni" from the EasyStaff badge telemetry, plus one per Moodle session
// register — driven by the same sliding segmented footer the esami sostenuti modal uses.
// The hosting sheet's pinned header carries the course name; each tab is a single hero
// ring with at most one supporting stat row beneath it.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CourseOverviewPage(course: CourseAttendance) {
    val tabs = remember(course) { course.attendanceTabs() }
    val pagerState = rememberPagerState { tabs.size }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // FIXED height, sized to the tab layout (every tab body is TAB_BODY_HEIGHT
                // tall): the sheet never resizes while swiping between sources.
                .height(420.dp)
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) { pageIndex ->
                when (val tab = tabs[pageIndex]) {
                    is AttendanceTab.Lezioni -> LezioniTabBody(classroom = tab.classroom)
                    is AttendanceTab.Register -> RegisterTabBody(session = tab.session)
                }
            }
        }

        // Always present, even with the single "Lezioni" tab: the footer is part of the
        // page's identity, not just a switcher.
        SegmentedSwitch(
            options = tabs.indices.toList(),
            selected = pagerState.targetPage.coerceIn(0, tabs.lastIndex),
            onSelected = { index -> scope.launch { pagerState.animateScrollToPage(index) } },
            label = { tabs[it].title },
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 24.dp),
        )
    }
}

private val TAB_BODY_HEIGHT = 400.dp

private sealed interface AttendanceTab {
    val title: String

    // EasyStaff in-classroom badge telemetry.
    data class Lezioni(val classroom: ClassroomAttendance?) : AttendanceTab {
        override val title: String get() = "Lezioni"
    }

    // One Moodle mod_attendance session register.
    data class Register(override val title: String, val session: SessionAttendance) : AttendanceTab
}

private fun CourseAttendance.attendanceTabs(): List<AttendanceTab> = buildList {
    add(AttendanceTab.Lezioni(classroomAttendance))
    sessionAttendance.forEach { add(AttendanceTab.Register(it.tabTitle(), it)) }
}

// Moodle register names arrive as "Presenze LABORATORIO": drop the prefix and
// sentence-case what remains.
private fun SessionAttendance.tabTitle(): String {
    val cleaned = label.trim()
        .let { if (it.startsWith("presenze", ignoreCase = true)) it.drop("presenze".length) else it }
        .trim()
        .lowercase()
        .replaceFirstChar { it.titlecase() }
    return cleaned.ifBlank { "Sessioni" }
}

// EasyStaff path: the hero presence ring — green once the minimum frequenza for the
// exam is met, yellow while still below it — with the minimum itself footnoted inside.
@Composable
private fun LezioniTabBody(classroom: ClassroomAttendance?) {
    if (classroom == null) {
        TabEmptyState(
            title = "Nessuna rilevazione in aula",
            body = "EasyStaff non ha ancora registrato presenze per questo corso.",
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(TAB_BODY_HEIGHT),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AttendanceRing(
            progress = (classroom.attendancePercentage / 100.0).toFloat(),
            headline = "${classroom.attendancePercentage.roundToInt()}%",
            caption = "frequenza",
            footnote = classroom.minRequiredPercentage()?.let { "minimo $it%" },
            color = classroom.requirementColor(),
        )
    }
}

// Moodle path: the ring rides the taken-sessions percentage. The register's raw total
// (sessionstotal) is NOT shown: for group-based labs the module holds every turno's
// sessions, so a perfect 10/10 attendance reads as a nonsensical 10/82 against it.
@Composable
private fun RegisterTabBody(session: SessionAttendance) {
    val recorded = session.recordedPercentage
    if (recorded == null) {
        TabEmptyState(
            title = "Nessuna rilevazione disponibile",
            body = "Il registro non contiene ancora presenze registrate.",
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(TAB_BODY_HEIGHT),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AttendanceRing(
            progress = (recorded / 100.0).toFloat(),
            headline = "${recorded.roundToInt()}%",
            caption = "sulle registrate",
            footnote = session.attendedSessions?.let {
                if (it == 1) "1 presenza" else "$it presenze"
            },
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

// Requirement gate on the ring stroke: requirementProgressPercentage is progress towards
// the course's minimum attendance for the exam, so 100 means the requirement is met.
@Composable
private fun ClassroomAttendance.requirementColor(): Color =
    if (requirementProgressPercentage >= 100.0) registryAccent(RegistryAccent.Success)
    else registryAccent(RegistryAccent.Warning)

// EasyStaff exposes no minimum-attendance field, but conseguimento is frequenza measured
// against it, so the threshold falls out as frequenza / conseguimento. Exact while the
// requirement is unmet; once conseguimento saturates the bound degrades to the current
// frequenza, which is still a true (if loose) "at most this" reading.
private fun ClassroomAttendance.minRequiredPercentage(): Int? {
    if (requirementProgressPercentage <= 0.0) return null
    return (attendancePercentage / requirementProgressPercentage * 100.0).roundToInt()
}

// The hero of a tab: a big wavy ring with the headline value at its centre, in the same
// expressive language as the percorso editorial header but scaled up to own the page.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AttendanceRing(
    progress: Float,
    headline: String,
    caption: String,
    color: Color,
    footnote: String? = null,
) {
    // A thicker stroke than the default so the ring reads as a border at this size.
    val density = LocalDensity.current
    val stroke = remember(density) {
        Stroke(width = with(density) { 10.dp.toPx() }, cap = StrokeCap.Round)
    }
    Box(contentAlignment = Alignment.Center) {
        CircularWavyProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.size(220.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            stroke = stroke,
            trackStroke = stroke,
            // Keep the wave even at full progress — the default amplitude flattens the ring
            // to a plain circle at 100%, which loses the expressive border.
            amplitude = { 1f },
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = headline,
                style = MaterialTheme.typography.displaySmallEmphasized,
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            footnote?.let {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// App-wide empty-state style, filling the tab's fixed height so the pager stays stable.
@Composable
private fun TabEmptyState(title: String, body: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(TAB_BODY_HEIGHT),
    ) {
        EmptyState(
            icon = Icons.Outlined.EventBusy,
            title = title,
            body = body,
        )
    }
}
