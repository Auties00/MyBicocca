package it.unimib.indoornav.lab.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import it.unimib.indoornav.lab.format.BeaconFrameType

val BicoccaDarkColorScheme = darkColorScheme(
    primary = Color(0xFFD81648),
    onPrimary = Color(0xFF3D121A),
    primaryContainer = Color(0xFF7A1530),
    onPrimaryContainer = Color(0xFFFFD9DF),
    secondary = Color(0xFFE5BCC2),
    onSecondary = Color(0xFF432229),
    secondaryContainer = Color(0xFF5B3940),
    onSecondaryContainer = Color(0xFFFFD9DF),
    tertiary = Color(0xFFE9C28C),
    onTertiary = Color(0xFF412C04),
    background = Color(0xFF0B0808),
    onBackground = Color(0xFFF3E7E9),
    surface = Color(0xFF0B0808),
    onSurface = Color(0xFFF3E7E9),
    surfaceVariant = Color(0xFF261317),
    onSurfaceVariant = Color(0xFFB89398),
    surfaceContainerLowest = Color(0xFF0F0606),
    surfaceContainerLow = Color(0xFF140A0C),
    surfaceContainer = Color(0xFF1A0D10),
    surfaceContainerHigh = Color(0xFF261317),
    surfaceContainerHighest = Color(0xFF322028),
    outline = Color(0xFF7C5A60),
    outlineVariant = Color(0xFF3A1B22),
    error = Color(0xFFFFB4AB),
)

object BeaconHaloTokens {
    val OnlineFill = Color(0xFF7A1530)
    val OnlineStroke = Color(0xFF9E2344)
    const val ONLINE_MODEL_ALPHA = 1.0f

    val OfflineFill = Color(0xFF262122)
    val OfflineStroke = Color(0xFF3D3436)
    const val OFFLINE_MODEL_ALPHA = 0.76f
}

data class SignalBadgeStyle(
    val fullLabel: String,
    val shortLabel: String,
    val containerColor: Color,
    val contentColor: Color,
    val dotColor: Color,
)

object SignalBadgeTokens {
    val EdUid = SignalBadgeStyle(
        fullLabel = "ED-UID",
        shortLabel = "UID",
        containerColor = Color(0xFF123C25),
        contentColor = Color(0xFF79E8AA),
        dotColor = Color(0xFF4ADE80),
    )
    val IBeacon = SignalBadgeStyle(
        fullLabel = "iBeacon",
        shortLabel = "iB",
        containerColor = Color(0xFF103457),
        contentColor = Color(0xFF8DC8FF),
        dotColor = Color(0xFF60A5FA),
    )
    val EdTlm = SignalBadgeStyle(
        fullLabel = "ED-TLM",
        shortLabel = "TLM",
        containerColor = Color(0xFF4E1F06),
        contentColor = Color(0xFFFFB58A),
        dotColor = Color(0xFFFB923C),
    )
    val AltBeacon = SignalBadgeStyle(
        fullLabel = "AltBeacon",
        shortLabel = "Alt",
        containerColor = Color(0xFF341B59),
        contentColor = Color(0xFFD2B8FF),
        dotColor = Color(0xFFC084FC),
    )
    val Vendor = SignalBadgeStyle(
        fullLabel = "Vendor",
        shortLabel = "BLE",
        containerColor = Color(0xFF2E2426),
        contentColor = Color(0xFFC4B0B3),
        dotColor = Color(0xFFB89398),
    )

    fun forFrameType(type: BeaconFrameType): SignalBadgeStyle = when (type) {
        BeaconFrameType.ED_UID -> EdUid
        BeaconFrameType.IBEACON -> IBeacon
        BeaconFrameType.ED_TLM -> EdTlm
        BeaconFrameType.ALTBEACON -> AltBeacon
        BeaconFrameType.VENDOR -> Vendor
    }
}

object LabStatusColors {
    val Green = Color(0xFF4ADE80)
    val Amber = Color(0xFFFB923C)
    val Red = Color(0xFFFF6B6B)
}

@Composable
fun LabTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = BicoccaDarkColorScheme,
        content = content,
    )
}
