package it.attendance100.mybicocca.ui.screen.registry.subscreen.isee

import android.content.Context
import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.tax.IseeDeclaration
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.component.feedback.rememberMinDurationLoading
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import it.attendance100.mybicocca.ui.component.modal.SheetMessage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.TaxesViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.formatEuro
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.taxFriendlyMessage
import java.time.LocalDate

/**
 * The INPS "ISEE precompilato" service (SPID/CIE login) where the DSU is subscribed;
 * Bicocca then acquires the resulting ISEE automatically from the INPS database — Esse3
 * has no declaration form of its own.
 */
private const val INPS_ISEE_URL = "https://servizi2.inps.it/servizi/ISEEPrecompilato/home.aspx"

/**
 * Ateneo "Contribuzione studentesca" page, hosting the current year's Guida ISEE (the
 * per-year PDF URL rots, this page doesn't).
 */
private const val ISEE_GUIDE_URL =
    "https://www.unimib.it/studiare/servizi-studenti-e-laureati/segreterie/immatricolazione/contribuzione-studentesca"

/**
 * Root page of the ISEE sheet: the list of academic years carrying a declaration, newest
 * first, with a "Dichiara ISEE" footer when the current year can still be filed. The sheet
 * container, the pinned morphing header and the list -> detail transition are owned by
 * BottomSheetSceneStrategy; the year detail is a separate back-stack entry
 * (SheetRoute.IseeDetail rendering [IseeDetailPage]).
 *
 * Reads the hoisted TaxesViewModel — the same in-memory fetch as the Tasse page — so
 * opening it never re-hits the network; a re-open renders the cached snapshot instantly
 * while a refresh runs. Years whose ISEE is null (the 99999999 "not declared" sentinel is
 * erased in the mapper) are dropped, so a student who never filed an ISEE sees the empty
 * state.
 *
 * The declare footer shows only while the DSU window is open: the DSU for a.a. Y/Y+1 is
 * acquired automatically from the INPS database only when subscribed during calendar year
 * Y (per the Guida ISEE) — past 31/12 it becomes a mail-in procedure with penalties,
 * beyond what a button can start. The latest enrollment year says which a.a. is current;
 * its row exists even when undeclared (the sentinel), so the unfiltered list is the right
 * base for that check.
 */
@Composable
fun IseeDeclarationsPage(
    viewModel: TaxesViewModel,
    onOpenDetail: (year: Long) -> Unit,
) {
    val context = LocalContext.current
    val iseeState by viewModel.isee.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        if (viewModel.isee.value is Loadable.Loaded) viewModel.refresh()
    }

    val allDeclarations = iseeState.valueOrNull()
    val declarations = allDeclarations
        ?.filter { it.isee != null && it.academicYearEnrollmentId != null }
        ?.sortedByDescending { it.academicYearEnrollmentId ?: 0L }

    val latestYear = allDeclarations?.mapNotNull { it.academicYearEnrollmentId }?.maxOrNull()
    val currentYear = remember { LocalDate.now().year.toLong() }
    val declareAvailable = latestYear == currentYear &&
        declarations?.none { it.academicYearEnrollmentId == latestYear } == true

    DeclarationsPage(
        declarations = declarations,
        syncStatus = syncStatus,
        declareYear = latestYear.takeIf { declareAvailable },
        onRetry = viewModel::refresh,
        onOpenDetail = { decl -> decl.academicYearEnrollmentId?.let(onOpenDetail) },
        onDeclare = { context.openUrl(INPS_ISEE_URL) },
        onOpenGuide = { context.openUrl(ISEE_GUIDE_URL) },
    )
}

/** "3 anni accademici": how many years carry a declaration, in place of a sub-page title. */
fun iseeHeaderSubtitle(declarations: List<IseeDeclaration>): String? {
    val count = declarations.size
    if (count == 0) return null
    return if (count == 1) "1 anno accademico" else "$count anni accademici"
}

