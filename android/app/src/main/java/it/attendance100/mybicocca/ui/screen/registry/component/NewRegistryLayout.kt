package it.attendance100.mybicocca.ui.screen.registry.component

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.FactCheck
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Euro
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.EventSeat
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.exam.BookedExam
import it.attendance100.mybicocca.domain.model.exam.ExamCall
import it.attendance100.mybicocca.domain.model.exam.ExamCallKey
import it.attendance100.mybicocca.domain.model.exam.ExamCallType
import it.attendance100.mybicocca.domain.model.exam.ExamEnrollmentWindow
import it.attendance100.mybicocca.domain.model.exam.ExamType
import it.attendance100.mybicocca.domain.model.tax.InvoiceId
import it.attendance100.mybicocca.domain.model.tax.TaxInvoice
import it.attendance100.mybicocca.domain.model.tax.TaxStatus
import it.attendance100.mybicocca.domain.model.transcript.TranscriptStats
import it.attendance100.mybicocca.ui.screen.profile.ProfileViewModel
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryAccentShape
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryCategory
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryDashboardTile
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryTileStatus
import it.attendance100.mybicocca.ui.screen.registry.state.materialShape
import it.attendance100.mybicocca.ui.screen.registry.subscreen.bookableExams.BookableExamsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.bookedExams.BookedExamsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.TaxesViewModel
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Composable
fun NewRegistryLayout(
    profileViewModel: ProfileViewModel,
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

    val account by profileViewModel.account.collectAsStateWithLifecycle()
    val stats by profileViewModel.stats.collectAsStateWithLifecycle()
    val photoFile by profileViewModel.photoFile.collectAsStateWithLifecycle()

    NewRegistryLayout(
        account = account,
        stats = stats,
        photoFile = photoFile,
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
fun NewRegistryLayout(
    account: Account?,
    stats: Loadable<TranscriptStats>,
    photoFile: java.io.File?,
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

    val carrieraInteraction = remember { MutableInteractionSource() }
    val pressed by carrieraInteraction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "heroPressScale",
    )

    val bookedCount = bookings.valueOrNull()?.size
    val availableCount = examCalls.valueOrNull()?.size
    val examSubtitle = when {
        bookedCount == null -> "Gestisci le iscrizioni agli appelli"
        availableCount != null && availableCount > 0 -> "$bookedCount prenotati · $availableCount disponibili"
        else -> "$bookedCount appelli prenotati"
    }

    val invoiceList = invoices.valueOrNull()
    val expiredCount = invoiceList?.count { it.status == TaxStatus.EXPIRED } ?: 0
    val pendingCount = invoiceList?.count { it.status == TaxStatus.PENDING } ?: 0
    val taxSubtitle = when {
        invoiceList == null -> "Visualizza e paga le tasse"
        expiredCount > 0 -> if (expiredCount == 1) "1 pagamento in ritardo" else "$expiredCount pagamenti in ritardo"
        pendingCount > 0 -> if (pendingCount == 1) "1 tassa da pagare" else "$pendingCount tasse da pagare"
        else -> "Tutto in regola"
    }
    val taxStatus = if (expiredCount > 0) RegistryTileStatus.Warning else RegistryTileStatus.Normal

    val examsHero = RegistryDashboardTile(
        id = "exams",
        title = "Appelli",
        subtitle = examSubtitle,
        icon = Icons.Outlined.EventAvailable,
        shape = RegistryAccentShape.Sunny,
        category = RegistryCategory.Exams,
        onClick = onOpenBookedExams,
        mirrored = false,
    )

    val taxesHero = RegistryDashboardTile(
        id = "taxes",
        title = "Tasse",
        subtitle = taxSubtitle,
        status = taxStatus,
        icon = Icons.Outlined.Euro,
        shape = RegistryAccentShape.Cookie6,
        category = RegistryCategory.Taxes,
        onClick = onOpenTaxes,
        mirrored = true,
    )

    val scheme = MaterialTheme.colorScheme
    // Pick a decorative shape once, so it stays stable across recompositions.
    val carrieraShape = remember { RegistryAccentShape.entries.random() }.materialShape()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
//        CarrieraTile(
//            onClick = onOpenStudyPlan,
//            carrieraInteraction = carrieraInteraction,
//            scale = scale,
//            account = account,
//            stats = stats,
//            photoFile = photoFile,
//            shape = carrieraShape,
//            container = scheme.primaryContainer,
//            onContainer = scheme.onPrimaryContainer,
//            shapeFill = scheme.surface,
//            shapeIconTint = scheme.primary,
//            modifier = Modifier.height(220.dp).padding(start = 16.dp, end = 16.dp, top = 32.dp),
//        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            RegistryHeroTile(
                tile = examsHero,
                layout = RegistryHeroLayout.Portrait,
                modifier = Modifier
                    .height(200.dp)
                    .weight(0.5f),
            )
            RegistryHeroTile(
                tile = taxesHero,
                layout = RegistryHeroLayout.Portrait,
                modifier = Modifier
                    .height(200.dp)
                    .weight(0.5f),
            )
        }

        ServicesSection(
            onOpenExamResults = onOpenExamResults,
            onOpenStudyPlan = onOpenStudyPlan,
            onOpenQuestionnaires = onOpenQuestionnaires,
            onOpenReservations = onOpenReservations,
            onOpenAttendance = onOpenAttendance,
            onOpenInternships = onOpenInternships,
            onOpenSelfCertificates = onOpenSelfCertificates,
            onOpenDegreeAward = onOpenDegreeAward,
        )
    }
}

