package it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.AppointmentsViewModel

/**
 * Booking wizard step 1: pick a day and time from the live availability via [SlotPicker],
 * then continue to the form. Until the setup lands it shows the loading/error status body, a
 * plain message when the desk has no bookable offering, and a full empty-state page when the
 * whole booking horizon holds no open dates (mirroring the other sub-modal error pages).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SlotsPage(
    viewModel: AppointmentsViewModel,
    modifier: Modifier = Modifier,
) {
    val offerings by viewModel.offerings.collectAsStateWithLifecycle()
    val setupStatus by viewModel.setupStatus.collectAsStateWithLifecycle()

    if (offerings is Loadable.NotYetLoaded) {
        SheetStatusBody(syncStatus = setupStatus, onRetry = viewModel::retrySetup, modifier = modifier)
        return
    }

    val offering = viewModel.selectedOffering
    if (offering == null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 220.dp)
                .padding(24.dp),
        ) {
            Text(
                text = stringResource(R.string.appointments_desk_not_available),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val noAvailability by viewModel.noAvailability.collectAsStateWithLifecycle()
    if (noAvailability) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(bottom = 4.dp),
        ) {
            EmptyState(
                icon = Icons.Outlined.EventBusy,
                title = stringResource(R.string.appointments_no_dates),
                body = stringResource(R.string.appointments_no_dates_body),
            )
        }
        return
    }

    val month by viewModel.month.collectAsStateWithLifecycle()
    val monthAvailability by viewModel.monthAvailability.collectAsStateWithLifecycle()
    val availabilityStatus by viewModel.availabilityStatus.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val daySlots by viewModel.daySlots.collectAsStateWithLifecycle()
    val slotsStatus by viewModel.slotsStatus.collectAsStateWithLifecycle()
    val selectedSlot by viewModel.selectedSlot.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 720.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SlotPicker(
                month = month,
                canGoToPreviousMonth = viewModel.canGoToPreviousMonth,
                canGoToNextMonth = viewModel.canGoToNextMonth,
                monthAvailability = monthAvailability,
                availabilityStatus = availabilityStatus,
                selectedDate = selectedDate,
                daySlots = daySlots,
                slotsStatus = slotsStatus,
                selectedSlot = selectedSlot,
                enabled = true,
                onPreviousMonth = viewModel::goToPreviousMonth,
                onNextMonth = viewModel::goToNextMonth,
                onRetryMonth = viewModel::retryMonth,
                onSelectDate = viewModel::selectDate,
                onRetryDaySlots = viewModel::retryDaySlots,
                onSelectSlot = viewModel::selectSlot,
            )
        }

        ContinuaButton(
            enabled = selectedSlot != null,
            onClick = viewModel::goToForm,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 24.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ContinuaButton(enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    Button(
        onClick = onClick,
        enabled = enabled,
        shapes = ButtonDefaults.shapes(),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ButtonDefaults.MediumContainerHeight),
        contentPadding = ButtonDefaults.MediumContentPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (dark) scheme.primaryContainer else scheme.primary,
            contentColor = if (dark) scheme.onPrimaryContainer else scheme.onPrimary,
        ),
    ) {
        Text(
            stringResource(R.string.appointments_continue),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
