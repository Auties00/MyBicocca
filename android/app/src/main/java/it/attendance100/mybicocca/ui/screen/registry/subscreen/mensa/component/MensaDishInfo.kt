package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalConfiguration
import it.attendance100.mybicocca.domain.model.mensa.MensaDish
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergen
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergenPreferences
import it.attendance100.mybicocca.domain.model.mensa.MensaDishDetail
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.MensaDescriptions
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.MensaHtml
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.cleanAllergenName
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import it.attendance100.mybicocca.core.os.currentLocale
import org.json.JSONObject

/** The dish's own description when the canteen wrote one (the generic blurb is not shown). */
@Composable
internal fun MensaDishDescription(info: MensaDishDetail, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val language = currentLocale().language
    // The canteen's own text when it says something about the dish; ours when it is the generic blurb.
    val description = remember(info.id, info.description, language) {
        MensaDescriptions.useful(info.description)?.toAnnotatedString()
            ?: MensaDishTexts.description(context, info.id, language)?.let(::AnnotatedString)
    } ?: return
    Text(
        text = description,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

/**
 * What matters most to this user, shown without opening the details: the allergens they chose that
 * the dish contains or may contain. Nothing is drawn when there are none. Works from the dish
 * alone, so it is there before the full details have loaded.
 */
@Composable
internal fun MensaDishFlags(
    dish: MensaDish,
    info: MensaDishDetail?,
    allergenPreferences: MensaAllergenPreferences,
    modifier: Modifier = Modifier,
) {
    val contains = (info?.majorAllergens ?: dish.majorAllergens).filter { it.flagged(allergenPreferences) }
    val traces = (info?.traceAllergens ?: dish.traceAllergens).filter { it.flagged(allergenPreferences) }
    if (contains.isEmpty() && traces.isEmpty()) return
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = scheme.errorContainer,
        contentColor = scheme.onErrorContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Outlined.WarningAmber, contentDescription = null, modifier = Modifier.size(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (contains.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.mensa_dish_flagged_contains, contains.joinToString(", ") { cleanAllergenName(it.name) }),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                if (traces.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.mensa_dish_flagged_traces, traces.joinToString(", ") { cleanAllergenName(it.name) }),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

/** The full details of a dish in a sheet of their own, so the carousel page keeps to the essentials. */
@Composable
internal fun MensaDishDetailsSheet(
    dishName: String,
    info: MensaDishDetail,
    allergenPreferences: MensaAllergenPreferences,
    onDismiss: () -> Unit,
) {
    val maxHeight = (LocalConfiguration.current.screenHeightDp * 0.82f).dp
    PredictiveModalBottomSheet(onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxHeight)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = dishName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            MensaDishInfoSections(info = info, allergenPreferences = allergenPreferences)
        }
    }
}

/** Allergens, ingredients, nutrition and reference intake of a dish. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MensaDishInfoSections(
    info: MensaDishDetail,
    allergenPreferences: MensaAllergenPreferences,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = scheme.surfaceContainer,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = stringResource(R.string.mensa_dish_allergens_major),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                if (info.majorAllergens.isEmpty()) {
                    Text(
                        text = stringResource(R.string.mensa_dish_no_allergens),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        // What matters to the user comes first.
                        info.majorAllergens
                            .sortedByDescending { it.flagged(allergenPreferences) }
                            .forEach { allergen -> AllergenChip(allergen, allergen.flagged(allergenPreferences)) }
                    }
                }
                if (info.traceAllergens.isNotEmpty()) {
                    val flaggedColor = scheme.error
                    Text(
                        text = buildAnnotatedString {
                            append(stringResource(R.string.mensa_dish_allergens_trace))
                            append(": ")
                            info.traceAllergens.forEachIndexed { index, allergen ->
                                if (index > 0) append(", ")
                                if (allergen.flagged(allergenPreferences)) {
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = flaggedColor)) {
                                        append(cleanAllergenName(allergen.name))
                                    }
                                } else {
                                    append(cleanAllergenName(allergen.name))
                                }
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = stringResource(R.string.mensa_dish_allergen_disclaimer),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                )
            }
        }

        if (info.ingredients.isNotEmpty()) {
            InfoCard(title = stringResource(R.string.mensa_dish_ingredients)) {
                info.ingredients.forEach { ing ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = remember(ing.name) { MensaHtml.parse(ing.name).toAnnotatedString() },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        ing.quantity?.let { qty ->
                            Text(
                                text = MensaHtml.plain(qty),
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        if (info.displayNutrition && info.nutritionRows.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = scheme.surfaceContainer,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(R.string.mensa_dish_nutrition),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1.4f),
                        )
                        Text(
                            text = stringResource(R.string.mensa_dish_nutrition_portion),
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant,
                            modifier = Modifier.weight(0.8f),
                        )
                        Text(
                            text = stringResource(R.string.mensa_dish_nutrition_100g),
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant,
                            modifier = Modifier.weight(0.8f),
                        )
                    }
                    HorizontalDivider()
                    info.nutritionRows.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = MensaHtml.plain(row.name),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1.4f),
                            )
                            Text(
                                text = row.portionValue?.let { "${formatNum(it)} ${row.unit}" } ?: "—",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(0.8f),
                            )
                            Text(
                                text = row.per100gValue?.let { "${formatNum(it)} ${row.unit}" } ?: "—",
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant,
                                modifier = Modifier.weight(0.8f),
                            )
                        }
                    }
                }
            }
        }

        if (info.gdaItems.isNotEmpty()) {
            InfoCard(title = stringResource(R.string.mensa_dish_gda), spacing = 10.dp) {
                info.gdaItems.forEach { item ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(MensaHtml.plain(item.name), style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = "${item.formattedValue} (${item.percentage.toInt()}%)",
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.onSurfaceVariant,
                            )
                        }
                        LinearProgressIndicator(
                            progress = { (item.percentage / 100.0).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            drawStopIndicator = {},
                        )
                    }
                }
            }
        }
    }
}

/** A titled rounded card holding a list of rows. */
@Composable
private fun InfoCard(
    title: String,
    spacing: androidx.compose.ui.unit.Dp = 8.dp,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(spacing),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            content()
        }
    }
}

