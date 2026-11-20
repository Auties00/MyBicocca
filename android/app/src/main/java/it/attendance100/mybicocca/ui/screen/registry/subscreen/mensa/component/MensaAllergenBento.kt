package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergen
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergenKind
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.cleanAllergenName
import kotlin.math.max
import kotlin.math.roundToInt

/** The icon drawn for each allergen Compass publishes, by id; the name is the fallback for others. */
@DrawableRes
internal fun allergenIcon(allergen: MensaAllergen): Int = AllergenIcons[allergen.id] ?: allergenIconByName(allergen.name)

private val AllergenIcons = mapOf(
    2 to R.drawable.ic_allergen_crustaceans,
    3 to R.drawable.ic_allergen_egg,
    4 to R.drawable.ic_allergen_fish,
    5 to R.drawable.ic_allergen_peanut,
    6 to R.drawable.ic_allergen_soy,
    7 to R.drawable.ic_allergen_milk,
    9 to R.drawable.ic_allergen_celery,
    10 to R.drawable.ic_allergen_mustard,
    11 to R.drawable.ic_allergen_sesame,
    13 to R.drawable.ic_allergen_lupin,
    14 to R.drawable.ic_allergen_molluscs,
    18 to R.drawable.ic_allergen_pork,
    24 to R.drawable.ic_allergen_sulphites,
    37 to R.drawable.ic_allergen_wheat,
    38 to R.drawable.ic_allergen_pistachio,
    39 to R.drawable.ic_allergen_macadamia,
    40 to R.drawable.ic_allergen_pecan,
    41 to R.drawable.ic_allergen_brazil_nut,
    42 to R.drawable.ic_allergen_cashew,
    43 to R.drawable.ic_allergen_walnut,
    44 to R.drawable.ic_allergen_hazelnut,
    45 to R.drawable.ic_allergen_almond,
    46 to R.drawable.ic_allergen_spelt,
    47 to R.drawable.ic_allergen_khorasan,
    48 to R.drawable.ic_allergen_rye,
    49 to R.drawable.ic_allergen_barley,
    50 to R.drawable.ic_allergen_oats,
)

@DrawableRes
private fun allergenIconByName(raw: String): Int {
    val name = raw.lowercase()
    fun has(vararg words: String) = words.any { it in name }
    return when {
        has("farro") -> R.drawable.ic_allergen_spelt
        has("khorasan", "kamut") -> R.drawable.ic_allergen_khorasan
        has("segale") -> R.drawable.ic_allergen_rye
        has("orzo") -> R.drawable.ic_allergen_barley
        has("avena") -> R.drawable.ic_allergen_oats
        has("glutine", "grano", "cereal") -> R.drawable.ic_allergen_wheat
        has("crostace") -> R.drawable.ic_allergen_crustaceans
        has("uov") -> R.drawable.ic_allergen_egg
        has("pesce") -> R.drawable.ic_allergen_fish
        has("arachid") -> R.drawable.ic_allergen_peanut
        has("soia") -> R.drawable.ic_allergen_soy
        has("latte", "lattosio") -> R.drawable.ic_allergen_milk
        has("pistacch") -> R.drawable.ic_allergen_pistachio
        has("macadamia") -> R.drawable.ic_allergen_macadamia
        has("pecan") -> R.drawable.ic_allergen_pecan
        has("brasile") -> R.drawable.ic_allergen_brazil_nut
        has("anacard", "acagi") -> R.drawable.ic_allergen_cashew
        has("nocciol") -> R.drawable.ic_allergen_hazelnut
        has("mandorl") -> R.drawable.ic_allergen_almond
        has("noc", "guscio") -> R.drawable.ic_allergen_walnut
        has("sedano") -> R.drawable.ic_allergen_celery
        has("senape") -> R.drawable.ic_allergen_mustard
        has("sesamo") -> R.drawable.ic_allergen_sesame
        has("lupin") -> R.drawable.ic_allergen_lupin
        has("mollusc") -> R.drawable.ic_allergen_molluscs
        has("solfit", "solforos") -> R.drawable.ic_allergen_sulphites
        has("maiale", "suino") -> R.drawable.ic_allergen_pork
        else -> R.drawable.ic_allergen_generic
    }
}