@Composable
private fun ServicesSection(
    onOpenExamResults: () -> Unit,
    onOpenStudyPlan: () -> Unit,
    onOpenQuestionnaires: () -> Unit,
    onOpenReservations: () -> Unit,
    onOpenAttendance: () -> Unit,
    onOpenInternships: () -> Unit,
    onOpenSelfCertificates: () -> Unit,
    onOpenDegreeAward: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = scheme.surface,
        contentColor = scheme.onSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 12.dp, top = 24.dp, bottom = 16.dp),
        ) {
            Text(
                text = "Servizi",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            ServiceRow(Icons.AutoMirrored.Rounded.FactCheck, "Esiti esami", onOpenExamResults)
            ServiceRow(Icons.AutoMirrored.Rounded.MenuBook, "Piano di studi", onOpenStudyPlan)
            ServiceRow(Icons.AutoMirrored.Rounded.Assignment, "Questionari", onOpenQuestionnaires)
            ServiceRow(Icons.Rounded.EventSeat, "Prenotazione aule", onOpenReservations)
            ServiceRow(Icons.Rounded.Checklist, "Presenze", onOpenAttendance)
            ServiceRow(Icons.Rounded.Work, "Tirocini", onOpenInternships)
            ServiceRow(Icons.Rounded.Description, "Autocertificazioni", onOpenSelfCertificates)
            ServiceRow(Icons.Rounded.School, "Conseguimento titolo", onOpenDegreeAward)
        }
    }
}