/**
 * Declarations list body. [declareYear] is non-null while the current academic year has no
 * declaration and the DSU window is open, which pins the declare footer beneath the list.
 * First-load failures render a retry page, loading holds a minimum-duration indicator so
 * quick fetches don't flash it, the full-screen EmptyState renders inside a bounded box so
 * it can't stretch the sheet to full height, and height changes animate so the modal
 * doesn't snap as content lands.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DeclarationsPage(
    declarations: List<IseeDeclaration>?,
    syncStatus: SyncStatus,
    declareYear: Long?,
    onRetry: () -> Unit,
    onOpenDetail: (IseeDeclaration) -> Unit,
    onDeclare: () -> Unit,
    onOpenGuide: () -> Unit,
) {
    val failure = syncStatus as? SyncStatus.Failed
    val showLoading = rememberMinDurationLoading(loading = declarations == null)
    val settled = declarations != null && !showLoading
    val showFooter = settled && failure == null && declareYear != null

    val motion = MaterialTheme.motionScheme
    val sizeSpec = remember(motion) { motion.defaultSpatialSpec<IntSize>() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = sizeSpec),
    ) {
        when {
            failure != null && declarations == null -> SheetMessage(
                icon = Icons.Outlined.CloudOff,
                title = stringResource(R.string.common_error_title),
                body = failure.cause.taxFriendlyMessage(LocalContext.current),
                action = {
                    val haptic =
                        rememberHapticManager(); RetryButton(onClick = { haptic.tap(); onRetry() })
                },
            )

            !settled -> SheetLoadingIndicator(label = stringResource(R.string.isee_loading))

            declarations.isEmpty() -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .padding(bottom = if (showFooter) 4.dp else 16.dp),
            ) {
                EmptyState(
                    icon = Icons.Outlined.Savings,
                    title = stringResource(R.string.isee_no_declarations),
                    body = stringResource(R.string.isee_no_declarations_body),
                )
            }

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = if (showFooter) 12.dp else 24.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                itemsIndexed(
                    items = declarations,
                    key = { _, declaration -> declaration.academicYearEnrollmentId ?: 0L },
                ) { index, declaration ->
                    DeclarationRow(
                        declaration = declaration,
                        isFirst = index == 0,
                        isLast = index == declarations.lastIndex,
                        onClick = { onOpenDetail(declaration) },
                    )
                }
            }
        }

        if (showFooter) {
            DeclareFooter(
                year = declareYear,
                onDeclare = onDeclare,
                onOpenGuide = onOpenGuide,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            )
        }
    }
}

/**
 * Connected expressive footer pair, same scheme as percorso's modifica/stampa, beneath a
 * caption naming the still-fileable academic year: the tonal "Guida" leads to the Ateneo
 * contribution page hosting the year's Guida ISEE, while the brand "Dichiara ISEE" trails
 * wide to the INPS DSU service — the declaration happens there; Esse3 has no form, and the
 * Ateneo pulls the result from the INPS database.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DeclareFooter(
    year: Long,
    onDeclare: () -> Unit,
    onOpenGuide: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val brandBg = if (dark) scheme.primaryContainer else scheme.primary
    val brandFg = if (dark) scheme.onPrimaryContainer else scheme.onPrimary

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.isee_declare_message, academicYearLabel(year)),
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            val haptic = rememberHapticManager()
            FilledTonalButton(
                onClick = { haptic.tap(); onOpenGuide() },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = ButtonGroupDefaults.connectedLeadingButtonShape,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = scheme.surfaceContainerHighest,
                    contentColor = scheme.onSurface,
                ),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.isee_guide), fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = { haptic.tap(); onDeclare() },
                modifier = Modifier
                    .weight(1.4f)
                    .height(56.dp),
                shape = ButtonGroupDefaults.connectedTrailingButtonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = brandBg,
                    contentColor = brandFg,
                ),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.isee_declare), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/**
 * Segmented group row like the buildings list: 28dp corners cap the group's ends, 6dp where
 * rows touch. A rectangular year tile leads, the academic-year title follows.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DeclarationRow(
    declaration: IseeDeclaration,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = if (isFirst) 28.dp else 6.dp,
            topEnd = if (isFirst) 28.dp else 6.dp,
            bottomStart = if (isLast) 28.dp else 6.dp,
            bottomEnd = if (isLast) 28.dp else 6.dp,
        ),
        color = scheme.surfaceContainer,
    ) {
        val haptic = rememberHapticManager()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = { haptic.tap(); onClick() })
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = RoundedCornerShape(12.dp), color = scheme.primaryContainer) {
                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = declaration.shortYearLabel(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onPrimaryContainer,
                    )
                }
            }
            Text(
                text = stringResource(R.string.isee_academic_year, declaration.academicYearLabel()),
                style = MaterialTheme.typography.titleMediumEmphasized,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * One year's declaration, as a headerless page inside the sheet pager: the morphing header
 * above carries the year and course line, a hero card carries the ISEE indicator itself,
 * and a details section lists the benefits threshold, course and exemption when present.
 */
@Composable
fun IseeDetailPage(
    declaration: IseeDeclaration,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 560.dp)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(scheme.surfaceContainerHigh)
                    .padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(R.string.isee_indicator),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp,
                    color = scheme.primary,
                )
                Text(
                    text = declaration.isee?.let { formatEuro(it) } ?: "—",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                    textAlign = TextAlign.Center,
                )
            }
        }

        item {
            DetailSection(
                title = stringResource(R.string.common_details),
                rows = buildList {
                    declaration.iseeThreshold?.let {
                        add(
                            stringResource(R.string.isee_threshold) to formatEuro(
                                it
                            )
                        )
                    }
                    declaration.courseDescription?.let { add(stringResource(R.string.isee_course) to it) }
                    declaration.exemptionDescription
                        ?.takeIf { it.isNotBlank() }
                        ?.let { add(stringResource(R.string.isee_exemption) to it) }
                },
            )
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    rows: List<Pair<String, String>>,
) {
    if (rows.isEmpty()) return
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(scheme.surfaceContainerHigh)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = title.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp,
            color = scheme.primary,
        )
        rows.forEach { (label, value) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = value,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.3f),
                )
            }
        }
    }
}

/** aaIscrId 2025 -> "25/26", for the leading year tile. */
private fun IseeDeclaration.shortYearLabel(): String {
    val year = academicYearEnrollmentId ?: return "—"
    return "%02d/%02d".format(year % 100, (year + 1) % 100)
}

private fun IseeDeclaration.academicYearLabel(): String =
    academicYearEnrollmentId?.let(::academicYearLabel) ?: "—"

/** aaIscrId 2025 -> "2025/26", for titles. */
private fun academicYearLabel(year: Long): String = "$year/${"%02d".format((year + 1) % 100)}"

/** Detail header title, exposed for the sheet entry's pinned header in MainShell. */
fun iseeDetailTitle(declaration: IseeDeclaration): String =
    "Anno accademico ${declaration.academicYearLabel()}"

/** Detail header subtitle, exposed for the sheet entry's pinned header in MainShell. */
fun iseeDetailSubtitle(declaration: IseeDeclaration): String =
    declaration.courseDescription ?: "Indicatore situazione economica"

private fun Context.openUrl(url: String) {
    runCatching {
        startActivity(
            Intent(Intent.ACTION_VIEW, url.toUri())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}
