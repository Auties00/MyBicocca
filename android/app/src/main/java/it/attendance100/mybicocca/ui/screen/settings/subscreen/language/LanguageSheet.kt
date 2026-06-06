package it.attendance100.mybicocca.ui.screen.settings.subscreen.language

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.ui.component.button.MorphKnob
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val LOCALE_SYSTEM = "system"

// Chip is either a glyph (Sistema) or the locale's two-letter mark (IT / EN).
private data class LanguageOption(
    val code: String,
    val label: String,
    val chipText: String? = null,
    val chipIcon: ImageVector? = null,
)

private val LANGUAGE_OPTIONS = listOf(
    LanguageOption(LOCALE_SYSTEM, "Sistema", chipIcon = Icons.Outlined.Smartphone),
    LanguageOption("it", "Italiano", chipText = "IT"),
    LanguageOption("en", "English", chipText = "EN"),
)

// Label of the language the app is currently running with, for the settings tile subtitle.
fun currentAppLanguageLabel(context: Context): String {
    val current = currentAppLanguage(context)
    return LANGUAGE_OPTIONS.first { it.code == current }.label
}

// Language picker as a modal in the app's expressive language: a connected segmented card
// of neutral tiles — selection lives in the trailing MorphKnob (circle morphing to sunny
// on the motion-scheme springs), not in a container wash, like the Piano di Studi picks.
// Header text follows the edifici sheet style.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LanguageSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedLocale by remember { mutableStateOf(currentAppLanguage(context)) }
    var applying by remember { mutableStateOf(false) }

    PredictiveModalBottomSheet(
        onDismiss = onDismiss,
        sizeDuration = 500,
    ) { _, _ ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(
                text = "Lingua",
                style = MaterialTheme.typography.titleLargeEmphasized,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Lingua dell'app",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                LANGUAGE_OPTIONS.forEachIndexed { index, option ->
                    LanguageTile(
                        option = option,
                        selected = selectedLocale == option.code,
                        isFirst = index == 0,
                        isLast = index == LANGUAGE_OPTIONS.lastIndex,
                        onClick = {
                            if (selectedLocale == option.code) {
                                onDismiss()
                            } else if (!applying) {
                                applying = true
                                selectedLocale = option.code
                                scope.launch {
                                    // Let the knob morph play before applying and closing.
                                    // The locale lands as a configuration change (the
                                    // activity declares locale|layoutDirection), so the
                                    // sheet dismisses normally instead of being torn down.
                                    delay(350)
                                    setAppLanguage(context, option.code)
                                    onDismiss()
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageTile(
    option: LanguageOption,
    selected: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = rememberHapticManager()

    val large = 20.dp
    val small = 4.dp
    Surface(
        onClick = {
            haptic.tap()
            onClick()
        },
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        shape = RoundedCornerShape(
            topStart = if (isFirst) large else small,
            topEnd = if (isFirst) large else small,
            bottomEnd = if (isLast) large else small,
            bottomStart = if (isLast) large else small,
        ),
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(scheme.primaryContainer, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (option.chipIcon != null) {
                    Icon(
                        imageVector = option.chipIcon,
                        contentDescription = null,
                        tint = scheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp),
                    )
                } else {
                    Text(
                        text = option.chipText.orEmpty(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onPrimaryContainer,
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Text(
                text = option.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(10.dp))
            MorphKnob(checked = selected, uncheckedIcon = null)
        }
    }
}

private fun currentAppLanguage(context: Context): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val locales = context.getSystemService(LocaleManager::class.java).applicationLocales
        if (locales.isEmpty) LOCALE_SYSTEM else locales.get(0)?.language ?: LOCALE_SYSTEM
    } else {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) LOCALE_SYSTEM else locales.get(0)?.language ?: LOCALE_SYSTEM
    }

private fun setAppLanguage(context: Context, code: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.getSystemService(LocaleManager::class.java).applicationLocales =
            if (code == LOCALE_SYSTEM) LocaleList.getEmptyLocaleList() else LocaleList.forLanguageTags(
                code
            )
    } else {
        AppCompatDelegate.setApplicationLocales(
            if (code == LOCALE_SYSTEM) LocaleListCompat.getEmptyLocaleList() else LocaleListCompat.forLanguageTags(
                code
            ),
        )
    }
}