/**
 * "Frutta a guscio: Noci di Macadamia" is shown as "Noci di Macadamia": the family is what the
 * tiles around it already say. Single allergens keep their short name ("Crostacei").
 */
internal fun allergenTileLabel(allergen: MensaAllergen): String {
    val plain = cleanAllergenName(allergen.name)
    return when {
        ':' in plain -> plain.substringAfter(':').trim().replaceFirstChar { it.uppercase() }
        allergen.shortName.isNotBlank() -> cleanAllergenName(allergen.shortName)
        else -> plain
    }
}

/** Where a tile sits in the four-column grid, and how many columns and rows it spans. */
internal data class BentoCell(val col: Int, val row: Int, val colSpan: Int = 1, val rowSpan: Int = 1)

/**
 * The hand-placed grid for the allergens Compass publishes: wheat large in the corner with the
 * other cereals under it, the nuts gathered around walnut, and a few long or tall tiles so the
 * grid does not read as a spreadsheet. Ids not listed here fill the gaps that are left, one cell each.
 */
internal val AllergenBentoCells = mapOf(
    37 to BentoCell(0, 0, 2, 2), // Grano
    7 to BentoCell(2, 0), // Latte
    3 to BentoCell(3, 0), // Uova
    4 to BentoCell(2, 1, 2, 1), // Pesce
    46 to BentoCell(0, 2), // Farro
    48 to BentoCell(1, 2), // Segale
    49 to BentoCell(2, 2), // Orzo
    50 to BentoCell(3, 2), // Avena
    47 to BentoCell(0, 3, 2, 1), // Khorasan
    2 to BentoCell(2, 3, 1, 2), // Crostacei
    14 to BentoCell(3, 3, 1, 2), // Molluschi
    5 to BentoCell(0, 4), // Arachidi
    6 to BentoCell(1, 4), // Soia
    43 to BentoCell(0, 5, 2, 2), // Noci
    45 to BentoCell(2, 5), // Mandorle
    44 to BentoCell(3, 5), // Nocciole
    38 to BentoCell(2, 6), // Pistacchio
    42 to BentoCell(3, 6), // Anacardi
    39 to BentoCell(0, 7), // Macadamia
    40 to BentoCell(1, 7), // Pecan
    41 to BentoCell(2, 7, 2, 1), // Noci del Brasile
    9 to BentoCell(0, 8, 1, 2), // Sedano
    10 to BentoCell(1, 8), // Senape
    11 to BentoCell(2, 8, 2, 1), // Sesamo
    13 to BentoCell(1, 9), // Lupini
    24 to BentoCell(2, 9, 2, 1), // Solfiti
)

/**
 * Places [ids] on a [columns]-wide grid: those in [cells] where the plan puts them (unless that
 * spot is taken), the rest one cell each in the first gaps, reading row by row.
 */
internal fun placeBento(ids: List<Int>, cells: Map<Int, BentoCell>, columns: Int = 4): List<BentoCell> {
    val taken = mutableSetOf<Pair<Int, Int>>()
    fun area(c: BentoCell) = (c.row until c.row + c.rowSpan).flatMap { r -> (c.col until c.col + c.colSpan).map { r to it } }
    val placed = arrayOfNulls<BentoCell>(ids.size)
    ids.forEachIndexed { i, id ->
        val cell = cells[id]?.takeIf { it.col + it.colSpan <= columns } ?: return@forEachIndexed
        val spots = area(cell)
        if (spots.none { it in taken }) {
            taken += spots
            placed[i] = cell
        }
    }
    var row = 0
    var col = 0
    ids.indices.forEach { i ->
        if (placed[i] != null) return@forEach
        while ((row to col) in taken) {
            col++
            if (col == columns) { col = 0; row++ }
        }
        placed[i] = BentoCell(col, row)
        taken += row to col
    }
    return placed.map { it!! }
}

