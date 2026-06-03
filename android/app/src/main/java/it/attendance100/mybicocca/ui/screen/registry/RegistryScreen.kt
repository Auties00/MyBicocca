package it.attendance100.mybicocca.ui.screen.registry

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.FactCheck
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.outlined.Euro
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.EventSeat
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.OpenInFull
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.exam.BookedExam
import it.attendance100.mybicocca.domain.model.exam.ExamCall
import it.attendance100.mybicocca.domain.model.exam.ExamCallKey
import it.attendance100.mybicocca.domain.model.exam.ExamCallType
import it.attendance100.mybicocca.domain.model.exam.ExamEnrollmentWindow
import it.attendance100.mybicocca.domain.model.tax.InvoiceId
import it.attendance100.mybicocca.domain.model.tax.TaxInvoice
import it.attendance100.mybicocca.domain.model.tax.TaxStatus
import it.attendance100.mybicocca.ui.screen.registry.subscreen.bookableExams.BookableExamsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.bookedExams.BookedExamsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.TaxesViewModel
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Composable
fun Registry(
    bookedExamsViewModel: BookedExamsViewModel,
    bookableExamsViewModel: BookableExamsViewModel,
    taxesViewModel: TaxesViewModel,
    onOpenBookedExams: () -> Unit,
    onOpenTaxes: () -> Unit,
    onOpenExamResults: () -> Unit,
    onOpenStudyPlan: () -> Unit,
    onOpenQuestionnaires: () -> Unit,
    onOpenReservations: () -> Unit,
    onOpenAttendance: () -> Unit,
    onOpenInternships: () -> Unit,
    onOpenSelfCertificates: () -> Unit,
    onOpenDegreeAward: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    onProvideFilterToggle: ((() -> Unit)?) -> Unit = {},
) {
    val bookings by bookedExamsViewModel.bookings.collectAsStateWithLifecycle()
    val examCalls by bookableExamsViewModel.examCalls.collectAsStateWithLifecycle()
    val invoices by taxesViewModel.invoices.collectAsStateWithLifecycle()

    Registry(
        bookings = bookings,
        examCalls = examCalls,
        invoices = invoices,
        onOpenBookedExams = onOpenBookedExams,
        onOpenTaxes = onOpenTaxes,
        onOpenExamResults = onOpenExamResults,
        onOpenStudyPlan = onOpenStudyPlan,
        onOpenQuestionnaires = onOpenQuestionnaires,
        onOpenReservations = onOpenReservations,
        onOpenAttendance = onOpenAttendance,
        onOpenInternships = onOpenInternships,
        onOpenSelfCertificates = onOpenSelfCertificates,
        onOpenDegreeAward = onOpenDegreeAward,
        modifier = modifier,
        isActive = isActive,
        onProvideFilterToggle = onProvideFilterToggle,
    )
}

