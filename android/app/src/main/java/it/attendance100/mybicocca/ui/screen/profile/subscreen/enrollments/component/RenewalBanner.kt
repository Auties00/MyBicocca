package it.attendance100.mybicocca.ui.screen.profile.subscreen.enrollments.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.AutoMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.domain.model.enrollment.RenewalState

// The renewal affordance. There is no student REST submission path (confirmed against the
// OpenAPI specs), so the action honestly deep-links to the official Esse3 web flow; the copy
// makes that explicit rather than implying an in-app write.
@Composable
fun RenewalBanner(
    state: RenewalState,
    onRenew: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is RenewalState.Renewable -> RenewableCard(state.academicYear, onRenew, modifier)
        is RenewalState.Enrolled -> EnrolledCard(state.academicYear, modifier)
        RenewalState.NotApplicable -> Unit
    }
}

@Composable
private fun RenewableCard(
    academicYear: Int,
    onRenew: () -> Unit,
    modifier: Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(scheme.primaryContainer)
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(scheme.primary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoMode,
                    contentDescription = null,
                    tint = scheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.padding(end = 4.dp)) {
                Text(
                    text = "Rinnova l'iscrizione",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp,
                    color = scheme.onPrimaryContainer,
                )
                Text(
                    text = "Anno accademico $academicYear/${academicYear + 1}",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onPrimaryContainer.copy(alpha = 0.8f),
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Non risulta ancora un'iscrizione per quest'anno. Il rinnovo si completa sul " +
                "portale Esse3: si aprirà nel browser per confermare l'iscrizione e generare la prima rata.",
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium,
            color = scheme.onPrimaryContainer.copy(alpha = 0.85f),
        )
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = onRenew,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = scheme.primary,
                contentColor = Color.White,
            ),
            shape = RoundedCornerShape(100),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.size(8.dp))
            Text("Apri il rinnovo su Esse3", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EnrolledCard(
    academicYear: Int,
    modifier: Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(scheme.surfaceContainerHigh)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = PassedGreen,
            modifier = Modifier.size(24.dp),
        )
        Column {
            Text(
                text = "Iscrizione in regola",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
            )
            Text(
                text = "Sei iscritto per l'a.a. $academicYear/${academicYear + 1}.",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

private val PassedGreen = Color(0xFF1FA84B)
