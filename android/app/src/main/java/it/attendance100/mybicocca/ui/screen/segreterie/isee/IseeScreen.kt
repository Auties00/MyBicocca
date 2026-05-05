package it.attendance100.mybicocca.ui.screen.segreterie.isee

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.data.model.isee.IseeDeclaration
import it.attendance100.mybicocca.ui.component.EmptyOfflineState
import it.attendance100.mybicocca.ui.component.EmptyState
import it.attendance100.mybicocca.ui.component.ErrorState
import it.attendance100.mybicocca.ui.component.StatusBar
import it.attendance100.mybicocca.ui.component.card.DitheredTexture
import it.attendance100.mybicocca.ui.component.card.SimpleCard
import it.attendance100.mybicocca.ui.component.profile.CreditCard
import it.attendance100.mybicocca.ui.component.shimmer.SkeletonIseeContent
import it.attendance100.mybicocca.ui.component.tax.formatCurrency
import it.attendance100.mybicocca.ui.theme.BadgeWhiteDrawableColor
import it.attendance100.mybicocca.ui.theme.OnBackgroundColor
import it.attendance100.mybicocca.util.UiFormatter
import it.attendance100.mybicocca.util.rememberPreferencesManager
import java.util.Locale.getDefault

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IseeScreen(
    viewModel: IseeViewModel = hiltViewModel(
        checkNotNull<ViewModelStoreOwner>(
            LocalViewModelStoreOwner.current
        ) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null
    ),
) {
    val iseeDeclaration by viewModel.iseeDeclaration.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
        indicator = {},
        modifier = Modifier.fillMaxSize(),
    ) {
        when {
            isRefreshing -> {
                Column {
                    StatusBar(
                        isOnline = isOnline,
                        errorMessage = error,
                        onDismissError = viewModel::clearError
                    )
                    SkeletonIseeContent()
                }
            }

            iseeDeclaration != null -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    IseeContent(iseeDeclaration!!)
                    StatusBar(
                        isOnline = isOnline,
                        errorMessage = error,
                        onDismissError = viewModel::clearError,
                    )
                }
            }

            !isOnline -> EmptyOfflineState()
            error != null -> ErrorState(message = error ?: "Errore")
            else -> EmptyState(message = "Nessuna dichiarazione ISEE trovata")
        }
    }
}

@Composable
private fun FrontIsee(
    iseeDeclaration: IseeDeclaration,
    textColor: Color = OnBackgroundColor,
    touchX: Float = 0.5f,
    touchY: Float = 0.5f,
    whiteBadge: Boolean = false,
) {
    val preferencesManager = rememberPreferencesManager()

    var movementCoeff = 30
    if (!preferencesManager.badgeParallax) movementCoeff = 0

    val drawableColor = if (whiteBadge) BadgeWhiteDrawableColor else textColor

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp, start = 40.dp)
    ) {
        // BG Texture
        DitheredTexture(
            modifier = Modifier
                .fillMaxSize()
                .offset(
                    x = (-190 + (-touchX + 0.5f) * (movementCoeff * 1.5)).dp,
                    y = ((-touchY + 0.5f) * (movementCoeff * 1.5)).dp
                )
                .rotate(10f),
            color = Color.Black,
            spacing = 10f,
            dotSize = 5f,
            globalRotation = 35f,
            dotRotation = 80f,
            fadeStart = 0.2f,
            fadeEnd = 1f
        )

        Image(
            Icons.Default.AccountBalance,
            contentDescription = null,
            modifier = Modifier
                .wrapContentSize()
                .size(500.dp)
                .scale(1.35f)
                .rotate(7.5f)
                .absoluteOffset(
                    x = (43 + (-touchX + 0.5f) * (movementCoeff)).dp,
                    y = (30 + (-touchY + 0.5f) * (movementCoeff / 2.5)).dp
                )
                .alpha(0.2f),
            colorFilter = ColorFilter.lighting(drawableColor, drawableColor)
        )

        // ISEE value centered
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 40.dp, bottom = 40.dp)
                .offset(y = (-5).dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = iseeDeclaration.iseeValue?.let { formatCurrency(it) } ?: "-",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily(
                    Font(R.font.bebas_neue_regular, FontWeight.Bold),
                )
            )
            Text(text = "ISEE", modifier = Modifier.offset(y = 35.dp))
        }
    }
}

@Composable
private fun IseeContent(iseeDeclaration: IseeDeclaration) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            CreditCard(
                enabled = false,
                sensorsEnabled = true,
                accentColor = primaryColor,
                isChromatic = false,
                frontContent = { x, y, whiteBadge, _ ->
                    FrontIsee(
                        iseeDeclaration,
                        touchX = x,
                        touchY = y,
                        whiteBadge = whiteBadge,
                    )
                },
                backContent = { _, _, _, _ ->
                    Box {}
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Info cards grid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(390.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        val band = iseeDeclaration.bandDescription ?: "-"
                        val displayBand = if (band.lowercase(getDefault()).startsWith("fascia "))
                            band.substring(7)
                        else
                            band
                        InfoCard(
                            label = "Fascia",
                            value = displayBand,
                            icon = Icons.AutoMirrored.Filled.FactCheck,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.6f),
                        )
                        InfoCard(
                            label = "Data Presentazione",
                            value = iseeDeclaration.presentationDate?.let { dateStr ->
                                runCatching {
                                    UiFormatter.getCustomDate(
                                        java.time.LocalDate.parse(dateStr, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE),
                                        getDefault(),
                                        "dd MMM yy"
                                    )
                                }.getOrDefault(dateStr)
                            } ?: "-",
                            icon = Icons.Default.CalendarMonth,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        InfoCard(
                            label = "ISPEE",
                            value = iseeDeclaration.ispeValue?.let { formatCurrency(it) } ?: "-",
                            icon = ImageVector.vectorResource(R.drawable.family_group),
                            modifier = Modifier.weight(0.7f),
                        )
                        InfoCard(
                            label = "Membri Famiglia",
                            value = iseeDeclaration.familyMemberCount.toString(),
                            icon = Icons.Default.FamilyRestroom,
                            modifier = Modifier.weight(0.5f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    label: String?,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    SimpleCard(modifier = modifier, onClick = {}, ditherImage = R.drawable.dither_160dp) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (label != null) Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                fontFamily = FontFamily(
                    Font(R.font.bebas_neue_regular, FontWeight.Bold),
                ),
            )
        }
    }
}