/** A grid of allergen tiles in varied sizes; tapping one toggles it. */
@Composable
internal fun AllergenBento(
    allergens: List<MensaAllergen>,
    selectedIds: Set<Int>,
    onToggle: (MensaAllergen) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 4,
    gap: Dp = 8.dp,
) {
    val cells = remember(allergens) { placeBento(allergens.map { it.id }, AllergenBentoCells, columns) }
    // Rows grow with the font size the user chose, so two-line names are never cut.
    val minRow = 74.dp * max(1f, LocalDensity.current.fontScale)
    BentoGrid(cells = cells, columns = columns, gap = gap, minRowHeight = minRow, modifier = modifier) {
        allergens.forEachIndexed { index, allergen ->
            AllergenTile(
                label = allergenTileLabel(allergen),
                icon = allergenIcon(allergen),
                cell = cells[index],
                selected = allergen.id in selectedIds,
                onClick = { onToggle(allergen) },
            )
        }
    }
}

@Composable
private fun BentoGrid(
    cells: List<BentoCell>,
    columns: Int,
    gap: Dp,
    minRowHeight: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val gapPx = gap.roundToPx()
        val width = constraints.maxWidth
        val cellW = (width - gapPx * (columns - 1)) / columns
        val cellH = max((cellW * 0.92f).roundToInt(), minRowHeight.roundToPx())
        val rows = cells.maxOfOrNull { it.row + it.rowSpan } ?: 0
        val placeables = measurables.mapIndexed { i, m ->
            val c = cells[i]
            m.measure(
                Constraints.fixed(
                    width = cellW * c.colSpan + gapPx * (c.colSpan - 1),
                    height = cellH * c.rowSpan + gapPx * (c.rowSpan - 1),
                ),
            )
        }
        val height = if (rows == 0) 0 else rows * cellH + gapPx * (rows - 1)
        layout(width, height) {
            placeables.forEachIndexed { i, p ->
                val c = cells[i]
                p.place(c.col * (cellW + gapPx), c.row * (cellH + gapPx))
            }
        }
    }
}

/** One tile: a large one shows a big icon, a wide one puts the name beside it, a small one under it. */
@Composable
private fun AllergenTile(
    label: String,
    @DrawableRes icon: Int,
    cell: BentoCell,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val large = cell.colSpan > 1 && cell.rowSpan > 1
    val wide = cell.colSpan > 1 && cell.rowSpan == 1
    val iconSize = when {
        large -> 72.dp
        cell.rowSpan > 1 -> 48.dp
        wide -> 40.dp
        else -> 34.dp
    }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(if (large) 24.dp else 16.dp),
        color = if (selected) scheme.primaryContainer else scheme.surfaceContainerHigh,
        contentColor = if (selected) scheme.onPrimaryContainer else scheme.onSurface,
        border = if (selected) BorderStroke(1.5.dp, scheme.primary) else null,
        modifier = Modifier.semantics {
            role = Role.Checkbox
            stateDescription = if (selected) "on" else "off"
        },
    ) {
        val text: @Composable (Modifier, TextAlign) -> Unit = { mod, align ->
            Text(
                text = label,
                style = if (large) MaterialTheme.typography.titleSmall else MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                textAlign = align,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = mod,
            )
        }
        if (wide) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(iconSize))
                text(Modifier.weight(1f), TextAlign.Start)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (large) 10.dp else 4.dp, Alignment.CenterVertically),
            ) {
                Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(iconSize))
                text(Modifier, TextAlign.Center)
            }
        }
    }
}

@Preview(name = "Allergen bento", widthDp = MENSA_PREVIEW_WIDTH, heightDp = 1000)
@Composable
private fun AllergenBentoPreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        AllergenBento(
            allergens = MensaPreviewData.allergens.filter { it.kind == MensaAllergenKind.ALLERGEN },
            selectedIds = setOf(37, 7),
            onToggle = {},
            modifier = Modifier.padding(20.dp),
        )
    }
}

@Preview(name = "Allergen icons", widthDp = MENSA_PREVIEW_WIDTH, heightDp = 520)
@Composable
private fun AllergenIconsPreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MensaPreviewData.allergens.chunked(6).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { allergen ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.size(width = 52.dp, height = 64.dp)) {
                            Image(painterResource(allergenIcon(allergen)), contentDescription = null, modifier = Modifier.size(36.dp))
                            Text(allergenTileLabel(allergen), style = MaterialTheme.typography.labelSmall, maxLines = 2, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}