/**
 * Red only for what the user chose in the allergen settings; every other declared allergen or
 * content is a plain chip, so the colour keeps meaning "this one is yours".
 */
@Composable
private fun AllergenChip(allergen: MensaAllergen, flagged: Boolean) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (flagged) scheme.errorContainer else scheme.surfaceContainerHighest,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (flagged) {
                Icon(
                    imageVector = Icons.Outlined.WarningAmber,
                    contentDescription = null,
                    tint = scheme.onErrorContainer,
                    modifier = Modifier.size(14.dp),
                )
            }
            Text(
                text = cleanAllergenName(allergen.name),
                style = MaterialTheme.typography.labelMedium,
                color = if (flagged) scheme.onErrorContainer else scheme.onSurface,
            )
        }
    }
}

private fun MensaAllergen.flagged(preferences: MensaAllergenPreferences): Boolean =
    !isFrozenNotice && id in preferences.allergenIds

private fun formatNum(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else "%.1f".format(value)

@Preview(name = "Dish flags", widthDp = MENSA_PREVIEW_WIDTH, heightDp = 160)
@Composable
private fun DishFlagsPreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        MensaDishFlags(
            dish = MensaPreviewData.containsDish,
            info = null,
            allergenPreferences = MensaPreviewData.glutenSelected,
            modifier = Modifier.padding(20.dp),
        )
    }
}

/**
 * Descriptions written for each dish from its name and ingredients, bundled with the app as
 * `assets/mensa/dish_descriptions.json` (dish id -> Italian and English text). Read once.
 */
internal object MensaDishTexts {
    private const val ASSET = "mensa/dish_descriptions.json"

    @Volatile
    private var texts: JSONObject? = null

    fun description(context: Context, dishId: Int, language: String): String? {
        val all = texts ?: runCatching {
            JSONObject(context.assets.open(ASSET).bufferedReader().use { it.readText() })
        }.getOrNull()?.also { texts = it } ?: return null
        val entry = all.optJSONObject(dishId.toString()) ?: return null
        return entry.optString(if (language == "it") "it" else "en").takeIf { it.isNotBlank() }
    }
}
