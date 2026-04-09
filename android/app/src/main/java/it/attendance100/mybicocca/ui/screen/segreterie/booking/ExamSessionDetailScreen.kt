package it.attendance100.mybicocca.ui.screen.segreterie.booking

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import it.attendance100.mybicocca.data.model.exam.ExamCall
import it.attendance100.mybicocca.ui.component.SingleActionBottomBar
import it.attendance100.mybicocca.ui.component.card.SimpleCard
import it.attendance100.mybicocca.ui.navigation.LocalSharedTransitionScope
import it.attendance100.mybicocca.util.rememberHapticManager
import it.attendance100.mybicocca.util.shared_transitions.CommonSharedElementKey
import it.attendance100.mybicocca.util.shared_transitions.CommonSharedElementType
import it.attendance100.mybicocca.util.shared_transitions.ExamSessionSharedElementType
import it.attendance100.mybicocca.util.shared_transitions.ExamSessionsElementKey
import it.attendance100.mybicocca.util.shared_transitions.bicoccaSharedElement
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamSessionDetailScreen(
    sessionId: Long,
    viewModel: BookingViewModel = hiltViewModel(
        checkNotNull<ViewModelStoreOwner>(
            LocalViewModelStoreOwner.current
        ) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null
    ),
) {
    val examCall by viewModel.selectedExamCall.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingDetail.collectAsStateWithLifecycle()
    val error by viewModel.selectedExamError.collectAsStateWithLifecycle()

    val haptic = rememberHapticManager()

    // Bottom sheet state
    var showBookingSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    val locale = Locale.getDefault()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", locale) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm", locale) }
    val shortDateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy", locale) }

    // Load data
    LaunchedEffect(sessionId) {
        viewModel.loadExamCallById(sessionId)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        SimpleCard(
            modifier = Modifier
                .fillMaxSize()
                .bicoccaSharedElement(
                    key = ExamSessionsElementKey(
                        sessionId.toString(),
                        ExamSessionSharedElementType.Card
                    ),
                    zIndexInOverlay = -100f
                ),
            shape = RectangleShape,
            ditherImage = null,
            onClick = null,
        ) {
            when {
                examCall == null && isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                error != null && examCall == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp),
                        ) {
                            Icon(
                                Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = error ?: "Errore",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }

                examCall != null -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Scrollable content inside the card
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(bottom = 100.dp, top = 12.dp),
                        ) {
                            ExamSessionContent(
                                examCall = examCall!!,
                                dateFormatter = dateFormatter,
                                timeFormatter = timeFormatter,
                                shortDateFormatter = shortDateFormatter,
                            )
                        }
                    }
                }
            }
        }

        with(LocalSharedTransitionScope.current!!) {
            SingleActionBottomBar(
                text = "Prenota Appello",
                icon = Icons.Default.EventAvailable,
                onClick = {
                    haptic.tap()
                    showBookingSheet = true
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .renderInSharedTransitionScopeOverlay(100f)
                    .bicoccaSharedElement(
                        key = CommonSharedElementKey(
                            CommonSharedElementKey.EXAM_SESSIONS_KEY,
                            CommonSharedElementType.BottomActionBar
                        )
                    ),
            )
        }
    }

    // Booking Bottom Sheet
    if (showBookingSheet && examCall != null) {
        ModalBottomSheet(
            onDismissRequest = { showBookingSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            BookingConfirmationSheet(
                examCall = examCall!!,
                dateFormatter = dateFormatter,
                timeFormatter = timeFormatter,
                onConfirm = {
                    // TODO: Implement actual booking
                    showBookingSheet = false
                },
                onDismiss = {
                    showBookingSheet = false
                },
            )
        }
    }
}