@Composable
private fun Registry(
    bookings: Loadable<List<BookedExam>>,
    examCalls: Loadable<List<ExamCall>>,
    invoices: Loadable<List<TaxInvoice>>,
    onOpenBookedExams: () -> Unit,
    onOpenTaxes: () -> Unit,
    onOpenExamResults: () -> Unit,
    onOpenStudyPlan: () -> Unit,
    onOpenQuestionnaires: () -> Unit,
    onOpenReservations: () -> Unit,
    onOpenAttendance: () -> Unit,
    onOpenInternships: () -> Unit,
    onOpenSelfCertificates: () -> Unit,
    onOpenDegreeAward: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    onProvideFilterToggle: ((() -> Unit)?) -> Unit = {},
) {
    LaunchedEffect(isActive) { if (isActive) onProvideFilterToggle(null) }

    val scheme = MaterialTheme.colorScheme

    // Derive dynamic subtitles / badges from the loaded data.
    val bookedCount = bookings.valueOrNull()?.size
    val availableCount = examCalls.valueOrNull()?.size
    val examSubtitle = when {
        bookedCount == null -> "Appelli, esiti e verbali"
        availableCount != null && availableCount > 0 -> "$bookedCount prenotati · $availableCount disponibili"
        else -> "$bookedCount appelli prenotati"
    }
    val examBadge = availableCount?.takeIf { it > 0 }?.let { "$it disp." }

    val invoiceList = invoices.valueOrNull()
    val expiredCount = invoiceList?.count { it.status == TaxStatus.EXPIRED } ?: 0
    val pendingCount = invoiceList?.count { it.status == TaxStatus.PENDING } ?: 0
    val taxSubtitle = when {
        invoiceList == null -> "Pagamenti ed esoneri"
        expiredCount > 0 -> if (expiredCount == 1) "1 pagamento in ritardo" else "$expiredCount pagamenti in ritardo"
        pendingCount > 0 -> if (pendingCount == 1) "1 tassa da pagare" else "$pendingCount tasse da pagare"
        else -> "Pagamenti ed esoneri"
    }
    val taxBadge = when {
        expiredCount > 0 -> "In scadenza" to ServiceBadge.Warning
        pendingCount > 0 -> "Da pagare" to ServiceBadge.Attention
        else -> null
    }

    val scadenzeSubtitle = when {
        expiredCount > 0 -> "$expiredCount in ritardo · controlla subito"
        (bookedCount ?: 0) > 0 -> "$bookedCount appelli in arrivo"
        else -> "Nessuna scadenza imminente"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        ScadenzeHero(
            subtitle = scadenzeSubtitle,
            urgent = expiredCount > 0,
            onClick = onOpenBookedExams,
        )

        CategorySection(
            accentContainer = scheme.primaryContainer,
            accentOnContainer = scheme.onPrimaryContainer,
            title = "Didattica",
            subtitle = "Il tuo percorso accademico",
            items = listOf(
                ServiceItem(
                    icon = Icons.AutoMirrored.Rounded.MenuBook,
                    title = "Piano di studi",
                    subtitle = "Percorso e crediti",
                    badge = "Modificabile",
                    badgeStyle = ServiceBadge.Positive,
                    onClick = onOpenStudyPlan,
                ),
                ServiceItem(
                    icon = Icons.Outlined.EventAvailable,
                    title = "Appelli",
                    subtitle = examSubtitle,
                    badge = examBadge,
                    badgeStyle = ServiceBadge.Attention,
                    onClick = onOpenBookedExams,
                ),
                ServiceItem(
                    icon = Icons.AutoMirrored.Rounded.FactCheck,
                    title = "Esami & libretto",
                    subtitle = "Esiti e verbali",
                    onClick = onOpenExamResults,
                ),
                ServiceItem(
                    icon = Icons.Rounded.Checklist,
                    title = "Presenze",
                    subtitle = "Frequenze e rilevazioni",
                    onClick = onOpenAttendance,
                ),
                ServiceItem(
                    icon = Icons.AutoMirrored.Rounded.Assignment,
                    title = "Questionari",
                    subtitle = "Valutazione didattica",
                    onClick = onOpenQuestionnaires,
                ),
            ),
        )

        CategorySection(
            accentContainer = scheme.tertiaryContainer,
            accentOnContainer = scheme.onTertiaryContainer,
            title = "Tasse & documenti",
            subtitle = "La tua posizione amministrativa",
            items = listOf(
                ServiceItem(
                    icon = Icons.Outlined.Euro,
                    title = "Tasse & agevolazioni",
                    subtitle = taxSubtitle,
                    badge = taxBadge?.first,
                    badgeStyle = taxBadge?.second ?: ServiceBadge.None,
                    onClick = onOpenTaxes,
                ),
                ServiceItem(
                    icon = Icons.Rounded.Description,
                    title = "Documenti & tessera",
                    subtitle = "Tessera, titoli e certificati",
                    onClick = onOpenSelfCertificates,
                ),
            ),
        )

        CategorySection(
            accentContainer = scheme.secondaryContainer,
            accentOnContainer = scheme.onSecondaryContainer,
            title = "Procedure & opportunità",
            subtitle = "Esperienze e nuove occasioni",
            items = listOf(
                ServiceItem(
                    icon = Icons.Rounded.Work,
                    title = "Tirocini",
                    subtitle = "Aziende e progetti formativi",
                    onClick = onOpenInternships,
                ),
                ServiceItem(
                    icon = Icons.Rounded.EventSeat,
                    title = "Prenotazione aule",
                    subtitle = "Posti e spazi studio",
                    onClick = onOpenReservations,
                ),
                ServiceItem(
                    icon = Icons.Rounded.School,
                    title = "Conseguimento titolo",
                    subtitle = "Domanda di laurea",
                    onClick = onOpenDegreeAward,
                ),
            ),
        )
    }
}

private enum class ServiceBadge { None, Positive, Warning, Attention }

private data class ServiceItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val badge: String? = null,
    val badgeStyle: ServiceBadge = ServiceBadge.None,
    val onClick: () -> Unit,
)