@Composable
private fun ServiceRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(scheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = scheme.onPrimaryContainer,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = scheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = scheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
fun CarrieraTile(
    onClick: () -> Unit = {},
    carrieraInteraction: MutableInteractionSource,
    scale: Float,
    account: Account?,
    stats: Loadable<TranscriptStats>,
    photoFile: java.io.File?,
    shape: Shape,
    container: Color,
    onContainer: Color,
    shapeFill: Color,
    shapeIconTint: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        interactionSource = carrieraInteraction,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        color = container,
        contentColor = onContainer,
        shape = RoundedCornerShape(
            topStart = 36.dp,
            topEnd = 18.dp,
            bottomStart = 18.dp,
            bottomEnd = 36.dp,
        ),
    ) {
        Box(Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 30.dp)
                    .size(180.dp)
                    .graphicsLayer { rotationZ = -8f }
                    .clip(shape)
                    .background(onContainer.copy(alpha = 0.14f)),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 20.dp)
                    .size(98.dp)
                    .graphicsLayer { rotationZ = 12f }
                    .clip(shape)
                    .background(shapeFill),
                contentAlignment = Alignment.Center,
            ) {
                val matrix = remember { ColorMatrix().apply { setToSaturation(0f) } }
                if (photoFile != null) {
                    AsyncImage(
                        model = photoFile,
                        contentDescription = "Foto profilo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center,
                        colorFilter = ColorFilter.colorMatrix(matrix),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Foto non disponibile",
                        tint = shapeIconTint,
                        modifier = Modifier.size(40.dp),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 22.dp, end = 140.dp),
            ) {
                Text(
                    text = account?.displayName ?: "Carriera",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = onContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                val transcript = stats.valueOrNull()
                Spacer(Modifier.height(18.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    CareerStat(
                        value = transcript?.weightedAverage?.let { "%.1f".format(it) } ?: "–",
                        label = "Media",
                        onContainer = onContainer,
                    )
                    Box(
                        modifier = Modifier
                            .height(34.dp)
                            .width(1.dp)
                            .background(onContainer.copy(alpha = 0.22f)),
                    )
                    CareerStat(
                        value = transcript?.let {
                            "${it.passedCredits.toInt()}/${it.totalCreditsRequired.toInt()}"
                        } ?: "–",
                        label = "CFU",
                        onContainer = onContainer,
                    )
                }
            }
        }
    }
}

@Composable
private fun CareerStat(
    value: String,
    label: String,
    onContainer: Color,
) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = onContainer,
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = onContainer.copy(alpha = 0.7f),
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF0F0606,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun NewRegistryLayoutPreview() {
    val careerId = CareerId(1L)
    BicoccaTheme(dark = true) {
        NewRegistryLayout(
            account = Account(
                id = AccountId("user123"),
                username = "m.rossi@campus.unimib.it",
                displayName = "Mario Rossi",
                academic = AcademicIdentity(
                    recordUserId = "record123",
                    personId = 1L,
                    fiscalCode = "RSSMRA80A01H501A",
                    careers = emptyList(),
                    selectedCareerId = careerId
                ),
                learning = LearningIdentity(
                    lmsUserId = 1,
                    lmsUsername = "mrossi",
                    locale = "it",
                    isSiteAdmin = false,
                    maxUploadFileSizeBytes = 1024L,
                    storageQuotaBytes = 1024L
                ),
                createdAt = Instant.now(),
                lastUsedAt = Instant.now(),
                lastSyncedAt = Instant.now()
            ),
            stats = Loadable.Loaded(
                TranscriptStats(
                    careerId = careerId,
                    passedCredits = 120f,
                    totalCreditsRequired = 180f,
                    arithmeticAverage = 27.5f,
                    weightedAverage = 28.2f,
                    passedExamCount = 15,
                    plannedExamCount = 20,
                    maxGrade = 30,
                    cumLaudeAvailable = true
                )
            ),
            photoFile = null,
            bookings = Loadable.Loaded(
                listOf(
                    BookedExam(
                        key = ExamCallKey(1, 1, 1),
                        applicationListId = 1,
                        studentId = 1,
                        activityChoiceId = 1,
                        activityDescription = "Sistemi Operativi",
                        examCallDescription = "Appello Ordinario",
                        examType = ExamType.Written,
                        callType = ExamCallType.Final,
                        examDateTime = LocalDateTime.now().plusDays(5),
                        classroomDescription = "Aula G24",
                        buildingDescription = "Edificio U6",
                        credits = 8f,
                        examModeDescription = "Esame in Presenza",
                        position = 1,
                        bookingDate = LocalDateTime.now(),
                        cancellableUntil = LocalDate.now().plusDays(3),
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