@Composable
private fun ExamSessionContent(
    examCall: ExamCall,
    dateFormatter: DateTimeFormatter,
    timeFormatter: DateTimeFormatter,
    shortDateFormatter: DateTimeFormatter,
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
    ) {
        // Title
        Text(
            text = examCall.activityName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = primaryColor,
            modifier = Modifier
                .padding(end = 38.dp)
                .fillMaxWidth()
                .bicoccaSharedElement(
                    key = ExamSessionsElementKey(
                        examCall.id.toString(),
                        ExamSessionSharedElementType.Title
                    ),
                ),
        )

        // Activity code as subtitle if different from name
        examCall.activityCode?.let { code ->
            val displayDescription = cleanDescription(examCall.activityName, code)
            if (displayDescription != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = displayDescription,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick info chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val dateText = examCall.date?.format(
                DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault())
            )
            val timeText = examCall.startTime?.format(
                DateTimeFormatter.ofPattern("HH:mm")
            )
            val dateTimeChipText = listOfNotNull(dateText, timeText).joinToString(" ")

            if (dateTimeChipText.isNotBlank()) {
                InfoChip(
                    icon = Icons.Outlined.CalendarMonth,
                    text = dateTimeChipText,
                )
            }
            val building = examCall.building
            if (!building.isNullOrBlank()) {
                InfoChip(
                    icon = Icons.Outlined.LocationOn,
                    text = building.take(15),
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Extra content with slide-in animation
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow,
                )
            ) { it } + fadeIn(),
            exit = slideOutVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium,
                )
            ) { it } + fadeOut(),
        ) {
            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(20.dp))

                // Date & Time Section
                SectionHeader(title = "Data e Ora", icon = Icons.Outlined.Event)
                Spacer(modifier = Modifier.height(12.dp))

                examCall.date?.let { date ->
                    DetailItem(
                        icon = Icons.Outlined.CalendarMonth,
                        label = "Data",
                        value = date.format(dateFormatter).replaceFirstChar { it.uppercase() },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                examCall.startTime?.let { startTime ->
                    val timeDisplay = examCall.endTime?.let { endTime ->
                        "${startTime.format(timeFormatter)} - ${endTime.format(timeFormatter)}"
                    } ?: startTime.format(timeFormatter)
                    DetailItem(
                        icon = Icons.Outlined.AccessTime,
                        label = "Ora",
                        value = timeDisplay,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Location Section
                if (!examCall.building.isNullOrBlank() || !examCall.room.isNullOrBlank()) {
                    SectionHeader(title = "Luogo", icon = Icons.Outlined.LocationOn)
                    Spacer(modifier = Modifier.height(12.dp))

                    examCall.building?.let { building ->
                        DetailItem(
                            icon = Icons.Outlined.Business,
                            label = "Edificio",
                            value = building,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    examCall.room?.let { room ->
                        DetailItem(
                            icon = Icons.Outlined.MeetingRoom,
                            label = "Aula",
                            value = room,
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Exam Details Section
                SectionHeader(
                    title = "Dettagli Esame",
                    icon = Icons.AutoMirrored.Outlined.Assignment
                )
                Spacer(modifier = Modifier.height(12.dp))

                examCall.stateDescription?.let { state ->
                    DetailItem(
                        icon = Icons.Outlined.Category,
                        label = "Stato",
                        value = state,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                DetailItem(
                    icon = Icons.Outlined.Groups,
                    label = "Modalità",
                    value = "In Presenza",
                )
                Spacer(modifier = Modifier.height(8.dp))

                examCall.activityCode?.let { code ->
                    DetailItem(
                        icon = Icons.Outlined.Description,
                        label = "Codice",
                        value = code,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                examCall.enrolledCount?.let { count ->
                    DetailItem(
                        icon = Icons.Outlined.People,
                        label = "Iscritti",
                        value = "$count studenti",
                        valueColor = primaryColor,
                    )
                }

                // Teachers Section (from examinerEmails)
                val emails = examCall.examinerEmails
                if (!emails.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))

                    SectionHeader(title = "Docenti", icon = Icons.Outlined.School)
                    Spacer(modifier = Modifier.height(12.dp))

                    emails.forEach { email ->
                        val displayName = email.substringBefore("@")
                            .replace(".", " ")
                            .split(" ")
                            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = displayName.split(" ")
                                            .mapNotNull { it.firstOrNull()?.uppercase() }
                                            .take(2)
                                            .joinToString(""),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Registration Period Section
                if (examCall.enrollmentStartDate != null || examCall.enrollmentEndDate != null) {
                    SectionHeader(title = "Periodo Iscrizione", icon = Icons.Outlined.DateRange)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Opening card
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            ),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Icon(
                                    Icons.Outlined.LockOpen,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp),
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Apertura",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = examCall.enrollmentStartDate ?: "-",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }

                        // Closing card
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            ),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Icon(
                                    Icons.Outlined.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(24.dp),
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Chiusura",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = examCall.enrollmentEndDate ?: "-",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // State description as notes if available
                examCall.stateDescription?.let { state ->
                    SectionHeader(title = "Note", icon = Icons.AutoMirrored.Outlined.Notes)
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(
                                alpha = 0.5f
                            ),
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Icon(
                                Icons.Outlined.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = state,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}


@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}


@Composable
private fun DetailItem(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(100.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = valueColor,
        )
    }
}


@Composable
private fun BookingConfirmationSheet(
    examCall: ExamCall,
    dateFormatter: DateTimeFormatter,
    timeFormatter: DateTimeFormatter,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
    ) {
        // Title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.EventAvailable,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(28.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Conferma Prenotazione",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Exam summary card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = examCall.activityName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = "Data",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = examCall.date?.format(dateFormatter)
                                ?.replaceFirstChar { it.uppercase() }
                                ?: "-",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Ora",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = examCall.startTime?.format(timeFormatter) ?: "-",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (!examCall.building.isNullOrBlank() || !examCall.room.isNullOrBlank()) {
                    Column {
                        Text(
                            text = "Luogo",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = listOfNotNull(examCall.building, examCall.room)
                                .joinToString(" - "),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Warning text
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                Icons.Outlined.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "La prenotazione e vincolante. Ricorda di cancellarla se non puoi presentarti all'esame.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
            ) {
                Text("Annulla")
            }

            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val cornerRadius by animateIntAsState(
                targetValue = if (isPressed) 30 else 50,
                label = "cornerRadius",
            )

            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(percent = cornerRadius),
                interactionSource = interactionSource,
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                ),
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Conferma", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}


private fun cleanDescription(name: String, description: String): String? {
    val n = name.lowercase().trim()
    val d = description.lowercase().trim()

    if (n == d || n.startsWith(d)) return null

    if (d.startsWith(n)) {
        val remaining = description.substring(name.length)
        val trimmed = remaining.dropWhile { !it.isLetterOrDigit() }
        return trimmed.ifBlank { null }
    }

    return description
}