@Composable
private fun ScadenzeHero(
    subtitle: String,
    urgent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val container = if (urgent) scheme.errorContainer else Color(0xff951533)
    val onContainer = if (urgent) scheme.onErrorContainer else scheme.onPrimary
    val accentOnContainer = scheme.onSecondaryContainer

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = container,
        contentColor = onContainer,
        shape = RoundedCornerShape(28.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconBadge(
                icon = Icons.Rounded.NotificationsActive,
                background = onContainer.copy(alpha = 0.18f),
                tint = accentOnContainer,
                size = 50.dp,
                cornerRadius = 16.dp,
                iconSize = 26.dp,
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Scadenze",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = accentOnContainer,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = accentOnContainer.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Rounded.OpenInFull,
                contentDescription = null,
                tint = accentOnContainer.copy(alpha = 0.9f),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun CategorySection(
    accentContainer: Color,
    accentOnContainer: Color,
    title: String,
    subtitle: String,
    items: List<ServiceItem>,
    modifier: Modifier = Modifier,
) {
    val large = 20.dp
    val small = 4.dp
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        // The title/subtitle is the first segment of the group: a plain tile
        // without the leading icon or the trailing chevron.
        CategoryHeaderTile(
            title = title,
            subtitle = subtitle,
            shape = RoundedCornerShape(
                topStart = large,
                topEnd = large,
                bottomStart = small,
                bottomEnd = small,
            ),
        )
        items.forEachIndexed { index, item ->
            val isLast = index == items.lastIndex
            SegmentedTile(
                item = item,
                accentContainer = accentContainer,
                accentOnContainer = accentOnContainer,
                shape = RoundedCornerShape(
                    topStart = small,
                    topEnd = small,
                    bottomStart = if (isLast) large else small,
                    bottomEnd = if (isLast) large else small,
                ),
            )
        }
    }
}

@Composable
private fun CategoryHeaderTile(
    title: String,
    subtitle: String,
    shape: Shape,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        shape = shape,
    ) {
        Column(
            modifier = Modifier.padding(start = 18.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SegmentedTile(
    item: ServiceItem,
    accentContainer: Color,
    accentOnContainer: Color,
    shape: Shape,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = item.onClick,
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        shape = shape,
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconBadge(
                icon = item.icon,
                background = accentContainer,
                tint = accentOnContainer,
                size = 44.dp,
                cornerRadius = 14.dp,
                iconSize = 22.dp,
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (item.badge != null) {
                Spacer(Modifier.width(10.dp))
                StatusBadge(text = item.badge, style = item.badgeStyle)
            }
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = scheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun StatusBadge(
    text: String,
    style: ServiceBadge,
) {
    val scheme = MaterialTheme.colorScheme
    val background: Color
    val foreground: Color
    when (style) {
        ServiceBadge.Positive -> {
            background = scheme.tertiaryContainer
            foreground = scheme.onTertiaryContainer
        }
        ServiceBadge.Warning -> {
            background = scheme.errorContainer
            foreground = scheme.onErrorContainer
        }
        ServiceBadge.Attention -> {
            background = scheme.secondaryContainer
            foreground = scheme.onSecondaryContainer
        }
        ServiceBadge.None -> {
            background = scheme.surfaceVariant
            foreground = scheme.onSurfaceVariant
        }
    }
    Box(
        modifier = Modifier
            .background(background, RoundedCornerShape(percent = 50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = foreground,
            maxLines = 1,
        )
    }
}

@Composable
private fun IconBadge(
    icon: ImageVector,
    background: Color,
    tint: Color,
    size: androidx.compose.ui.unit.Dp,
    cornerRadius: androidx.compose.ui.unit.Dp,
    iconSize: androidx.compose.ui.unit.Dp,
) {
    Box(
        modifier = Modifier
            .size(size)
            .background(background, RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(iconSize),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0606, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NewRegistryLayoutPreview() {
    BicoccaTheme(dark = true) {
        Registry(
            bookings = Loadable.Loaded(
                listOf(
                    BookedExam(
                        key = ExamCallKey(1, 1, 1),
                        applicationListId = 1,
                        studentId = 1,
                        activityChoiceId = 1,
                        activityDescription = "Sistemi Operativi",
                        examCallDescription = "Appello Ordinario",
                        examDateTime = LocalDateTime.now().plusDays(5),
                        classroomDescription = "Aula G24",
                        buildingDescription = "Edificio U6",
                        position = 1,
                        bookingDate = LocalDateTime.now(),
                        studentNote = null
                    )
                )
            ),
            examCalls = Loadable.Loaded(
                listOf(
                    ExamCall(
                        key = ExamCallKey(1, 1, 2),
                        examCallId = 2,
                        activityChoiceId = 2,
                        activityCode = "SO",
                        activityDescription = "Sistemi Operativi",
                        courseOfStudyDescription = "Informatica",
                        callDescription = "Appello Straordinario",
                        callDate = LocalDate.now().plusDays(10),
                        callTime = LocalTime.of(10, 0),
                        enrollmentWindow = ExamEnrollmentWindow(
                            LocalDate.now(),
                            LocalDate.now().plusDays(9)
                        ),
                        enrolledNumber = 10,
                        state = "P",
                        stateDescription = "Prenotabile",
                        callType = ExamCallType.Final,
                        isReserved = false,
                        matId = 1
                    )
                )
            ),
            invoices = Loadable.Loaded(
                listOf(
                    TaxInvoice(
                        id = InvoiceId(1L),
                        academicYear = 2023,
                        title = "Prima Rata",
                        amount = 156.0,
                        paidAmount = 156.0,
                        status = TaxStatus.PAID,
                        issueDate = LocalDate.now().minusMonths(6),
                        expiration = LocalDate.now().minusMonths(5),
                        paymentDate = LocalDate.now().minusMonths(5),
                        pagoPaEnabled = true,
                        pagoPaImmediate = true,
                        pagoPaNotice = true,
                        iuv = "123",
                        noticeCode = "456",
                        items = emptyList()
                    )
                )
            ),
            onOpenBookedExams = {},
            onOpenTaxes = {},
            onOpenExamResults = {},
            onOpenStudyPlan = {},
            onOpenQuestionnaires = {},
            onOpenReservations = {},
            onOpenAttendance = {},
            onOpenInternships = {},
            onOpenSelfCertificates = {},
            onOpenDegreeAward = {},
        )
    }
}
