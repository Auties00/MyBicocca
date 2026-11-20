package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.runtime.Composable
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.currentLocale
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergenMatch
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergenPreferences
import it.attendance100.mybicocca.domain.model.mensa.MensaDish
import it.attendance100.mybicocca.domain.model.mensa.MensaEligibility
import it.attendance100.mybicocca.domain.model.mensa.MensaMealRule
import it.attendance100.mybicocca.domain.model.mensa.MensaMealRuleId
import it.attendance100.mybicocca.domain.model.mensa.allergenMatch
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.component.feedback.friendlyMessage
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import it.attendance100.mybicocca.ui.component.modal.SheetMessage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.MensaTestTags
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.MensaDishArtwork
import java.text.Normalizer
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Every dish on the menus of the university canteens right now, with the meal combinations the
 * canteen accepts. The menu service only publishes the coming days, so this is "what is on offer",
 * not everything the kitchens could ever serve.
 */
@Composable
internal fun MensaFoodCatalogPage(
    catalog: Loadable<List<MensaDish>>,
    status: SyncStatus,
    eligibility: MensaEligibility?,
    allergenPreferences: MensaAllergenPreferences,
    onOpenDishes: (dishes: List<MensaDish>, startIndex: Int) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var courseFilter by rememberSaveable { mutableStateOf<Int?>(null) }
    val dishes = catalog.valueOrNull().orEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
            .testTag(MensaTestTags.FOOD_CATALOG_PAGE),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (eligibility != null) {
            EligibilityCard(eligibility = eligibility, modifier = Modifier.padding(horizontal = 20.dp))
        }

        when {
            dishes.isEmpty() && status is SyncStatus.Failed -> SheetMessage(
                icon = Icons.Outlined.CloudOff,
                title = stringResource(R.string.mensa_schedule_load_failed),
                body = stringResource(status.cause.friendlyMessage()),
                action = { RetryButton(onClick = onRetry) },
            )

            catalog is Loadable.NotYetLoaded -> Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                SheetLoadingIndicator(
                    label = stringResource(R.string.mensa_loading_schedule),
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            dishes.isEmpty() -> EmptyState(
                icon = Icons.Outlined.Restaurant,
                title = stringResource(R.string.mensa_no_dishes_for_day),
                body = "",
                modifier = Modifier.padding(24.dp),
            )

            else -> {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    placeholder = { Text(stringResource(R.string.mensa_catalog_search)) },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                )

                val allLabel = stringResource(R.string.mensa_course_all)
                val tiles = remember(dishes, allLabel) {
                    val byCourse = dishes.filter { it.courseId != null }.groupBy { it.courseId!! }
                    listOf(MensaCourseTile(null, allLabel, null)) + byCourse.entries
                        .sortedBy { courseRank(it.key) }
                        .map { (id, list) ->
                            MensaCourseTile(id, list.first().courseName ?: "", MensaDishArtwork.forCourse(id))
                        }
                }
                MensaCourseStrip(tiles = tiles, selectedId = courseFilter, onSelect = { courseFilter = it })

                val normalizedQuery = remember(query) { query.normalizedForSearch() }
                val visible = remember(dishes, courseFilter, normalizedQuery, allergenPreferences) {
                    dishes.filter { dish ->
                        (courseFilter == null || dish.courseId == courseFilter) &&
                            (normalizedQuery.isEmpty() || dish.name.normalizedForSearch().contains(normalizedQuery)) &&
                            !(allergenPreferences.hideMatchingDishes &&
                                dish.allergenMatch(allergenPreferences) != MensaAllergenMatch.NONE)
                    }.sortedWith(compareBy({ courseRank(it.courseId) }, { it.name }))
                }

                Text(
                    text = stringResource(R.string.mensa_catalog_count, visible.size),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )

                if (visible.isEmpty()) {
                    EmptyState(
                        icon = Icons.Outlined.Search,
                        title = stringResource(R.string.mensa_catalog_no_results),
                        body = "",
                        modifier = Modifier.padding(24.dp),
                    )
                } else {
                    MensaDishGrid(dishes = visible, modifier = Modifier.padding(horizontal = 20.dp)) { dish, cardModifier ->
                        MensaDishPlateCard(
                            dish = dish,
                            art = MensaDishArtwork.forDish(dish),
                            allergenMatch = dish.allergenMatch(allergenPreferences),
                            cartQty = 0,
                            showAdd = false,
                            onClick = { onOpenDishes(visible, visible.indexOfFirst { it.id == dish.id }) },
                            onAdd = {},
                            onDecrement = {},
                            modifier = cardModifier,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EligibilityCard(eligibility: MensaEligibility, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val locale = currentLocale()
    val verified = eligibility.lastVerified.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = scheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.mensa_eligibility_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            eligibility.rules.forEach { rule ->
                RuleRow(rule = rule, locale = locale)
            }
            // One line, whatever the font size the user has chosen: it shrinks to fit rather than wrapping.
            val noteStyle = MaterialTheme.typography.bodySmall
            Text(
                text = stringResource(R.string.mensa_eligibility_note, verified),
                style = noteStyle,
                color = scheme.onSurfaceVariant,
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(minFontSize = 7.sp, maxFontSize = noteStyle.fontSize, stepSize = 0.25.sp),
            )
        }
    }
}

@Composable
private fun RuleRow(rule: MensaMealRule, locale: java.util.Locale) {
    val scheme = MaterialTheme.colorScheme
    val (title, body) = when (rule.id) {
        MensaMealRuleId.FULL_MENU -> R.string.mensa_rule_full_title to R.string.mensa_rule_full_body
        MensaMealRuleId.REDUCED_MENU -> R.string.mensa_rule_reduced_title to R.string.mensa_rule_reduced_body
        MensaMealRuleId.SALAD_MENU -> R.string.mensa_rule_salad_title to R.string.mensa_rule_salad_body
        MensaMealRuleId.SINGLE_DISH_MENU -> R.string.mensa_rule_single_title to R.string.mensa_rule_single_body
        MensaMealRuleId.LUNCH_BOX_SANDWICH -> R.string.mensa_rule_box_sandwich_title to R.string.mensa_rule_box_sandwich_body
        MensaMealRuleId.LUNCH_BOX_PIZZA -> R.string.mensa_rule_box_pizza_title to R.string.mensa_rule_box_pizza_body
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(body),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
        }
        Surface(shape = RoundedCornerShape(12.dp), color = scheme.primaryContainer) {
            Text(
                text = String.format(locale, "%.2f €", rule.priceCents / 100.0),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = scheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
    }
}

private val COURSE_ORDER = listOf(1, 2, 14, 17, 12, 13, 4, 6, 7, 9)

private fun courseRank(courseId: Int?): Int =
    COURSE_ORDER.indexOf(courseId).let { if (it < 0) Int.MAX_VALUE else it }

private fun String.normalizedForSearch(): String =
    Normalizer.normalize(lowercase(), Normalizer.Form.NFD)
        .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        .trim()

@Preview(name = "All dishes", widthDp = MENSA_PREVIEW_WIDTH, heightDp = MENSA_PREVIEW_HEIGHT)
@Composable
private fun CatalogPreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        MensaFoodCatalogPage(
            catalog = Loadable.Loaded(MensaPreviewData.dishes),
            status = SyncStatus.Idle,
            eligibility = MensaPreviewData.eligibility,
            allergenPreferences = MensaPreviewData.glutenSelected,
            onOpenDishes = { _, _ -> },
            onRetry = {},
        )
    }
}
